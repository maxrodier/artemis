package ca.artemis.vulkan.api.memory;

import ca.artemis.math.Vector2f;

public class SpriteSheet {
    
    public int spriteWidth;
    public int spriteHeight;
    public VulkanTexture texture;

    public SpriteSheet(int spriteWidth, int spriteHeight, String filePath) {
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.texture = new VulkanTexture(filePath, false);
    }

    public void destroy() {
        this.texture.destroy();
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
