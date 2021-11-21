package ca.artemis.game;

import ca.artemis.Configuration;
import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.engine.scene.base.FontNode;
import ca.artemis.engine.scene.base.SimpleNode;
import ca.artemis.engine.text.Font;
import ca.artemis.engine.text.FontLoader;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class TestGame {
    
    private SceneGraph sceneGraph;

    private Font font;

    private SimpleNode simpleNode1;
    private SimpleNode simpleNode2;
    private SimpleNode simpleNode3;
    private SimpleNode simpleNode4;

    private FontNode fontNode1;
    private FontNode fontNode2;
    private FontNode fontNode3;
    private FontNode fontNode4;

    public TestGame(RenderingEngine renderingEngine) {
        init(renderingEngine);
    }

    public void init(RenderingEngine renderingEngine) {
        this.sceneGraph = new SceneGraph(0, 0, Configuration.windowWidth, Configuration.windowHeight);

        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();
        
        this.simpleNode1 = new SimpleNode(sceneRenderer, Configuration.windowWidth, 50, new Vector3f(1.0f, 0.0f, 1.0f));
        this.simpleNode2 = new SimpleNode(sceneRenderer, Configuration.windowWidth, 50, new Vector3f(0.0f, 1.0f, 1.0f));
        this.simpleNode3 = new SimpleNode(sceneRenderer, Configuration.windowWidth, 50, new Vector3f(1.0f, 1.0f, 0.0f));
        this.simpleNode4 = new SimpleNode(sceneRenderer, Configuration.windowWidth, 50, new Vector3f(1.0f, 0.0f, 0.0f));

        this.simpleNode2.getTransform().addPosition(new Vector3f(0.0f, 50.0f, 0.0f));
        this.simpleNode3.getTransform().addPosition(new Vector3f(0.0f, 50.0f, 0.0f));
        this.simpleNode4.getTransform().addPosition(new Vector3f(0.0f, 50.0f, 0.0f));

        this.font = FontLoader.load("montserrat_bold");
        this.fontNode1 = new FontNode(sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 16, new Vector3f(0.0f, 0.0f, 0.0f), font);
        this.fontNode2 = new FontNode(sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 24, new Vector3f(0.0f, 0.0f, 0.0f), font);
        this.fontNode3 = new FontNode(sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 32, new Vector3f(0.0f, 0.0f, 0.0f), font);
        this.fontNode4 = new FontNode(sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 40, new Vector3f(0.0f, 0.0f, 0.0f), font);

        simpleNode1.add(fontNode1);
        simpleNode2.add(fontNode2);
        simpleNode3.add(fontNode3);
        simpleNode4.add(fontNode4);

        simpleNode3.add(simpleNode4);
        simpleNode2.add(simpleNode3);
        simpleNode1.add(simpleNode2);
        sceneGraph.add(simpleNode1);

        renderingEngine.setSceneGraph(this.sceneGraph);
    }

    public void destroy(RenderingEngine renderingEngine) {
        sceneGraph.destroy(renderingEngine.getSceneRenderer().getCommandPool());
        this.font.destroy();
    }
}
