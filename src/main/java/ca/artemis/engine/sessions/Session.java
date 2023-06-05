package ca.artemis.engine.sessions;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.scenes.Scene;

public class Session implements AutoCloseable {
    
    private Scene activeScene = null;

    public Session(Scene activeScene) {
        this.activeScene = activeScene;
    }

    @Override
    public void close() throws Exception {
        if(activeScene != null) {
            activeScene.close();
        }
    }

    public void init() {
        activeScene.init();
    }

    public void update(MemoryStack stack, float delta) {
        activeScene.update(stack, delta);
    }
}
