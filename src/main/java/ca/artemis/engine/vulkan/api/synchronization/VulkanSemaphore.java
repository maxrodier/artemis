package ca.artemis.engine.vulkan.api.synchronization;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;

public class VulkanSemaphore {
    
    private final long handle;

    public VulkanSemaphore(VulkanDevice device) {
        this.handle = createHandle(device);
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroySemaphore(device.getHandle(), handle, null);
    }

    private long createHandle(VulkanDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo pSemaphoreCreateInfo = VkSemaphoreCreateInfo.callocStack(stack)
	            .sType(VK11.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .flags(0);
                
            LongBuffer pSemaphore = stack.callocLong(1);
            int error = VK11.vkCreateSemaphore(device.getHandle(), pSemaphoreCreateInfo, null, pSemaphore);
            if(error != VK11.VK_SUCCESS) 
                throw new AssertionError("Failed to create semaphore");

            return pSemaphore.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}
