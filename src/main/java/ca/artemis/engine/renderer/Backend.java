package ca.artemis.engine.renderer;

public abstract class Backend {
    
    public abstract boolean initialize();
    public abstract void destroy();
    public abstract void onResized();

    public abstract boolean beginFrame();
    public abstract boolean endFrame();
}
