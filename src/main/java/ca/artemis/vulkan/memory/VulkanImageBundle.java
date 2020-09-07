package ca.artemis.vulkan.memory;

import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.context.VulkanMemoryAllocator;

public class VulkanImageBundle {
    
    private final VulkanImage image;
    private final VulkanImageView imageView;

    public VulkanImageBundle(VulkanImage image, VulkanImageView imageView) {
        this.image = image;
        this.imageView = imageView;
    }

    public void destroy(VulkanMemoryAllocator allocator, VulkanDevice device) {
        this.image.destroy(allocator);
        this.imageView.destroy(device);
    }

    public VulkanImage getImage() {
        return image;
    }

    public VulkanImageView getImageView() {
        return imageView;
    }
}