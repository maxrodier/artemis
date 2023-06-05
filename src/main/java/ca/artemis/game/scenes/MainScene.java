package ca.artemis.game.scenes;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.rendering.entity.EntityRenderData;
import ca.artemis.engine.rendering.swapchain.SwapchainRenderData;
import ca.artemis.engine.rendering.ui.UiRenderData;
import ca.artemis.engine.scenes.Scene;
import ca.artemis.game.rendering.LowPolyRenderingEngine;

public class MainScene extends Scene {

    private final LowPolyRenderingEngine lowPolyRenderingEngine;

    private final EntityRenderData entityRenderData;
    private final UiRenderData uiRenderData;
    private final SwapchainRenderData swapchainRenderData;

    public MainScene() {
        this.lowPolyRenderingEngine = LowPolyRenderingEngine.instance();

        this.entityRenderData = new EntityRenderData();
        this.uiRenderData = new UiRenderData();
        this.swapchainRenderData = new SwapchainRenderData();
    }

    public void init() {
        lowPolyRenderingEngine.getEntityRenderer().setRenderData(entityRenderData);
        lowPolyRenderingEngine.getUiRenderer().setRenderData(uiRenderData);
        lowPolyRenderingEngine.getSwapchainRenderer().setRenderData(swapchainRenderData);
    }

    @Override
    public void update(MemoryStack stack, float delta) {
        entityRenderData.update(stack);
        uiRenderData.update(stack);
        swapchainRenderData.update(stack);
    }

    @Override
    public void close() throws Exception {
        swapchainRenderData.close();
        entityRenderData.close();
    }
}
