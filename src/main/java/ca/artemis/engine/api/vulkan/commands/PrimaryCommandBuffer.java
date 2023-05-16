package ca.artemis.engine.api.vulkan.commands;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;

public class PrimaryCommandBuffer extends CommandBuffer {

    public PrimaryCommandBuffer(VulkanDevice device, CommandPool commandPool) {
        super(device, createHandle(device, commandPool));
    }
    
    public void beginRecording(MemoryStack stack, int flags) {
        VkCommandBufferBeginInfo pBeginInfo = VkCommandBufferBeginInfo.calloc(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            .flags(flags);

        int result = VK11.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
        if(result != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to begin recording command buffer: " + result);
        }
    }
    
    private static long createHandle(VulkanDevice device, CommandPool commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo pAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool.getHandle())
                .level(VK11.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(1);
    
            PointerBuffer pCommandBuffer = stack.callocPointer(1);
            int result = VK11.vkAllocateCommandBuffers(device.getHandle(), pAllocateInfo, pCommandBuffer);
            if(result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffer: " + result);
            }

            return pCommandBuffer.get(0);
        }
    }
}