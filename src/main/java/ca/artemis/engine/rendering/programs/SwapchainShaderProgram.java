package ca.artemis.engine.rendering.programs;

import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.descriptor.DescriptorPool;
import ca.artemis.engine.api.vulkan.descriptor.DescriptorSet;
import ca.artemis.engine.api.vulkan.descriptor.DescriptorSetLayout;
import ca.artemis.engine.api.vulkan.framebuffer.RenderPass;
import ca.artemis.engine.api.vulkan.pipeline.ColorBlendState;
import ca.artemis.engine.api.vulkan.pipeline.GraphicsPipeline;
import ca.artemis.engine.api.vulkan.pipeline.ShaderModule;
import ca.artemis.engine.core.EngineSettings;
import ca.artemis.engine.util.SharderUtils.ShaderStageKind;
import ca.artemis.engine.api.vulkan.pipeline.VertexInputState;
import ca.artemis.engine.api.vulkan.pipeline.ViewportState;

public class SwapchainShaderProgram extends ShaderProgram {
    public SwapchainShaderProgram(VulkanDevice device, RenderPass renderPass, int size) {
        super(device, renderPass, size);
    }

    @Override
    protected DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device) {
        return new DescriptorSetLayout[] { 
            new DescriptorSetLayout.Builder()
                .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK11.VK_SHADER_STAGE_FRAGMENT_BIT)
                .build(device)
        };
    }

    @Override
    protected DescriptorPool createDescriptorPool(VulkanDevice device, int size) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, size)
            .setMaxSets(size)
            .build(device);
    }

    @Override
    protected GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule(device, "src/main/resources/shaders/swapchain.vert", ShaderStageKind.VERTEX_SHADER))
            .addShaderModule(new ShaderModule(device, "src/main/resources/shaders/swapchain.frag", ShaderStageKind.FRAGMENT_SHADER))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 20, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32_SFLOAT, 12)))
            .setViewportState(new ViewportState()
                .addViewport(new ViewportState.Viewport(0, 0, EngineSettings.WIDTH, EngineSettings.HEIGHT, 0.0f, 1.0f))
                .addScissors(new ViewportState.Scissor(0, 0, EngineSettings.WIDTH, EngineSettings.HEIGHT)))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(descriptorSetLayouts)
            .setRenderPass(renderPass)
            .build(device);
    }

    @Override
    public DescriptorSet[] allocate(VulkanDevice device) {
            DescriptorSet descriptorSet = new DescriptorSet(device, descriptorPools.stream().findFirst().get(), descriptorSetLayouts[0]);
            if(descriptorSet.getHandle() == VK11.VK_NULL_HANDLE) {
                throw new AssertionError("Failed to create descriptor set");
            }

            return new DescriptorSet[] { descriptorSet };
    }
}