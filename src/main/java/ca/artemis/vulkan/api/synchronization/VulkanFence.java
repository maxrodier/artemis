package ca.artemis.vulkan.api.synchronization;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import ca.artemis.vulkan.api.context.VulkanContext;

public class VulkanFence {
    
    private final long handle;

    public VulkanFence() {
        this.handle = createHandle();
    }

    public void destroy() {
        VK11.vkDestroyFence(VulkanContext.getContext().getDevice().getHandle(), handle, null);
    }

    private long createHandle() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo pFenceCreateInfo = VkFenceCreateInfo.callocStack(stack);
            pFenceCreateInfo.sType(VK11.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            pFenceCreateInfo.flags(VK11.VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pFence = stack.callocLong(1);
            int error = VK11.vkCreateFence(VulkanContext.getContext().getDevice().getHandle(), pFenceCreateInfo, null, pFence);
            if(error != VK11.VK_SUCCESS) 
                throw new AssertionError("Failed to create fence");

            return pFence.get(0);
        }
    }

    public void reset() {
        int error = VK11.vkResetFences(VulkanContext.getContext().getDevice().getHandle(), handle);
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to reset fence");
    }

    public void waitFor() {
        int error = VK11.vkWaitForFences(VulkanContext.getContext().getDevice().getHandle(), handle, true, 1000000000l);
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to wait for fence");
    }

    public long getHandle() {
        return handle;
    }
}