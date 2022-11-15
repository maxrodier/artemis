package ca.artemis.engine.core;

import org.lwjgl.system.MemoryStack;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.FrameInfo;
import ca.artemis.vulkan.rendering.RenderingEngine;

public abstract class Game {
    
    public abstract void init(RenderingEngine renderingEngine);
    public abstract void update(MemoryStack stack, RenderingEngine renderingEngine, FrameInfo frameInfo);
    public abstract void destroy(VulkanContext context);
}
