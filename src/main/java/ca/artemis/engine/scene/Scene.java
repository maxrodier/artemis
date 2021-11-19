package ca.artemis.engine.scene;

import ca.artemis.Configuration;

public class Scene {
    
    private SceneGraph sceneGraph;

    public Scene() {
        this.sceneGraph = new SceneGraph(0, 0, Configuration.windowWidth, Configuration.windowHeight);
    }

    public void destroy() {
        sceneGraph.destroy();
    }
}
