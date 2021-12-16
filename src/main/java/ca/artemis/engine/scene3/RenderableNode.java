package ca.artemis.engine.scene3;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.rendering.programs.ShaderProgram;

@JsonDeserialize(builder = RenderableNode.Builder.class)
public abstract class RenderableNode extends Node {
    
    protected final ShaderProgram shaderProgram;
    protected final DescriptorSet[] descriptorSets;
    protected final SecondaryCommandBuffer drawCommandBuffer;

    protected RenderableNode(Builder<?> builder) {
        super(builder);
        this.shaderProgram = builder.shaderProgram;
        this.descriptorSets = this.shaderProgram.allocate();
        this.drawCommandBuffer = new SecondaryCommandBuffer(builder.commandPool);
    }

    public void destroy() {
        super.destroy();
        this.drawCommandBuffer.destroy();
    }

    public abstract void updateDescriptorSets();

    public abstract static class Builder<T extends Builder<T>> extends Node.Builder<Builder<?>> {

        private ShaderProgram shaderProgram;
        private CommandPool commandPool;
    }
}
