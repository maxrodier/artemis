package ca.artemis.engine.scenes;

import java.io.Closeable;

public abstract class Component implements Closeable {
    
    public GameObject parent;

    @Override
    public void close() {
        //The close method does nothing by default;
    }

    public void init() {
        //The init method does nothing by default;
    }

    public GameObject getParent() {
        return parent;
    }

    public void setParent(GameObject parent) {
        this.parent = parent;
    }
}
