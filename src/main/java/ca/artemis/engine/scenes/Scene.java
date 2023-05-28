package ca.artemis.engine.scenes;

public abstract class Scene implements AutoCloseable {
    
    public abstract void init();
    public abstract void update();
    public abstract void render();
}
