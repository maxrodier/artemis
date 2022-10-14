package ca.artemis.game;

import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.engine.text.Font;
import ca.artemis.engine.text.FontLoader;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;
import ca.artemis.vulkan.rendering.scene.elements.TextElement;

public class TestGame {
    
    private SceneGraph sceneGraph;

    private Font font;

    private TextElement textElement1;
    private TextElement textElement2;
    private TextElement textElement3;
    private TextElement textElement4;

    public TestGame(VulkanContext context, RenderingEngine renderingEngine) {
        init(context, renderingEngine);
    }

    public void init(VulkanContext context, RenderingEngine renderingEngine) {
        this.sceneGraph = new SceneGraph(context);

        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.font = FontLoader.load(context, "montserrat");
        this.textElement1 = new TextElement(context, sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 12, font);
        this.textElement1.getTransform().setPosition(this.textElement1.getTransform().getPosition().add(new Vector3f(0.0f, 50.0f, 0.0f)));
        this.textElement2 = new TextElement(context, sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 20, font);
        this.textElement2.getTransform().setPosition(this.textElement2.getTransform().getPosition().add(new Vector3f(0.0f, 50.0f, 0.0f)));
        this.textElement3 = new TextElement(context, sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 28, font);
        this.textElement3.getTransform().setPosition(this.textElement3.getTransform().getPosition().add(new Vector3f(0.0f, 50.0f, 0.0f)));
        this.textElement4 = new TextElement(context, sceneRenderer, "Lorem ipsum dolor sit amet, consectetur adipiscing elit", 36, font);
        this.textElement4.getTransform().setPosition(this.textElement4.getTransform().getPosition().add(new Vector3f(0.0f, 50.0f, 0.0f)));

        textElement3.add(textElement4);
        textElement2.add(textElement3);
        textElement1.add(textElement2);
        sceneGraph.add(textElement1);

        renderingEngine.setSceneGraph(this.sceneGraph);
    }

    public void destroy(VulkanContext context, RenderingEngine renderingEngine) {
        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.textElement1.destroy(context, sceneRenderer);
        this.textElement2.destroy(context, sceneRenderer);
        this.textElement3.destroy(context, sceneRenderer);
        this.textElement4.destroy(context, sceneRenderer);
        this.font.destroy(context);
    }
}
