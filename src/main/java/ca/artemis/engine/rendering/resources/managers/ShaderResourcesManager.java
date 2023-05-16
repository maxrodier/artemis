package ca.artemis.engine.rendering.resources.managers;

import java.util.HashMap;

import ca.artemis.engine.rendering.resources.Shader;

public class ShaderResourcesManager extends ResourcesManager<Shader> {
    
    public static ShaderResourcesManager instance = new ShaderResourcesManager();

    public void addShader(String name, Shader shader) {
        addResource(name, shader);
    }

    public Shader getShader(String name) {
        return getResource(name);
    }

    public HashMap<String, Shader> getShaders() {
        return getResources();
    }
}
