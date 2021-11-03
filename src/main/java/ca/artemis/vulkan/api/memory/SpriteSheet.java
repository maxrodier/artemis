package ca.artemis.vulkan.api.memory;

import ca.artemis.math.Vector2f;
import ca.artemis.vulkan.api.context.VulkanContext;

public class SpriteSheet {
    
    public int spriteWidth;
    public int spriteHeight;
    public VulkanTexture texture;

    public SpriteSheet(VulkanContext context, int spriteWidth, int spriteHeight, String filePath) {
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.texture = new VulkanTexture(context, filePath, false);
    }

    public void destroy(VulkanContext context) {
        this.texture.destroy(context);
    }

    public float getNormalizedSpriteWidth() {
        return (float) spriteWidth / texture.getImageBundle().getImage().getWidth();
    }

    public float getNormalizedSpriteHeight() {
        return (float) spriteHeight / texture.getImageBundle().getImage().getHeight();
    }

    public Vector2f getU() {
        float width = texture.getImageBundle().getImage().getWidth();
        float u0 = 0.5f / width;
        float u1 = (spriteWidth - 0.5f) / width;

        return new Vector2f(u0, u1);
    }

    public Vector2f getV() {
        float height = texture.getImageBundle().getImage().getHeight();
        float v0 = 0.5f / height;
        float v1 = (spriteHeight - 0.5f) / height;

        return new Vector2f(v0, v1);
    }

    public VulkanTexture getTexture() {
        return texture;
    }
}
