package ca.artemis.engine.rendering.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.artemis.engine.api.vulkan.descriptor.DescriptorSet;
import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;
import ca.artemis.engine.rendering.programs.ShaderProgram;

public class Material extends Resource {

    private Map<String, Texture> textures = new HashMap<>();
    private ShaderProgram shaderProgram;

    public Material(VulkanRenderingSystem renderingSystem, String name) {
        this.shaderProgram = renderingSystem.getShaderProgram(name);        
    }

    @Override
    public void destroy(VulkanRenderingSystem renderingSystem) {
        // TODO Auto-generated method stub
        
    }
}
