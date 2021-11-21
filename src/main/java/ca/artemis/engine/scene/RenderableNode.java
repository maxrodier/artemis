package ca.artemis.engine.scene;

import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.rendering.programs.ShaderProgram;

public abstract class RenderableNode extends Node {
    
    protected final ShaderProgram shaderProgram;
    protected final DescriptorSet[] descriptorSets;
    protected final SecondaryCommandBuffer drawCommandBuffer;

    protected RenderableNode(ShaderProgram shaderProgram, CommandPool commandPool) {
        this.shaderProgram = shaderProgram;
        this.descriptorSets = this.shaderProgram.allocate();
        this.drawCommandBuffer = new SecondaryCommandBuffer(commandPool);
    }

    public void destroy(CommandPool commandPool) {
        super.destroy(commandPool);
        this.drawCommandBuffer.destroy(commandPool);
    }

    public abstract void updateDescriptorSets();
}
