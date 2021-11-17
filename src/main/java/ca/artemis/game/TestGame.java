package ca.artemis.game;

import ca.artemis.Configuration;
import ca.artemis.engine.text.Font;
import ca.artemis.engine.text.FontLoader;
import ca.artemis.engine.text.TextElement;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;
import ca.artemis.vulkan.rendering.scene.SceneGraph;

public class TestGame {
    
    private SceneGraph sceneGraph;

    private Font font;

    private TextElement textElement;

    public TestGame(VulkanContext context, RenderingEngine renderingEngine) {
        init(context, renderingEngine);
    }

    public void init(VulkanContext context, RenderingEngine renderingEngine) {
        this.sceneGraph = new SceneGraph(0, 0, Configuration.windowWidth, Configuration.windowHeight);

        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.font = FontLoader.load(context, "montserrat_bold_italic");
        this.textElement = new TextElement(context, sceneRenderer, "Allo, comment ca va? Oui je vais bien et toi?", 16, font);

        sceneGraph.add(textElement);

        renderingEngine.setSceneGraph(this.sceneGraph);
    }

    public void destroy(VulkanContext context, RenderingEngine renderingEngine) {
        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.textElement.destroy(context, sceneRenderer);
        this.font.destroy(context);
    }

    //Background Image
    //Button 1
    //Button 2
    //Button 3
}
