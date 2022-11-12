package ca.artemis.vulkan.rendering.programs;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;

public abstract class ShaderProgram {
    
    protected static final int MAXDESCRIPTORPOOLSIZE = 64;

    protected final DescriptorSetLayout descriptorSetLayout;
    protected final DescriptorPool descriptorPool;
    protected GraphicsPipeline graphicsPipeline;

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass) {
        this(device, renderPass, MAXDESCRIPTORPOOLSIZE);
    }

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass, int poolSize) {
        this.descriptorSetLayout = createDescriptorSetLayout(device);
        this.descriptorPool = createDescriptorPool(device, poolSize);
        this.graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public void destroy(VulkanDevice device) {
        graphicsPipeline.destroy(device);
        descriptorPool.destroy(device);
        descriptorSetLayout.destroy(device);
    }

    protected abstract DescriptorSetLayout createDescriptorSetLayout(VulkanDevice device);
    protected abstract DescriptorPool createDescriptorPool(VulkanDevice device, int size);
    protected abstract GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass);

    public abstract DescriptorSet allocate(VulkanDevice device);

    public void regenerateGraphicsPipeline(VulkanDevice device, RenderPass renderPass) { //TODO: Externalize pipeline layout It does not need to be recreated each time
        graphicsPipeline.destroy(device);
        graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}
