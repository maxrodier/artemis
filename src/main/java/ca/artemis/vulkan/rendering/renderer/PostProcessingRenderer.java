package ca.artemis.vulkan.rendering.renderer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.vulkan.api.commands.CommandBuffer;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.FramebufferObject.Attachment;
import ca.artemis.vulkan.api.framebuffer.SceneFramebufferObject;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.memory.VulkanSampler;
import ca.artemis.vulkan.api.pipeline.ViewportState;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.vulkan.rendering.mesh.Quad;
import ca.artemis.vulkan.rendering.programs.PostProcessingShaderProgram;

public class PostProcessingRenderer extends Renderer {

    private final SceneFramebufferObject sceneFramebufferObject;

    private final VulkanSampler textureSampler;
    private final PostProcessingShaderProgram postProcessingShaderProgram;

    private final CommandPool commandPool;

    private final Quad quad;
    private final DescriptorSet[] descriptorSets;

    private CommandBuffer drawCommandBuffer;

    private final SubmitInfo submitInfo;

    public PostProcessingRenderer(VulkanContext context, VulkanSemaphore waitSemaphore, VulkanImageView displayImageView) {
        super(context.getDevice(), waitSemaphore);

        this.sceneFramebufferObject = new SceneFramebufferObject(context, context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height());

        this.textureSampler = createTextureSampler(context.getDevice());
        this.postProcessingShaderProgram = new PostProcessingShaderProgram(context.getDevice(), this.sceneFramebufferObject.getRenderPass());

        this.commandPool = new CommandPool(context.getDevice(), context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        
        this.quad = new Quad(context);
        this.descriptorSets = postProcessingShaderProgram.allocate(context.getDevice());
        updateDescriptorSets(context, this.descriptorSets, displayImageView, textureSampler);

        this.drawCommandBuffer = createCommandBuffer(context, this.commandPool, this.quad, this.sceneFramebufferObject, this.postProcessingShaderProgram, this.descriptorSets);

        this.submitInfo = new SubmitInfo()
            .setWaitSemaphores(this.waitSemaphore)
            .setWaitDstStageMask(VK11.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
            .setCommandBuffers(drawCommandBuffer)
            .setSignalSemaphores(this.signalSemaphore);
    }

    public void destroy(VulkanContext context) {
        this.submitInfo.destroy();
        this.drawCommandBuffer.destroy(context.getDevice(), this.commandPool);
        this.quad.destroy(context.getMemoryAllocator());
        this.commandPool.destroy(context.getDevice());
        this.postProcessingShaderProgram.destroy(context.getDevice());
        this.textureSampler.destroy(context.getDevice());
        this.sceneFramebufferObject.destroy(context);
        super.destroy(context.getDevice());
    }

    private static CommandBuffer createCommandBuffer(VulkanContext context, CommandPool commandPool, Quad quad, SceneFramebufferObject sceneFramebufferObject, PostProcessingShaderProgram postProcessingShaderProgram, DescriptorSet[] descriptorSets) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkResetCommandPool(context.getDevice().getHandle(), commandPool.getHandle(), 0);
   
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
            commandBuffer.beginRenderPassCmd(stack, sceneFramebufferObject.getRenderPass().getHandle(), sceneFramebufferObject.getFramebuffer().getHandle(), context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height(), pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);

            commandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, postProcessingShaderProgram.getGraphicsPipeline());
            commandBuffer.bindVertexBufferCmd(stack, quad.getVertexBuffer());
            commandBuffer.bindIndexBufferCmd(quad.getIndexBuffer());
            commandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, postProcessingShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSets);
            commandBuffer.drawIndexedCmd(quad.getIndexBuffer().getLenght(), 1);

            commandBuffer.endRenderPassCmd();
            commandBuffer.endRecording();
            
            return commandBuffer;
        }
    }

    private static VulkanSampler createTextureSampler(VulkanDevice device) {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .build(device);

        return textureSampler;
    }

    private static void updateDescriptorSets(VulkanContext context, DescriptorSet[] descriptorSets, VulkanImageView textureImageView, VulkanSampler textureSampler) {
        for(DescriptorSet descriptorSet : descriptorSets) {
			descriptorSet.updateDescriptorImageBuffer(context.getDevice(), textureImageView, textureSampler, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
		}
    }

    @Override
    public void recreateRenderer(VulkanContext context) {
        this.sceneFramebufferObject.regenerateFramebuffer(context, context.getSurfaceCapabilities().currentExtent().width(), context.getSurfaceCapabilities().currentExtent().height());
        this.drawCommandBuffer = createCommandBuffer(context, commandPool, quad, sceneFramebufferObject, postProcessingShaderProgram, descriptorSets);
    }

    @Override
    public void draw(VulkanDevice device, MemoryStack stack) {
        submitInfo.submit(device, device.getGraphicsQueue());
    }

    public VulkanImageView getDisplayImage() {
        return sceneFramebufferObject.getAttachment(Attachment.COLOR).getImageView();
    }
}
