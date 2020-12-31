package ca.artemis;

import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.rendering.OldRenderingEngine;

public class Main {
    
    public static void main(String[] args) {
        VulkanContext context = VulkanContext.create();
        OldRenderingEngine renderingEngine = new OldRenderingEngine(context);
        renderingEngine.mainLoop();
        renderingEngine.destroy();
        context.destroy();
    }
}