package ca.artemis.vulkan.api.memory;

public class VulkanImageBundle {
    
    private final VulkanImage image;
    private final VulkanImageView imageView;

    public VulkanImageBundle(VulkanImage image, VulkanImageView imageView) {
        this.image = image;
        this.imageView = imageView;
    }

    public void destroy() {
        this.image.destroy();
        this.imageView.destroy();
    }

    public VulkanImage getImage() {
        return image;
    }

    public VulkanImageView getImageView() {
        return imageView;
    }
}