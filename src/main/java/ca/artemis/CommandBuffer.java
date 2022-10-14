package ca.artemis;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;

import ca.artemis.Util.VkImage;

public class CommandBuffer {

    protected final long handle;
    protected final VkCommandBuffer commandBuffer;

    protected CommandBuffer(VkDevice device, long handle) {
        this.handle = handle;
        this.commandBuffer = new VkCommandBuffer(this.handle, device);
    }

    public void destroy(VkDevice device, long commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pHandle = stack.callocPointer(1);
            pHandle.put(0, handle);
            VK11.vkFreeCommandBuffers(device, commandPool, pHandle);
        }
    }

    public void endRecording() {
        int error = VK11.vkEndCommandBuffer(commandBuffer);
        if(error != VK11.VK_SUCCESS)
            throw new AssertionError("Failed to end recording command buffer");
    }

    public void copyBufferCmd(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, VkBufferCopy.Buffer pRegions) {
        VK11.vkCmdCopyBuffer(commandBuffer, srcBuffer.getHandle(), dstBuffer.getHandle(), pRegions);
    }

    public void copyBufferToImage(VulkanBuffer buffer, VkImage image, VkBufferImageCopy.Buffer pRegions) {
        VK11.vkCmdCopyBufferToImage(commandBuffer, buffer.getHandle(), image.image, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, pRegions);
    }

    public void pipelineBarrierCmd(int sourceStage, int destinationStage, VkImageMemoryBarrier.Buffer pBarriers) {
        VK11.vkCmdPipelineBarrier(commandBuffer, sourceStage, destinationStage, 0, null, null, pBarriers);
    }

    public void blitImageCmd(VkImage srcImage, int srcImageLayout, VkImage dstImage, int dstImageLayout, VkImageBlit.Buffer pBlits, int filter) {
        VK11.vkCmdBlitImage(commandBuffer, srcImage.image, srcImageLayout, dstImage.image, dstImageLayout, pBlits, filter);
    }

    public long getHandle() {
        return handle;
    }
    public VkCommandBuffer getCommandBuffer() {
        return commandBuffer;
    }
}