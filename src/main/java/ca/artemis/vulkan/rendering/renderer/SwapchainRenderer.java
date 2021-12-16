package ca.artemis.vulkan.rendering.renderer;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.commands.CommandBuffer;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PresentInfo;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.Swapchain;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.memory.VulkanSampler;
import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.programs.SwapchainShaderProgram;

public class SwapchainRenderer extends Renderer {

    private final Swapchain swapchain;

    private final VulkanSampler textureSampler;
    private final SwapchainShaderProgram swapchainShaderProgram;

    private final CommandPool commandPool;

    private final Quad quad;
    private final DescriptorSet[][] descriptorSets;

    private final CommandBuffer[] drawCommandBuffers;

    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanFence renderFence;

    private final IntBuffer pImageIndex;
    private final SubmitInfo submitInfo;
    private final PresentInfo presentInfo;

    public SwapchainRenderer(Swapchain swapchain, VulkanSemaphore waitSemaphore, VulkanImageView displayImageView) {
        super(waitSemaphore);

        this.swapchain = swapchain;

        this.textureSampler = createTextureSampler();
        this.swapchainShaderProgram = new SwapchainShaderProgram(this.swapchain.getRenderPass(), this.swapchain.getFramebuffers().length);

        this.commandPool = new CommandPool(VulkanContext.getContext().getDevice(), VulkanContext.getContext().getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        
        this.quad = new Quad();
        this.descriptorSets = createDescriptorSets(this.swapchainShaderProgram, this.swapchain.getFramebuffers().length);
        updateDescriptorSets(this.descriptorSets, displayImageView, this.textureSampler);
        
        this.drawCommandBuffers = createCommandBuffers(this.commandPool, this.swapchain, this.quad, this.swapchainShaderProgram, this.descriptorSets);

        this.imageAcquiredSemaphore = new VulkanSemaphore();
        this.renderFence = new VulkanFence();

        this.pImageIndex = MemoryUtil.memCallocInt(1);
        this.submitInfo = new SubmitInfo(this.renderFence)
            .setWaitSemaphores(this.waitSemaphore, this.imageAcquiredSemaphore)
            .setWaitDstStageMask(VK11.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            .setSignalSemaphores(this.signalSemaphore);
        this.presentInfo = new PresentInfo()
            .setWaitSemaphores(this.signalSemaphore)
            .setSwapchains(this.swapchain)
            .setImageIndexPointer(pImageIndex);
    }

    public void destroy() {
        this.presentInfo.destroy();
        this.submitInfo.destroy();
        MemoryUtil.memFree(this.pImageIndex);
        this.renderFence.destroy();
        this.imageAcquiredSemaphore.destroy();
        for(CommandBuffer commandBuffer : drawCommandBuffers)
            commandBuffer.destroy();
        this.quad.destroy();
        this.commandPool.destroy(VulkanContext.getContext().getDevice());
        this.swapchainShaderProgram.destroy();
        this.textureSampler.destroy();
        super.destroy();
    }

    private DescriptorSet[][] createDescriptorSets(SwapchainShaderProgram swapchainShaderProgram, int size) {
        DescriptorSet[][] descriptorSets = new DescriptorSet[size][];
        for(int i = 0; i < size; i++) {
            descriptorSets[i] = swapchainShaderProgram.allocate();
        }
        return descriptorSets;
    }

    private static CommandBuffer[] createCommandBuffers(CommandPool commandPool, Swapchain swapchain, Quad quad, SwapchainShaderProgram swapchainShaderProgram, DescriptorSet[][] descriptorSets) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkResetCommandPool(VulkanContext.getContext().getDevice().getHandle(), commandPool.getHandle(), 0);

            CommandBuffer[] drawCommandBuffers = new CommandBuffer[swapchain.getFramebuffers().length];
            for(int i = 0; i < swapchain.getFramebuffers().length; i++) {
                
                PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
                
                VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1);
                pClearValues.get(0).color()
                    .float32(0, 36f/255f)
                    .float32(1, 10f/255f)
                    .float32(2, 48f/255f)
                    .float32(3, 1);

                commandBuffer.beginRecording(stack, 0);
                commandBuffer.beginRenderPassCmd(stack, swapchain.getRenderPass().getHandle(), swapchain.getFramebuffer(i).getHandle(), Configuration.windowWidth, Configuration.windowHeight, pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);

                commandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline());
                commandBuffer.bindVertexBufferCmd(stack, quad.getVertexBuffer());
                commandBuffer.bindIndexBufferCmd(quad.getIndexBuffer());
                commandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSets[i]);
                commandBuffer.drawIndexedCmd(quad.getIndexBuffer().getLenght(), 1);

                commandBuffer.endRenderPassCmd();
                commandBuffer.endRecording();
                
                drawCommandBuffers[i] = commandBuffer;
            }
            
            return drawCommandBuffers;
        }
    }

    private static VulkanSampler createTextureSampler() {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .build();

        return textureSampler;
    }

    private static void updateDescriptorSets(DescriptorSet[][] descriptorSets, VulkanImageView textureImageView, VulkanSampler textureSampler) {
    	for(DescriptorSet[] sets : descriptorSets) {
			sets[0].updateDescriptorImageBuffer(textureImageView, textureSampler, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
		}
    }

    @Override
    public void draw(MemoryStack stack) {
        int error = KHRSwapchain.vkAcquireNextImageKHR(VulkanContext.getContext().getDevice().getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if (error != VK11.VK_SUCCESS) {
            throw new AssertionError("Failed to acquire next swapchain image");
        }

        submitInfo.setCommandBuffers(drawCommandBuffers[pImageIndex.get(0)]);
        submitInfo.submit(VulkanContext.getContext().getDevice().getGraphicsQueue());

        presentInfo.present();
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }
}
