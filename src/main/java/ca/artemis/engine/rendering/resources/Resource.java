package ca.artemis.engine.rendering.resources;

import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;

public abstract class Resource {
    
    public abstract void destroy(VulkanRenderingSystem renderingSystem);
}
