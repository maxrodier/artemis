package ca.artemis.engine.core;

public abstract class Component {
    
    protected GameObject parent = null;

    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    public void start() {
        
    }

    public abstract void update(float dt);
}
