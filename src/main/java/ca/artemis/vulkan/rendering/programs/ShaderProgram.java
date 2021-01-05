package ca.artemis.vulkan.rendering.programs;

import java.util.ArrayList;
import java.util.List;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;

public abstract class ShaderProgram {

    protected static final int MAXDESCRIPTORPOOLSIZE = 64;

    protected final DescriptorSetLayout[] descriptorSetLayouts;
    protected final List<DescriptorPool> descriptorPools;
    protected final GraphicsPipeline graphicsPipeline;

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass) {
        this(device, renderPass, MAXDESCRIPTORPOOLSIZE);
    }

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass, int size) {
        this.descriptorSetLayouts = createDescriptorSetLayouts(device);
        this.descriptorPools = new ArrayList<>();
        this.descriptorPools.add(createDescriptorPool(device, size));
        this.graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public void destroy(VulkanDevice device) {
        graphicsPipeline.destroy(device);
        for(DescriptorPool descriptorPool : descriptorPools) {
            descriptorPool.destroy(device);
        }
        for(DescriptorSetLayout descriptorSetLayout : descriptorSetLayouts) {
            descriptorSetLayout.destroy(device);
        }
    }

    protected abstract DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device);
    protected abstract DescriptorPool createDescriptorPool(VulkanDevice device, int size);
    protected abstract GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass);

    public abstract DescriptorSet[] allocate(VulkanDevice device);

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}
