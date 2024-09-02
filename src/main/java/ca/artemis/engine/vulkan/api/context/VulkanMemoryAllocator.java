package ca.artemis.engine.vulkan.api.context;

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
    
    public long getHandle() {
        return handle;
    }

    private long createHandle(VulkanInstance instance, VulkanPhysicalDevice physicalDevice, VulkanDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions pVulkanFunctions = VmaVulkanFunctions.callocStack(stack)
                .set(instance.getHandle(), device.getHandle());

            VmaAllocatorCreateInfo pCreateInfo = VmaAllocatorCreateInfo.callocStack()
                .physicalDevice(physicalDevice.getHandle())
                .device(device.getHandle())
                .pVulkanFunctions(pVulkanFunctions)
                .instance(instance.getHandle());

            PointerBuffer pAllocator = stack.callocPointer(1);
            if(Vma.vmaCreateAllocator(pCreateInfo, pAllocator) != VK11.VK_SUCCESS) {
                throw new AssertionError("Failed to create vulkan memory allocator");
            }
            return pAllocator.get(0);
        }
    }
}