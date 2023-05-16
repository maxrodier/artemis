package ca.artemis.engine.api.vulkan.synchronization;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;

public class VulkanFence {
    
    private final long handle;

    public VulkanFence(VulkanDevice device) {
        this.handle = createHandle(device);
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyFence(device.getHandle(), handle, null);
    }

    private long createHandle(VulkanDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo pFenceCreateInfo = VkFenceCreateInfo.calloc(stack);
            pFenceCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            pFenceCreateInfo.flags(VK11.VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pFence = stack.callocLong(1);
            int result = VK11.vkCreateFence(device.getHandle(), pFenceCreateInfo, null, pFence);
            if(result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create fence: " + result);
            }

            return pFence.get(0);
        }
    }

    public void reset(VulkanDevice device) {
        int result = VK11.vkResetFences(device.getHandle(), handle);
        if(result != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to reset fence: " + result);
        }
    }

    public void waitFor(VulkanDevice device) {
        int result = VK11.vkWaitForFences(device.getHandle(), handle, true, 1000000000l);
        if(result != VK11.VK_SUCCESS)  {
            throw new AssertionError("Failed to wait for fence: " + result);
        }
    }

    public long getHandle() {
        return handle;
    }
}