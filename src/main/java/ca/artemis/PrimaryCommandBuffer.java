package ca.artemis;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;

public class PrimaryCommandBuffer extends CommandBuffer {

    public PrimaryCommandBuffer(VkDevice device, long commandPool) {
        super(device, createHandle(device, commandPool));
    }
    
    public void beginRecording(MemoryStack stack, int flags) {
        VkCommandBufferBeginInfo pBeginInfo = VkCommandBufferBeginInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            .flags(flags);

        int error = VK11.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
        if(error != VK11.VK_SUCCESS)
            throw new AssertionError("Failed to begin recording command buffer");
    }
    
    private static long createHandle(VkDevice device, long commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo pAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool)
                .level(VK11.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(1);
    
            PointerBuffer pCommandBuffer = stack.callocPointer(1);
            int error = VK11.vkAllocateCommandBuffers(device, pAllocateInfo, pCommandBuffer);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to allocate command buffer");

            return pCommandBuffer.get(0);
        }
    }
}