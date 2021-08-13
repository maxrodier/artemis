package ca.artemis.vulkan.rendering.scene;

import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.rendering.programs.ShaderProgram;

public abstract class RenderableNode extends Node {
    
    protected final ShaderProgram shaderProgram;
    protected final DescriptorSet[] descriptorSets;
    protected final SecondaryCommandBuffer drawCommandBuffer;

    protected RenderableNode(VulkanDevice device, ShaderProgram shaderProgram, CommandPool commandPool) {
        this.shaderProgram = shaderProgram;
        this.descriptorSets = this.shaderProgram.allocate(device);
        this.drawCommandBuffer = new SecondaryCommandBuffer(device, commandPool);
    }

    protected void destroy(VulkanDevice device, CommandPool commandPool) {
        this.drawCommandBuffer.destroy(device, commandPool);
    }

    public abstract void updateDescriptorSets(VulkanDevice device);
}
