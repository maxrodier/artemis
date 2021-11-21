package ca.artemis.vulkan.rendering.programs;

import org.lwjgl.vulkan.VK11;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.pipeline.ColorBlendState;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.api.pipeline.ShaderModule;
import ca.artemis.vulkan.api.pipeline.SharderUtils.ShaderStageKind;
import ca.artemis.vulkan.api.pipeline.VertexInputState;
import ca.artemis.vulkan.api.pipeline.ViewportState;

public class FontShaderProgram extends ShaderProgram {

    public FontShaderProgram(RenderPass renderPass) {
        super(renderPass);
    }

    @Override
    protected DescriptorSetLayout[] createDescriptorSetLayouts() {
        return new DescriptorSetLayout[] { 
            new DescriptorSetLayout.Builder()
                .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1, VK11.VK_SHADER_STAGE_VERTEX_BIT)
                .addDescriptorSetLayoutBinding(1, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK11.VK_SHADER_STAGE_FRAGMENT_BIT)
                .build()
        };
    }

    @Override
    protected DescriptorPool createDescriptorPool(int size) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, size)
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, size)
            .setMaxSets(size)
            .build();
    }

    @Override
    protected GraphicsPipeline createGraphicsPipeline(RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule("src/main/resources/shaders/font.vert", ShaderStageKind.VERTEX_SHADER))
            .addShaderModule(new ShaderModule("src/main/resources/shaders/font.frag", ShaderStageKind.FRAGMENT_SHADER))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 32, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32B32_SFLOAT, 12)
                    .addAttributes(2, VK11.VK_FORMAT_R32G32_SFLOAT, 24)))
            .setViewportState(new ViewportState()
                .addViewport(new ViewportState.Viewport(0, 0, Configuration.windowWidth, Configuration.windowHeight, 0.0f, 1.0f))
                .addScissors(new ViewportState.Scissor(0, 0, Configuration.windowWidth, Configuration.windowHeight)))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(true, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(descriptorSetLayouts)
            .setRenderPass(renderPass)
            .build();
    }

    @Override
    public DescriptorSet[] allocate() {
        for(DescriptorPool descriptorPool : descriptorPools) {
            DescriptorSet descriptorSet = new DescriptorSet(descriptorPool, descriptorSetLayouts[0]);
            if(descriptorSet.getHandle() != VK11.VK_NULL_HANDLE) {
                return new DescriptorSet[] { descriptorSet };
            }
        }

        DescriptorPool descriptorPool = createDescriptorPool(MAXDESCRIPTORPOOLSIZE);
        descriptorPools.add(descriptorPool);
        DescriptorSet descriptorSet = new DescriptorSet(descriptorPool, descriptorSetLayouts[0]);
        if(descriptorSet.getHandle() == VK11.VK_NULL_HANDLE) {
            throw new AssertionError("Failed to create descriptor set");
        }
        return new DescriptorSet[] { descriptorSet };
    }
}
