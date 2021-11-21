package ca.artemis.engine.scene;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.commands.CommandPool;

public class Scene {
    
    private SceneGraph sceneGraph;

    public Scene() {
        this.sceneGraph = new SceneGraph(0, 0, Configuration.windowWidth, Configuration.windowHeight);
    }

    public void destroy(CommandPool commandPool) {
        sceneGraph.destroy(commandPool);
    }
}
