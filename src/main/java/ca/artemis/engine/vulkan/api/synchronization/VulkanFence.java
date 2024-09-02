package ca.artemis.engine.vulkan.api.synchronization;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;

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
            int error = VK11.vkCreateFence(device.getHandle(), pFenceCreateInfo, null, pFence);
            if(error != VK11.VK_SUCCESS) 
                throw new AssertionError("Failed to create fence");

            return pFence.get(0);
        }
    }

    public void reset(MemoryStack stack, VulkanDevice device) {
        int error = VK11.vkResetFences(device.getHandle(), stack.callocLong(1).put(0, handle));
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to reset fence");
    }

    public void waitFor(MemoryStack stack, VulkanDevice device) {
        int error = VK11.vkWaitForFences(device.getHandle(), stack.callocLong(1).put(0, handle), true, Long.MAX_VALUE);
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to wait for fence");
    }

    public long getHandle() {
        return handle;
    }
}