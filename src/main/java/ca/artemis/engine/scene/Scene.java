package ca.artemis.engine.scene;

public class Scene {
    
    private SceneGraph sceneGraph;

    public Scene() {
        this.sceneGraph = new SceneGraph(null);
    }

    public void destroy() {
        sceneGraph.destroy();
    }
}
