package ca.artemis.vulkan.api.commands;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;

public class SecondaryCommandBuffer extends CommandBuffer {

    public SecondaryCommandBuffer(VulkanDevice device, CommandPool commandPool) { 
        super(device, createHandle(device, commandPool));
    }

    private static long createHandle(VulkanDevice device, CommandPool commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo pAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool.getHandle())
                .level(VK11.VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                .commandBufferCount(1);
    
            PointerBuffer pCommandBuffer = stack.callocPointer(1);
            int error = VK11.vkAllocateCommandBuffers(device.getHandle(), pAllocateInfo, pCommandBuffer);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to allocate command buffer");

            return pCommandBuffer.get(0);
        }
    }
    
    public void beginRecording(MemoryStack stack, int flags, RenderPass renderPass, VulkanFramebuffer framebuffer) {
        VkCommandBufferInheritanceInfo pInheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
            .renderPass(renderPass.getHandle())
            .subpass(0)
            .framebuffer(framebuffer.getHandle());
        
        VkCommandBufferBeginInfo pBeginInfo = VkCommandBufferBeginInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
            .flags(flags)
            .pInheritanceInfo(pInheritanceInfo);

        int error = VK11.vkBeginCommandBuffer(commandBuffer, pBeginInfo);
        if(error != VK11.VK_SUCCESS)
            throw new AssertionError("Failed to begin recording command buffer");
    }


}