package ca.artemis.engine.vulkan.api.framebuffer;

import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.engine.vulkan.api.memory.VulkanImage;
import ca.artemis.engine.vulkan.api.memory.VulkanImageView;

public class FramebufferObject {
    
    protected final VulkanImage[] images;
    protected final VulkanImageView[] imageViews;
    protected final VulkanFramebuffer[] framebuffers;

    protected FramebufferObject(VulkanImage[] images, VulkanImageView[] imageViews, VulkanFramebuffer[] framebuffers) {
        this.images = images;
        this.imageViews = imageViews;
        this.framebuffers = framebuffers;
    }

    public void destroy(VulkanDevice device, VulkanMemoryAllocator memoryAllocator) {
        for(VulkanFramebuffer framebuffer : framebuffers) {
            framebuffer.destroy(device);
        }

        for(VulkanImageView imageView : imageViews) {
            imageView.destroy(device);
        }

        for(VulkanImage image : images) {
            image.destroy(memoryAllocator);
        }
    }

    public VulkanImage[] getImages() {
        return images;
    }

    public VulkanImage getImage(int index) {
        return images[index];
    }

    public VulkanImageView[] getImageViews() {
        return imageViews;
    }

    public VulkanImageView getImageView(int index) {
        return imageViews[index];
    }

    public VulkanFramebuffer[] getFramebuffers() {
        return framebuffers;
    }

    public VulkanFramebuffer getFramebuffer(int index) {
        return framebuffers[index];
    }

    public static class Builder {

        //TODO: Support more attachements types
        /* 
        public static enum Attachment { 
            COLOR,
            DEPTH;
        }
        private final Set<Attachment> attachments = new HashSet<>();
        */
        
        private RenderPass renderPass;
        private int width;
        private int height;
        private int format;

        public FramebufferObject build(VulkanDevice device, VulkanMemoryAllocator memoryAllocator) {
            VulkanImage[] images = createImages(memoryAllocator);
            VulkanImageView[] imageViews = createImageViews(device, images);
            VulkanFramebuffer[] framebuffers = createFramebuffers(device, imageViews);

            return new FramebufferObject(images, imageViews, framebuffers);
        }

        public Builder setRenderPass(RenderPass renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFormat(int format) {
            this.format = format;
            return this;
        }
        
        private VulkanImage[] createImages(VulkanMemoryAllocator memoryAllocator) {
            VulkanImage[] images = new VulkanImage[RenderingEngine.MAX_FRAMES_IN_FLIGHT];
            for(int i = 0; i < images.length; i++) {
                images[i] = new VulkanImage.Builder()
                    .setExtentWidth(width)
                    .setExtentHeight(height)
                    .setFormat(format)
                    .setUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK11.VK_IMAGE_USAGE_SAMPLED_BIT | VK11.VK_IMAGE_USAGE_STORAGE_BIT)
                    .build(memoryAllocator);
            }
            return images;
        }

        private VulkanImageView[] createImageViews(VulkanDevice device, VulkanImage[] images) {
            VulkanImageView[] imageViews = new VulkanImageView[RenderingEngine.MAX_FRAMES_IN_FLIGHT];
            for(int i = 0; i < imageViews.length; i++) {
                imageViews[i] = new VulkanImageView.Builder()
                    .setImage(images[i].getHandle())
                    .setFormat(format)
                    .setComponentR(VK11.VK_COMPONENT_SWIZZLE_R)
                    .setComponentG(VK11.VK_COMPONENT_SWIZZLE_G)
                    .setComponentB(VK11.VK_COMPONENT_SWIZZLE_B)
                    .setComponentA(VK11.VK_COMPONENT_SWIZZLE_A)
                    .build(device);
            }
            return imageViews;
        }

        private VulkanFramebuffer[] createFramebuffers(VulkanDevice device, VulkanImageView[] imageViews) {
            VulkanFramebuffer[] framebuffers = new VulkanFramebuffer[RenderingEngine.MAX_FRAMES_IN_FLIGHT];
            for(int i = 0; i < framebuffers.length; i++) {
                framebuffers[i] = new VulkanFramebuffer.Builder()
                    .addAttachement(imageViews[i])
                    .setRenderPass(renderPass.getHandle())
                    .setWidth(width)
                    .setHeight(height)
                    .setLayers(1)
                    .build(device);
            }
            return framebuffers;
        }
    }
}
