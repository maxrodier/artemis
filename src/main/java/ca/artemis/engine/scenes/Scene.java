package ca.artemis.engine.scenes;

public abstract class Scene implements AutoCloseable {
    
    public abstract void update();
    public abstract void render();

    @Override
    public void close() throws Exception {

    }
}
