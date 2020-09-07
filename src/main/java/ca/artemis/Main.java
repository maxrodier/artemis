package ca.artemis;

import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.rendering.RenderingEngine;

public class Main {
    
    public static void main(String[] args) {
        VulkanContext context = VulkanContext.create();
        RenderingEngine renderingEngine = new RenderingEngine(context);
        renderingEngine.mainLoop();
        renderingEngine.destroy();
        context.destroy();
    }
}