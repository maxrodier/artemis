package ca.artemis.vulkan.context;

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
    
    private long createHandle(VulkanInstance instance, VulkanPhysicalDevice physicalDevice, VulkanDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions pVulkanFunctions = VmaVulkanFunctions.callocStack(stack);
            pVulkanFunctions.set(instance.getHandle(), device.getHandle());

            VmaAllocatorCreateInfo pCreateInfo = VmaAllocatorCreateInfo.callocStack();
            pCreateInfo.physicalDevice(physicalDevice.getHandle());
            pCreateInfo.device(device.getHandle());
            pCreateInfo.pVulkanFunctions(pVulkanFunctions);

            PointerBuffer pAllocator = stack.callocPointer(1);
            int error = Vma.vmaCreateAllocator(pCreateInfo, pAllocator);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create vulkan memory allocator");

            return pAllocator.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}