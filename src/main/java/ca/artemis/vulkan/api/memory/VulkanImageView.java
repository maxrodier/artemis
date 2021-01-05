package ca.artemis.vulkan.api.memory;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import ca.artemis.vulkan.api.context.VulkanDevice;

public class VulkanImageView {

    private final long handle;

    public VulkanImageView(long handle) {
        this.handle = handle;
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyImageView(device.getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public static class Builder {

        private long image;
        private int viewType = VK11.VK_IMAGE_VIEW_TYPE_2D;
        private int format;
        private int componentR = VK11.VK_COMPONENT_SWIZZLE_IDENTITY;
        private int componentG = VK11.VK_COMPONENT_SWIZZLE_IDENTITY;
        private int componentB = VK11.VK_COMPONENT_SWIZZLE_IDENTITY;
        private int componentA = VK11.VK_COMPONENT_SWIZZLE_IDENTITY;
        private int aspectMask = VK11.VK_IMAGE_ASPECT_COLOR_BIT;
        private int baseMipLevel = 0;
        private int levelCount = 1;
        private int baseArrayLayer = 0;
        private int layerCount = 1;
    
        public VulkanImageView build(VulkanDevice device) {
            try(MemoryStack stack = MemoryStack.stackPush()) {

                VkImageViewCreateInfo pImageViewCreateInfo = VkImageViewCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(image)
                .viewType(viewType)
                .format(format)
                .components(it -> it
                    .r(componentR)
                    .g(componentG)
                    .b(componentB)
                    .a(componentA))
                .subresourceRange(it -> it
                    .aspectMask(aspectMask)
                    .baseMipLevel(baseMipLevel)
                    .levelCount(levelCount)
                    .baseArrayLayer(baseArrayLayer)
                    .layerCount(layerCount));
    
                LongBuffer pImageView = stack.callocLong(1);
                int error = VK11.vkCreateImageView(device.getHandle(), pImageViewCreateInfo, null, pImageView);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to ccreate image view");
    
                return new VulkanImageView(pImageView.get(0));
            }
        }

        public Builder setImage(long image) {
            this.image = image;
            return this;
        }
    
        public Builder setViewType(int viewType) {
            this.viewType = viewType;
            return this;
        }
    
        public Builder setFormat(int format) {
            this.format = format;
            return this;
        }
    
        public Builder setComponentR(int componentR) {
            this.componentR = componentR;
            return this;
        }
    
        public Builder setComponentG(int componentG) {
            this.componentG = componentG;
            return this;
        }
    
        public Builder setComponentB(int componentB) {
            this.componentB = componentB;
            return this;
        }
    
        public Builder setComponentA(int componentA) {
            this.componentA = componentA;
            return this;
        }
    
        public Builder setAspectMask(int aspectMask) {
            this.aspectMask = aspectMask;
            return this;
        }
    
        public Builder setBaseMipLevel(int baseMipLevel) {
            this.baseMipLevel = baseMipLevel;
            return this;
        }
    
        public Builder setLevelCount(int levelCount) {
            this.levelCount = levelCount;
            return this;
        }
    
        public Builder setBaseArrayLayer(int baseArrayLayer) {
            this.baseArrayLayer = baseArrayLayer;
            return this;
        }
    
        public Builder setLayerCount(int layerCount) {
            this.layerCount = layerCount;
            return this;
        }
    }
}