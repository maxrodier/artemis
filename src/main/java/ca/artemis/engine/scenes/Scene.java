package ca.artemis.engine.scenes;

import org.lwjgl.system.MemoryStack;

public abstract class Scene implements AutoCloseable {
    
    public abstract void init();
    public abstract void update(MemoryStack stack, float delta);
}
