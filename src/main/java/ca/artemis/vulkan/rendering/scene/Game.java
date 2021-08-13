package ca.artemis.vulkan.rendering.scene;

import ca.artemis.Configuration;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.memory.SpriteSheet;
import ca.artemis.vulkan.api.memory.VulkanTexture;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;

public class Game {
    
    private SceneGraph sceneGraph;

    private VulkanTexture texture;
    private SpriteSheet spriteSheet;

    private UIElement uiElement;
    private SpriteElement spriteElement;
    private SpriteSheetElement spriteSheetElement;

    public Game(VulkanContext context, RenderingEngine renderingEngine) {
        init(context, renderingEngine);
    }

    public void init(VulkanContext context, RenderingEngine renderingEngine) {
        this.sceneGraph = new SceneGraph(0, 0, Configuration.windowWidth, Configuration.windowHeight);

        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.texture = new VulkanTexture(context, "src/main/resources/textures/wood.png");
        this.spriteSheet = new SpriteSheet(context, 32, 32, "src/main/resources/textures/terrain.png");

        System.out.println(spriteSheet.getNormalizedSpriteWidth());
        System.out.println(spriteSheet.getNormalizedSpriteHeight());

        this.uiElement = new UIElement(context, sceneRenderer, 0, 0, 200, 200);
        this.spriteElement = new SpriteElement(context, sceneRenderer, 0, 0, 200, 200, this.texture);
        this.spriteSheetElement = new SpriteSheetElement(context, sceneRenderer, 0, 0, 200, 200, this.spriteSheet);

        this.spriteSheetElement.setSpriteIndex(33,10);

        uiElement.getTransform().setPosition(uiElement.getTransform().getPosition().add(new Vector3f(100, 0, 0)));
        spriteElement.getTransform().setPosition(spriteElement.getTransform().getPosition().add(new Vector3f(200, 200, 0)));
        spriteSheetElement.getTransform().setPosition(spriteSheetElement.getTransform().getPosition().add(new Vector3f(200, 200, 0)));


        spriteElement.add(spriteSheetElement);
        uiElement.add(spriteElement);
        sceneGraph.add(uiElement);

        renderingEngine.setSceneGraph(this.sceneGraph);
    }

    public void destroy(VulkanContext context, RenderingEngine renderingEngine) {
        SceneRenderer sceneRenderer = renderingEngine.getSceneRenderer();

        this.spriteSheetElement.destroy(context, sceneRenderer);
        this.spriteElement.destroy(context, sceneRenderer);
        this.uiElement.destroy(context, sceneRenderer);

        this.spriteSheet.destroy(context);
        this.texture.destroy(context);
    }
}
