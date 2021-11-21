package ca.artemis.vulkan.rendering.programs;

import java.util.ArrayList;
import java.util.List;

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

    protected ShaderProgram(RenderPass renderPass) {
        this(renderPass, MAXDESCRIPTORPOOLSIZE);
    }

    protected ShaderProgram(RenderPass renderPass, int size) {
        this.descriptorSetLayouts = createDescriptorSetLayouts();
        this.descriptorPools = new ArrayList<>();
        this.descriptorPools.add(createDescriptorPool(size));
        this.graphicsPipeline = createGraphicsPipeline(renderPass);
    }

    public void destroy() {
        graphicsPipeline.destroy();
        for(DescriptorPool descriptorPool : descriptorPools) {
            descriptorPool.destroy();
        }
        for(DescriptorSetLayout descriptorSetLayout : descriptorSetLayouts) {
            descriptorSetLayout.destroy();
        }
    }

    protected abstract DescriptorSetLayout[] createDescriptorSetLayouts();
    protected abstract DescriptorPool createDescriptorPool(int size);
    protected abstract GraphicsPipeline createGraphicsPipeline(RenderPass renderPass);

    public abstract DescriptorSet[] allocate();

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}
