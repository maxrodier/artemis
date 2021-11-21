package ca.artemis.vulkan.api.memory;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import ca.artemis.vulkan.api.context.VulkanContext;

public class VulkanBuffer {

    private final long handle;
    private final long allocationHandle;
    private final int length;
    private final int size;

    private VulkanBuffer(long handle, long allocationHandle, int length, int size) {
        this.handle = handle;
        this.allocationHandle = allocationHandle;
        this.length = length;
        this.size = size;
    }

    public void destroy() {
        Vma.vmaDestroyBuffer(VulkanContext.getContext().getMemoryAllocator().getHandle(), handle, allocationHandle);
    }

    public long getHandle() {
        return handle;
    }

    public long getAllocationHandle() {
        return allocationHandle;
    }
    
    public int getLenght() {
        return length;
    }

    public int getSize() {
        return size;
    }

    public static class Builder {

        private int size;
        private int length;
        private int bufferUsage;
        private int memoryUsage;

        public VulkanBuffer build() {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkBufferCreateInfo pBufferCreateInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(length * size)
                    .usage(bufferUsage);

                VmaAllocationCreateInfo pAllocationCreateInfo = VmaAllocationCreateInfo.callocStack(stack);
                pAllocationCreateInfo.usage(memoryUsage);

                LongBuffer pBuffer = stack.callocLong(1);
                PointerBuffer pAllocation = stack.callocPointer(1);
                Vma.vmaCreateBuffer(VulkanContext.getContext().getMemoryAllocator().getHandle(), pBufferCreateInfo, pAllocationCreateInfo, pBuffer, pAllocation, null);

                return new VulkanBuffer(pBuffer.get(0), pAllocation.get(0), length, size);
            }
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setLength(int length) {
            this.length = length;
            return this;
        }

        public Builder setBufferUsage(int bufferUsage) {
            this.bufferUsage = bufferUsage;
            return this;
        }

        public Builder setMemoryUsage(int memoryUsage) {
            this.memoryUsage = memoryUsage;
            return this;
        }
    }
}