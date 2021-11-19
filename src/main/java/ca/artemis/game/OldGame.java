package ca.artemis.game;

public class OldGame {
    //Old Game is here to show old code


    /*
    private SceneGraph sceneGraph;

    private Sprite sprite;
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

        this.sprite = new Sprite(context, 512, 512, "src/main/resources/textures/wood.png");
        this.spriteSheet = new SpriteSheet(context, 32, 32, "src/main/resources/textures/terrain.png");

        System.out.println(spriteSheet.getNormalizedSpriteWidth());
        System.out.println(spriteSheet.getNormalizedSpriteHeight());

        this.uiElement = new UIElement(context, sceneRenderer, 0, 0, 200, 200);
        this.spriteElement = new SpriteElement(context, sceneRenderer, 0, 0, 200, 200, this.sprite);
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
            this.sprite.destroy(context);
    }
    */
}
