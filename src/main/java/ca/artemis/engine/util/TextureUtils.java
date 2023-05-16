package ca.artemis.engine.util;

import java.io.IOException;

import ca.artemis.engine.api.vulkan.commands.CommandPool;
import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanMemoryAllocator;
import ca.artemis.engine.rendering.resources.Texture;

public class TextureUtils {
    
    public static Texture loadTexture(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, String textureName) {
        try {
            return new Texture(device, allocator, commandPool, FileUtils.getBufferedImage("textures/" + textureName + ".png"), false);
        } catch (IOException e) {
            throw new RuntimeException("Could not load texture " + textureName, e);
        }
    }
}
