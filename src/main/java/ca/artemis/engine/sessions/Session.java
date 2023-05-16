package ca.artemis.engine.sessions;

import ca.artemis.engine.scenes.Scene;

public class Session implements AutoCloseable {
    
    private Scene activeScene = null;

    public Session(Scene activeScene) {
        this.activeScene = activeScene;
    }

    @Override
    public void close() throws Exception {
        
    }

    public void update() {
        activeScene.update();
    }

    public Scene getActiveScene() {
        return activeScene;
    }
}
