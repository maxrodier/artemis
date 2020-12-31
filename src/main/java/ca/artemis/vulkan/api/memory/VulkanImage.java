package ca.artemis.vulkan.api.memory;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;

import ca.artemis.vulkan.context.VulkanMemoryAllocator;

public class VulkanImage {

    private final long handle;
    private final long allocationHandle;
    private final int width;
    private final int height;

    private VulkanImage(long handle, long allocationHandle, int width, int height) {
        this.handle = handle;
        this.allocationHandle = allocationHandle;
        this.width = width;
        this.height = height;
    }

    public void destroy(VulkanMemoryAllocator allocator) {
        Vma.vmaDestroyImage(allocator.getHandle(), handle, allocationHandle);
    }

    public long getHandle() {
        return handle;
    }

    public long getAllocationHandle() {
        return allocationHandle;   
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static class Builder {

        private int imageType = VK11.VK_IMAGE_TYPE_2D;
        private int extentWidth;
        private int extentHeight;
        private int extentDepth = 1;
        private int mipLevels = 1;
        private int arrayLayers = 1;
        private int format;
        private int tiling = VK11.VK_IMAGE_TILING_OPTIMAL;
        private int initialLayout = VK11.VK_IMAGE_LAYOUT_UNDEFINED;
        private int usage;
        private int sharingMode = VK11.VK_SHARING_MODE_EXCLUSIVE;
        private int samples = VK11.VK_SAMPLE_COUNT_1_BIT;

        public VulkanImage build(VulkanMemoryAllocator allocator) {
            try(MemoryStack stack = MemoryStack.stackPush()) {

                VkImageCreateInfo pImageCreateInfo = VkImageCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(imageType)
                    .extent(VkExtent3D.callocStack(stack).set(extentWidth, extentHeight, extentDepth))
                    .mipLevels(mipLevels)
                    .arrayLayers(arrayLayers)
                    .format(format)
                    .tiling(tiling)
                    .initialLayout(initialLayout)
                    .usage(usage)
                    .sharingMode(sharingMode)
                    .samples(samples);
                
                VmaAllocationCreateInfo pAllocationCreateInfo = VmaAllocationCreateInfo.callocStack(stack)
                    .usage(Vma.VMA_MEMORY_USAGE_GPU_ONLY);

                LongBuffer pImage = stack.callocLong(1);
                PointerBuffer pAllocation = stack.callocPointer(1);
                int error = Vma.vmaCreateImage(allocator.getHandle(), pImageCreateInfo, pAllocationCreateInfo, pImage, pAllocation, null);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create image");

                return new VulkanImage(pImage.get(0), pAllocation.get(0), extentWidth, extentHeight);
            }
        }

        public Builder setImageType(int imageType) {
            this.imageType = imageType;
            return this;
        }

        public Builder setExtentWidth(int width) {
            this.extentWidth = width;
            return this;
        }

        public Builder setExtentHeight(int height) {
            this.extentHeight = height;
            return this;
        }

        public Builder setExtentDepth(int depth) {
            this.extentDepth = depth;
            return this;
        }

        public Builder setMipLevels(int mipLevels) {
            this.mipLevels = mipLevels;
            return this;
        }

        public Builder setArrayLayers(int arrayLayers) {
            this.arrayLayers = arrayLayers;
            return this;
        }

        public Builder setFormat(int format) {
            this.format = format;
            return this;
        }        
        
        public Builder setTilling(int tiling) {
            this.tiling = tiling;
            return this;
        }

        public Builder setInitialLayout(int initialLayout) {
            this.initialLayout = initialLayout;
            return this;
        }

        public Builder setUsage(int usage) {
            this.usage = usage;
            return this;
        }

        public Builder setSharingMode(int sharingMode) {
            this.sharingMode = sharingMode;
            return this;
        }

        public Builder setSamples(int samples) {
            this.samples = samples;
            return this;
        }
    }
}