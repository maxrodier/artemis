package ca.artemis.engine.api.vulkan.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK11;

public class VulkanMemoryAllocator {
    
    private final long handle;

    public VulkanMemoryAllocator(VulkanInstance instance, VulkanPhysicalDevice physicalDevice, VulkanDevice device) {
        this.handle = createHandle(instance, physicalDevice, device);
    }
    
    public void destroy() {
        Vma.vmaDestroyAllocator(handle);
    }

    private static long createHandle(VulkanInstance instance, VulkanPhysicalDevice physicalDevice, VulkanDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions pVulkanFunctions = VmaVulkanFunctions.calloc(stack);
            pVulkanFunctions.set(instance.getHandle(), device.getHandle());

            VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.calloc(stack)
                .pVulkanFunctions(pVulkanFunctions)
                .physicalDevice(physicalDevice.getHandle())
                .device(device.getHandle())
                .instance(instance.getHandle());

            PointerBuffer pAllocator = stack.mallocPointer(1);
            int result = Vma.vmaCreateAllocator(allocatorInfo, pAllocator);
            if (result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create memory allocator: " + result);
            }

            return pAllocator.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}
