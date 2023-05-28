package ca.artemis.engine.vulkan.api.commands;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;

public class PrimaryCommandBuffer extends CommandBuffer {

    public PrimaryCommandBuffer(VulkanDevice device, CommandPool commandPool) {
        super(device, createHandle(device, commandPool));
    }

    private static long createHandle(VulkanDevice device, CommandPool commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo pAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool.getHandle())
                .level(VK11.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(1);
    
            PointerBuffer pCommandBuffer = stack.callocPointer(1);
            int error = VK11.vkAllocateCommandBuffers(device.getHandle(), pAllocateInfo, pCommandBuffer);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to allocate command buffer");

            return pCommandBuffer.get(0);
        }
    }
    
    public void beginRecording(MemoryStack stack, int flags) {
        VkCommandBufferBeginInfo pBeginInfo = VkCommandBufferBeginInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            .flags(flags);

        int error = VK11.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
        if(error != VK11.VK_SUCCESS)
            throw new AssertionError("Failed to begin recording command buffer");
    }

    public void beginRenderPassCmd(MemoryStack stack, long renderPass, long framebuffer, int width, int height, VkClearValue.Buffer pClearValues, int flags) {
        VkRenderPassBeginInfo pRenderPassBegin = VkRenderPassBeginInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
            .renderPass(renderPass)
            .framebuffer(framebuffer)
            .renderArea(ra -> ra.extent(it -> it.width(width).height(height)))
            .pClearValues(pClearValues);

        VK11.vkCmdBeginRenderPass(commandBuffer, pRenderPassBegin, flags);
    }

    public void endRenderPassCmd() {
        VK11.vkCmdEndRenderPass(commandBuffer);
    }
    

}