package ca.artemis.engine.text;

import java.util.HashMap;
import java.util.Map;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.memory.VulkanTexture;

public class Font {
    
    public VulkanTexture texture;
    public Map<Integer, Character> characters = new HashMap<>();

    public Font(VulkanTexture texture) {
        this.texture = texture;
    }

    public void destroy(VulkanContext context) {
        texture.destroy(context);
    }

    public VulkanTexture getTexture() {
        return texture;
    }
}
