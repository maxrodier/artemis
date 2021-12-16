package ca.artemis.engine.scene3.components;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ca.artemis.engine.scene3.Node;
import ca.artemis.engine.scene3.View;
import ca.artemis.engine.scene3.annotations.YamlView;

@YamlView(path = "src/main/resources/game/block.yaml")
@JsonDeserialize(builder = Block.Builder.class)
public class Block extends View {
    
    private final BlockController blockController;

    protected Block(Builder<?> builder) {
        super(builder);
        this.blockController = new BlockController(this);
    }

    @Override
    public BlockController getController() {
        return blockController;
    }

    public static class Builder<T extends Builder<T>> extends Node.Builder<Builder<?>> {

        public Block build() {
            return new Block(this);
        }
    }
}