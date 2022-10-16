package ca.artemis.vulkan.rendering.programs;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.pipeline.GraphicsPipeline;

public abstract class ShaderProgram {
    
    protected final DescriptorSetLayout[] descriptorSetLayouts;
    protected GraphicsPipeline graphicsPipeline;

    protected ShaderProgram(VulkanDevice device, RenderPass renderPass) {
        this.descriptorSetLayouts = createDescriptorSetLayouts(device);
        this.graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public void destroy(VulkanDevice device) {
        graphicsPipeline.destroy(device);
        for(DescriptorSetLayout descriptorSetLayout : descriptorSetLayouts) {
            descriptorSetLayout.destroy(device);
        }
    }

    protected abstract DescriptorSetLayout[] createDescriptorSetLayouts(VulkanDevice device);
    protected abstract GraphicsPipeline createGraphicsPipeline(VulkanDevice device, RenderPass renderPass);

    public void regenerateGraphicsPipeline(VulkanDevice device, RenderPass renderPass) {
        graphicsPipeline.destroy(device);
        graphicsPipeline = createGraphicsPipeline(device, renderPass);
    }

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}
