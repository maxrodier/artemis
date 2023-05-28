package ca.artemis.engine.vulkan.core;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.pipeline.GraphicsPipeline;

public abstract class ShaderProgram implements AutoCloseable {
    
    protected DescriptorPool descriptorPool;
    protected DescriptorSetLayout[] descriptorSetLayouts;
    protected GraphicsPipeline graphicsPipeline;

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass) {
        this.descriptorPool = createDescriptorPool(device);
        this.descriptorSetLayouts = createDescriptorSetLayouts(device);
        this.graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    @Override
    public void close() throws Exception {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        graphicsPipeline.destroy(device);
        for(DescriptorSetLayout descriptorSetLayout : descriptorSetLayouts) {
            descriptorSetLayout.destroy(device);
        }
    }

    protected abstract DescriptorPool createDescriptorPool(VulkanDevice device);
    protected abstract DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device);
    protected abstract GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass);

    public void regenerateGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        graphicsPipeline.destroy(device);
        this.graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public DescriptorPool getDescriptorPool() {
        return descriptorPool;
    }

    public DescriptorSetLayout[] getDescriptorSetLayouts() {
        return descriptorSetLayouts;
    }

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}