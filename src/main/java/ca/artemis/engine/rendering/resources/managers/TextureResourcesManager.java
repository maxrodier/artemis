package ca.artemis.engine.rendering.resources.managers;

import java.util.HashMap;

import ca.artemis.engine.rendering.resources.Texture;

public class TextureResourcesManager extends ResourcesManager<Texture> {
    
    public static TextureResourcesManager instance = new TextureResourcesManager();

    public TextureResourcesManager() {
        super();
    }
    
    public void addTexture(String name, Texture texture) {
        addResource(name, texture);
    }
    
    public Texture getTexture(String name) {
        return getResource(name);
    }

    public HashMap<String, Texture> getTextures() {
        return getResources();
    }
}
