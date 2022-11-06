package ca.artemis.engine.vulkan.programs;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorPool;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.pipeline.GraphicsPipeline;
import ca.artemis.engine.vulkan.rendering.Renderer;

public abstract class ShaderProgram {
    
    protected final Renderer renderer;
    protected final DescriptorSetLayout[] descriptorSetLayouts;
    protected final DescriptorPool descriptorPool;
    protected GraphicsPipeline graphicsPipeline;

    protected ShaderProgram(VulkanDevice device, Renderer renderer) {
        this.renderer = renderer;
        this.descriptorSetLayouts = createDescriptorSetLayouts(device);
        this.descriptorPool = createDescriptorPool(device);
        this.graphicsPipeline = createGraphicsPipeline(device, this.renderer.getRenderPass());
    }

    public void destroy(VulkanDevice device) {
        graphicsPipeline.destroy(device);
        for(DescriptorSetLayout descriptorSetLayout : descriptorSetLayouts) {
            descriptorSetLayout.destroy(device);
        }
    }

    protected abstract DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device);
    protected abstract DescriptorPool createDescriptorPool(VulkanDevice device);
    protected abstract GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass);

    public void regenerateGraphicsPipeline(VulkanDevice device) {
        graphicsPipeline.destroy(device);
        graphicsPipeline = createGraphicsPipeline(device, this.renderer.getRenderPass());
    }

    public DescriptorSetLayout[] getDescriptorSetLayouts() {
        return descriptorSetLayouts;
    }

    public DescriptorPool getDescriptorPool() {
        return descriptorPool;
    }

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}
