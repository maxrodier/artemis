package ca.artemis.engine.rendering;

import org.lwjgl.system.MemoryStack;

public abstract class RenderData implements AutoCloseable {
    
    private int frameIndex;

    public abstract void update(MemoryStack stack);

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }
}
