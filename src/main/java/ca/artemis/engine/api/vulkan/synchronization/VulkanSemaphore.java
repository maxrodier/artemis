package ca.artemis.engine.api.vulkan.synchronization;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;

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
            VkSemaphoreCreateInfo pSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack)
	            .sType(VK11.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .flags(0);
                
            LongBuffer pSemaphore = stack.callocLong(1);
            int result = VK11.vkCreateSemaphore(device.getHandle(), pSemaphoreCreateInfo, null, pSemaphore);
            if(result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create semaphore: " + result);
            }

            return pSemaphore.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}