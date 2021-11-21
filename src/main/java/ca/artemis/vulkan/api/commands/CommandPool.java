package ca.artemis.vulkan.api.commands;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import ca.artemis.vulkan.api.context.VulkanDevice;

public class CommandPool {

    private final long handle;

    public CommandPool(VulkanDevice device, int queueFamilyIndex, int flags) {
        this.handle = createHandle(device, queueFamilyIndex, flags);
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyCommandPool(device.getHandle(), handle, null);
    }

    private static long createHandle(VulkanDevice device, int queueFamilyIndex, int flags) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo comandPoolInfo = VkCommandPoolCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .queueFamilyIndex(queueFamilyIndex)
                .flags(flags);

            LongBuffer pCommandPool = stack.callocLong(1);
            int error = VK11.vkCreateCommandPool(device.getHandle(), comandPoolInfo, null, pCommandPool);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create commandPool");

            return pCommandPool.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}