package ca.artemis.engine.api.vulkan.memory;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanMemoryAllocator;

public class VulkanImageBundle {
    
    private final VulkanImage image;
    private final VulkanImageView imageView;

    public VulkanImageBundle(VulkanImage image, VulkanImageView imageView) {
        this.image = image;
        this.imageView = imageView;
    }

    public void destroy(VulkanDevice device, VulkanMemoryAllocator memoryAllocator) {
        this.image.destroy(memoryAllocator);
        this.imageView.destroy(device);
    }

    public VulkanImage getImage() {
        return image;
    }

    public VulkanImageView getImageView() {
        return imageView;
    }
}