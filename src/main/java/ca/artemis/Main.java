package ca.artemis;

import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.rendering.OldRenderingEngine;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.RenderingEngine;
import ca.artemis.vulkan.rendering.scene.Game;

public class Main {
    
    public static void main(String[] args) {
        VulkanContext context = VulkanContext.create();
        RenderingEngine renderingEngine = new RenderingEngine(context);
        Game game = new Game(context, renderingEngine);
        renderingEngine.mainLoop();
        game.destroy(context, renderingEngine);
        renderingEngine.destroy();
        context.destroy();
    }
}
