package ca.artemis.vulkan.api.framebuffer;

import java.util.HashMap;

import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.api.memory.VulkanImage;
import ca.artemis.vulkan.api.memory.VulkanImageBundle;
import ca.artemis.vulkan.api.memory.VulkanImageView;

public abstract class FramebufferObject {

    protected final HashMap<Attachment, VulkanImageBundle> attachments = new HashMap<>();

    public void destroy() {
        for(VulkanImageBundle imageBundle : attachments.values()) {
            imageBundle.destroy();
        }
    }

    public VulkanImageBundle getAttachment(Attachment attachment) {
        return attachments.get(attachment);
    }

    protected static VulkanImageBundle createColorAttachment(int width, int height, int format) {
        VulkanImage image = new VulkanImage.Builder()
            .setExtentWidth(width)
            .setExtentHeight(height)
            .setFormat(format)
            .setUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK11.VK_IMAGE_USAGE_SAMPLED_BIT | VK11.VK_IMAGE_USAGE_STORAGE_BIT)
            .build();

        VulkanImageView imageView = new VulkanImageView.Builder()
            .setImage(image.getHandle())
            .setFormat(format)
            .setComponentR(VK11.VK_COMPONENT_SWIZZLE_R)
            .setComponentG(VK11.VK_COMPONENT_SWIZZLE_G)
            .setComponentB(VK11.VK_COMPONENT_SWIZZLE_B)
            .setComponentA(VK11.VK_COMPONENT_SWIZZLE_A)
            .build();

        return new VulkanImageBundle(image, imageView);
    }

    public static enum Attachment {
		COLOR,
		DEPTH;
	}
}