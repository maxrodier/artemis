package ca.artemis.game.main;

import java.io.IOException;

import ca.artemis.engine.utils.FileUtils;
import ca.artemis.game.TestGame;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.RenderingEngine;

public class Main {
    
    public static void main(String[] args) throws IOException {

        FileUtils.readString("shaders/simple.vert");
        FileUtils.readString("fonts/montserrat.json");

        VulkanContext context = VulkanContext.create();

        RenderingEngine renderingEngine = new RenderingEngine(context);
        TestGame game = new TestGame(context, renderingEngine);
        renderingEngine.mainLoop();
        game.destroy(context, renderingEngine);
        renderingEngine.destroy();
        
        context.destroy();
    }
}
