package ca.artemis.engine.vulkan.rendering;

import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.ResourceManager;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.pipeline.ColorBlendState;
import ca.artemis.engine.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.engine.vulkan.api.pipeline.RasterizationState;
import ca.artemis.engine.vulkan.api.pipeline.VertexInputState;
import ca.artemis.engine.vulkan.api.pipeline.ViewportState;

public class DefaultShaderProgram extends ShaderProgram {
    
    public DefaultShaderProgram(VulkanDevice device, Renderer renderer) {
        super(device, renderer);
    }

    @Override
    protected DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device) {
        return new DescriptorSetLayout[] { 
            new DescriptorSetLayout.Builder()
                .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK11.VK_SHADER_STAGE_VERTEX_BIT)
                .addDescriptorSetLayoutBinding(1, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK11.VK_SHADER_STAGE_FRAGMENT_BIT)
                .build(device)
        };
    }

    @Override
    protected DescriptorPool createDescriptorPool(VulkanDevice device) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, Presenter.MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, Presenter.MAX_FRAMES_IN_FLIGHT)
            .setMaxSets(Presenter.MAX_FRAMES_IN_FLIGHT)
            .build(device);
    }

    @Override
    protected GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(ResourceManager.getShaderModule("defaultVertShaderModule"))
            .addShaderModule(ResourceManager.getShaderModule("defaultFragShaderModule"))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 36, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32B32A32_SFLOAT, 12)
                    .addAttributes(2, VK11.VK_FORMAT_R32G32_SFLOAT, 28)))
            .addDynamicState(VK11.VK_DYNAMIC_STATE_VIEWPORT)
            .addDynamicState(VK11.VK_DYNAMIC_STATE_SCISSOR)
            .setViewportState(new ViewportState()
                .setViewportCount(1)
                .setScissorCount(1))
            .setRasterizationState(new RasterizationState()
                .setFrontFace(VK11.VK_FRONT_FACE_COUNTER_CLOCKWISE))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(descriptorSetLayouts)
            .setRenderPass(renderPass)
            .build(device);
    }
}
