package ca.artemis.vulkan.rendering.programs;

import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.pipeline.ColorBlendState;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.api.pipeline.RasterizationState;
import ca.artemis.vulkan.api.pipeline.ShaderModule;
import ca.artemis.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.vulkan.api.pipeline.VertexInputState;
import ca.artemis.vulkan.api.pipeline.ViewportState;

public class SwapchainShaderProgram extends ShaderProgram {

    public SwapchainShaderProgram(VulkanDevice device, RenderPass renderPass) {
        super(device, renderPass); //TODO: Find correct poolsize
    }

    public DescriptorSetLayout getDescriptorSetLayout() {
        return descriptorSetLayout;
    }

    @Override
    protected DescriptorSetLayout createDescriptorSetLayout(VulkanDevice device) {
        return new DescriptorSetLayout.Builder()
            .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK11.VK_SHADER_STAGE_VERTEX_BIT)
            .build(device);
    }

    @Override
    protected DescriptorPool createDescriptorPool(VulkanDevice device, int poolSize) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, poolSize)
            .setMaxSets(poolSize)
            .build(device);
    }

    @Override
    protected GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule(device, "shaders/swapchain.vert", ShaderStageKind.VERTEX_SHADER))
            .addShaderModule(new ShaderModule(device, "shaders/swapchain.frag", ShaderStageKind.FRAGMENT_SHADER))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 24, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32B32_SFLOAT, 12)))
            .addDynamicState(VK11.VK_DYNAMIC_STATE_VIEWPORT)
            .addDynamicState(VK11.VK_DYNAMIC_STATE_SCISSOR)
            .setViewportState(new ViewportState()
                .setViewportCount(1)
                .setScissorCount(1))
            .setRasterizationState(new RasterizationState()
                .setFrontFace(VK11.VK_FRONT_FACE_COUNTER_CLOCKWISE))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayout(descriptorSetLayout)
            .setRenderPass(renderPass)
            .build(device);
    }

    @Override
    public DescriptorSet allocate(VulkanDevice device) {
        DescriptorSet descriptorSet = new DescriptorSet(device, descriptorPool, descriptorSetLayout);
        if(descriptorSet.getHandle() == VK11.VK_NULL_HANDLE) {
            throw new AssertionError("Failed to create descriptor set");
        }

        return descriptorSet;
    }
}
