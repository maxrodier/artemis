package ca.artemis.vulkan.rendering.renderer;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.vulkan.api.commands.CommandBuffer;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PresentInfo;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.Swapchain;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.memory.VulkanSampler;
import ca.artemis.vulkan.api.pipeline.ViewportState;
import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.programs.SwapchainShaderProgram;

public class SwapchainRenderer extends Renderer {

    private final RenderingEngine renderingEngine;
    private final Swapchain swapchain;

    private final VulkanSampler textureSampler;
    private final SwapchainShaderProgram swapchainShaderProgram;

    private final CommandPool commandPool;

    private final Quad quad;
    private final DescriptorSet[][] descriptorSets;

    private CommandBuffer[] drawCommandBuffers;

    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanFence renderFence;

    private final IntBuffer pImageIndex;
    private final SubmitInfo submitInfo;
    private PresentInfo presentInfo;

    public SwapchainRenderer(VulkanContext context, RenderingEngine renderingEngine, Swapchain swapchain, VulkanSemaphore waitSemaphore, VulkanImageView displayImageView) {
        super(context.getDevice(), waitSemaphore);

        this.renderingEngine = renderingEngine;
        this.swapchain = swapchain;

        this.textureSampler = createTextureSampler(context.getDevice());
        this.swapchainShaderProgram = new SwapchainShaderProgram(context.getDevice(), this.swapchain.getRenderPass(), this.swapchain.getFramebuffers().length);

        this.commandPool = new CommandPool(context.getDevice(), context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        
        this.quad = new Quad(context);
        this.descriptorSets = createDescriptorSets(context.getDevice(), this.swapchainShaderProgram, this.swapchain.getFramebuffers().length);
        updateDescriptorSets(context, this.descriptorSets, displayImageView, this.textureSampler);
        
        this.drawCommandBuffers = createCommandBuffers(context, this.commandPool, this.swapchain, this.quad, this.swapchainShaderProgram, this.descriptorSets);

        this.imageAcquiredSemaphore = new VulkanSemaphore(context.getDevice());
        this.renderFence = new VulkanFence(context.getDevice());

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

    public void destroy(VulkanContext context) {
        this.presentInfo.destroy(context.getDevice());
        this.submitInfo.destroy();
        MemoryUtil.memFree(this.pImageIndex);
        this.renderFence.destroy(context.getDevice());
        this.imageAcquiredSemaphore.destroy(context.getDevice());
        for(CommandBuffer commandBuffer : drawCommandBuffers)
            commandBuffer.destroy(context.getDevice(), this.commandPool);
        this.quad.destroy(context.getMemoryAllocator());
        this.commandPool.destroy(context.getDevice());
        this.swapchainShaderProgram.destroy(context.getDevice());
        this.textureSampler.destroy(context.getDevice());
        super.destroy(context.getDevice());
    }

    private DescriptorSet[][] createDescriptorSets(VulkanDevice device, SwapchainShaderProgram swapchainShaderProgram, int size) {
        DescriptorSet[][] descriptorSets = new DescriptorSet[size][];
        for(int i = 0; i < size; i++) {
            descriptorSets[i] = swapchainShaderProgram.allocate(device);
        }
        return descriptorSets;
    }

    private static CommandBuffer[] createCommandBuffers(VulkanContext context, CommandPool commandPool, Swapchain swapchain, Quad quad, SwapchainShaderProgram swapchainShaderProgram, DescriptorSet[][] descriptorSets) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkResetCommandPool(context.getDevice().getHandle(), commandPool.getHandle(), 0);

            CommandBuffer[] drawCommandBuffers = new CommandBuffer[swapchain.getFramebuffers().length];
            for(int i = 0; i < swapchain.getFramebuffers().length; i++) {
                
                PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(context.getDevice(), commandPool);

                VkViewport.Buffer pViewports = VkViewport.calloc(1);
                pViewports.put(0, new ViewportState.Viewport(0, 0, context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height(), 0.0f, 1.0f).build(stack));

                VkRect2D.Buffer pScissors = VkRect2D.calloc(1);
                pScissors.put(0, new ViewportState.Scissor(0, 0, context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height()).build(stack));
                
                VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1);
                pClearValues.get(0).color()
                    .float32(0, 36f/255f)
                    .float32(1, 10f/255f)
                    .float32(2, 48f/255f)
                    .float32(3, 1);

                commandBuffer.beginRecording(stack, 0);

                commandBuffer.setViewportCmd(pViewports);
                commandBuffer.setScissorCmd(pScissors);

                commandBuffer.beginRenderPassCmd(stack, swapchain.getRenderPass().getHandle(), swapchain.getFramebuffer(i).getHandle(), context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height(), pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);
                
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

    private static VulkanSampler createTextureSampler(VulkanDevice device) {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .build(device);

        return textureSampler;
    }

    private static void updateDescriptorSets(VulkanContext context, DescriptorSet[][] descriptorSets, VulkanImageView textureImageView, VulkanSampler textureSampler) {
    	for(DescriptorSet[] sets : descriptorSets) {
			sets[0].updateDescriptorImageBuffer(context.getDevice(), textureImageView, textureSampler, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
		}
    }

    @Override
    public void recreateRenderer(VulkanContext context) {
        this.drawCommandBuffers = createCommandBuffers(context, commandPool, swapchain, quad, swapchainShaderProgram, descriptorSets);
        this.presentInfo = new PresentInfo()
            .setWaitSemaphores(this.signalSemaphore)
            .setSwapchains(this.swapchain)
            .setImageIndexPointer(pImageIndex);
    }

    @Override
    public void draw(VulkanDevice device, MemoryStack stack) {
        int error = KHRSwapchain.vkAcquireNextImageKHR(device.getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if(error == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
            renderingEngine.recreateRenderingEngine();
            return;
        } else if (error != VK11.VK_SUCCESS && error != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new AssertionError("Failed to acquire next swapchain image");
        }

        submitInfo.setCommandBuffers(drawCommandBuffers[pImageIndex.get(0)]);
        submitInfo.submit(device, device.getGraphicsQueue());

        if(presentInfo.present(device))
            renderingEngine.recreateRenderingEngine();
    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }
}
