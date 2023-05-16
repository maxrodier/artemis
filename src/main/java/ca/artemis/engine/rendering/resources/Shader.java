package ca.artemis.engine.rendering.resources;

import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;
import ca.artemis.engine.rendering.programs.ShaderProgram;

public class Shader extends Resource {
    
    public ShaderProgram shaderProgram;

    public Shader(VulkanRenderingSystem renderingSystem, String name) {
        
    }

    @Override
    public void destroy(VulkanRenderingSystem renderingSystem) {
        // TODO Auto-generated method stub
        
    }
}
