package ca.artemis.engine.rendering.swapchain;

import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.framebuffer.SurfaceSupportDetails;
import ca.artemis.engine.vulkan.api.pipeline.ColorBlendState;
import ca.artemis.engine.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.engine.vulkan.api.pipeline.RasterizationState;
import ca.artemis.engine.vulkan.api.pipeline.ShaderModule;
import ca.artemis.engine.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.engine.vulkan.api.pipeline.VertexInputState;
import ca.artemis.engine.vulkan.api.pipeline.ViewportState;
import ca.artemis.engine.vulkan.core.ShaderProgram;

public class SwapchainShaderProgram extends ShaderProgram {

    protected SwapchainShaderProgram(VulkanDevice device, RenderPass renderPass) {
        super(device, renderPass);
    }

    @Override
    protected DescriptorPool createDescriptorPool(VulkanDevice device) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, RenderingEngine.MAX_FRAMES_IN_FLIGHT)
            .setMaxSets(RenderingEngine.MAX_FRAMES_IN_FLIGHT)
            .build(device);
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
    protected GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        SurfaceSupportDetails surfaceSupportDetails = LowPolyEngine.instance().getContext().getSurfaceSupportDetails();

        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule(device, "shaders/swapchain.vert", ShaderStageKind.VERTEX_SHADER))
            .addShaderModule(new ShaderModule(device, "shaders/swapchain.frag", ShaderStageKind.FRAGMENT_SHADER))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 20, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32_SFLOAT, 12)))
            .setViewportState(new ViewportState()
                .addViewport(new ViewportState.Viewport(0, 0, surfaceSupportDetails.getSurfaceExtent().width(), surfaceSupportDetails.getSurfaceExtent().height(), 0.0f, 1.0f))
                .addScissors(new ViewportState.Scissor(0, 0, surfaceSupportDetails.getSurfaceExtent().width(), surfaceSupportDetails.getSurfaceExtent().height())))
            .setRasterizationState(new RasterizationState()
                .setFrontFace(VK11.VK_FRONT_FACE_CLOCKWISE))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(descriptorSetLayouts)
            .setRenderPass(renderPass)
            .build(device);
    }
}