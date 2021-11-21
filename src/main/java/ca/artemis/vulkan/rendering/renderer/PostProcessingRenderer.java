package ca.artemis.vulkan.rendering.renderer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.commands.CommandBuffer;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SubmitInfo;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.FramebufferObject.Attachment;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.framebuffer.SceneFramebufferObject;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.memory.VulkanSampler;
import ca.artemis.vulkan.api.pipeline.ColorBlendState;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.api.pipeline.ShaderModule;
import ca.artemis.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.vulkan.api.pipeline.VertexInputState;
import ca.artemis.vulkan.api.pipeline.ViewportState;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.vulkan.rendering.mesh.Quad;

public class PostProcessingRenderer extends Renderer {

    private final Quad quad;

    private final SceneFramebufferObject sceneFramebufferObject;

    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout descriptorSetLayout;
    private final DescriptorSet descriptorSet;
    private final GraphicsPipeline graphicsPipeline;

    private final VulkanSampler textureSampler;

    private final CommandPool commandPool;
    private final CommandBuffer drawCommandBuffer;

    private final SubmitInfo submitInfo;

    public PostProcessingRenderer(VulkanSemaphore waitSemaphore, VulkanImageView displayImageView) {
        super(waitSemaphore);

        this.sceneFramebufferObject = new SceneFramebufferObject();

        this.quad = new Quad();

        this.descriptorPool = createDescriptorPool(1);
        this.descriptorSetLayout = createDescriptorSetLayout();
        this.descriptorSet = new DescriptorSet(this.descriptorPool, this.descriptorSetLayout);
        this.graphicsPipeline = createGraphicsPipeline(this.descriptorSetLayout, this.sceneFramebufferObject.getRenderPass());

        this.textureSampler = createTextureSampler();
        updateDescriptorSet(this.descriptorSet, displayImageView, textureSampler);

        this.commandPool = new CommandPool(VulkanContext.getContext().getDevice(), VulkanContext.getContext().getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.drawCommandBuffer = createCommandBuffer(this.commandPool, this.quad, this.sceneFramebufferObject, this.graphicsPipeline, this.descriptorSet);

        this.submitInfo = new SubmitInfo()
            .setWaitSemaphores(this.waitSemaphore)
            .setWaitDstStageMask(VK11.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
            .setCommandBuffers(drawCommandBuffer)
            .setSignalSemaphores(this.signalSemaphore);
    }

    public void destroy() {
        this.submitInfo.destroy();
        this.drawCommandBuffer.destroy(this.commandPool);
        this.commandPool.destroy(VulkanContext.getContext().getDevice());
        this.textureSampler.destroy();
        this.graphicsPipeline.destroy();
        this.descriptorPool.destroy();
        this.descriptorSetLayout.destroy();
        this.quad.destroy();
        this.sceneFramebufferObject.destroy();
        super.destroy();
    }

    private DescriptorPool createDescriptorPool(int size) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, size)
            .setMaxSets(size)
            .build();
    }

    private DescriptorSetLayout createDescriptorSetLayout() {
        return new DescriptorSetLayout.Builder()
            .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK11.VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
    }

    private static GraphicsPipeline createGraphicsPipeline(DescriptorSetLayout descriptorSetLayout, RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule("src/main/resources/shaders/swapchain.vert", ShaderStageKind.VERTEX_SHADER))
            .addShaderModule(new ShaderModule("src/main/resources/shaders/swapchain.frag", ShaderStageKind.FRAGMENT_SHADER))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 20, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32_SFLOAT, 12)))
            .setViewportState(new ViewportState()
                .addViewport(new ViewportState.Viewport(0, 0, Configuration.windowWidth, Configuration.windowHeight, 0.0f, 1.0f))
                .addScissors(new ViewportState.Scissor(0, 0, Configuration.windowWidth, Configuration.windowHeight)))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(new DescriptorSetLayout[] {descriptorSetLayout})
            .setRenderPass(renderPass)
            .build();
    }
    
    private static VulkanSampler createTextureSampler() {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .build();

        return textureSampler;
    }

    private static void updateDescriptorSet(DescriptorSet descriptorSet, VulkanImageView textureImageView, VulkanSampler textureSampler) {
        descriptorSet.updateDescriptorImageBuffer(textureImageView, textureSampler, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

    private static CommandBuffer createCommandBuffer(CommandPool commandPool, Quad quad, SceneFramebufferObject sceneFramebufferObject, GraphicsPipeline graphicsPipeline, DescriptorSet descriptorSet) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkResetCommandPool(VulkanContext.getContext().getDevice().getHandle(), commandPool.getHandle(), 0);
   
            PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
            
            VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1);
            pClearValues.get(0).color()
                .float32(0, 36f/255f)
                .float32(1, 10f/255f)
                .float32(2, 48f/255f)
                .float32(3, 1);

            commandBuffer.beginRecording(stack, 0);
            commandBuffer.beginRenderPassCmd(stack, sceneFramebufferObject.getRenderPass().getHandle(), sceneFramebufferObject.getFramebuffer().getHandle(), Configuration.windowWidth, Configuration.windowHeight, pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);

            commandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
            commandBuffer.bindVertexBufferCmd(stack, quad.getVertexBuffer());
            commandBuffer.bindIndexBufferCmd(quad.getIndexBuffer());
            commandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getPipelineLayout(), descriptorSet);
            commandBuffer.drawIndexedCmd(quad.getIndexBuffer().getLenght(), 1);

            commandBuffer.endRenderPassCmd();
            commandBuffer.endRecording();
            
            return commandBuffer;
        }
    }

    @Override
    public void draw(MemoryStack stack) {
        submitInfo.submit(VulkanContext.getContext().getDevice().getGraphicsQueue());
    }

    public VulkanImageView getDisplayImage() {
        return sceneFramebufferObject.getAttachment(Attachment.COLOR).getImageView();
    }
}
