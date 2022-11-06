package ca.artemis.engine.core;

import java.util.HashMap;

import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.pipeline.ShaderModule;
import ca.artemis.engine.vulkan.programs.ShaderProgram;

public class ResourceManager {
    
    private static final HashMap<String, ShaderModule> shaderModules = new HashMap<>();
    private static final HashMap<String, ShaderProgram> shaderPrograms = new HashMap<>();

    public static void cleanup() {
        VulkanContext context = VulkanContext.getContext();
        for(ShaderModule shaderModule: ResourceManager.shaderModules.values()) {
            shaderModule.destroy(context.getDevice());
        }
        for(ShaderProgram shaderProgram: ResourceManager.shaderPrograms.values()) {
            shaderProgram.destroy(context.getDevice());
        }
        ResourceManager.shaderModules.clear();
        ResourceManager.shaderPrograms.clear();
    }

    public static void addShaderModule(String key, ShaderModule shaderModule) {
        if(shaderModules.containsKey(key)) {
            throw new AssertionError("ResourceManager already contains a ShaderModule with this key!");
        }
        ResourceManager.shaderModules.put(key, shaderModule);
    }

    public static void addShaderProgram(String key, ShaderProgram shaderModule) {
        if(shaderPrograms.containsKey(key)) {
            throw new AssertionError("ResourceManager already contains a ShaderProgram with this key!");
        }
        ResourceManager.shaderPrograms.put(key, shaderModule);
    }

    public static ShaderModule getShaderModule(String key) {
        return ResourceManager.shaderModules.getOrDefault(key, null);
    }


    public static ShaderProgram getShaderProgram(String key) {
        return ResourceManager.shaderPrograms.getOrDefault(key, null);
    }
}
