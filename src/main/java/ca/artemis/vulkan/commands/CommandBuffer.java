package ca.artemis.vulkan.commands;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.descriptor.DescriptorSet;
import ca.artemis.vulkan.memory.VulkanBuffer;
import ca.artemis.vulkan.memory.VulkanImage;
import ca.artemis.vulkan.pipeline.GraphicsPipeline;

public class CommandBuffer {

    protected final long handle;
    protected final VkCommandBuffer commandBuffer;

    protected CommandBuffer(VulkanDevice device, long handle) {
        this.handle = handle;
        this.commandBuffer = new VkCommandBuffer(this.handle, device.getHandle());
    }

    public void destroy(VulkanDevice device, CommandPool commandPool) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pHandle = stack.callocPointer(1);
            pHandle.put(0, handle);
            VK11.vkFreeCommandBuffers(device.getHandle(), commandPool.getHandle(), pHandle);
        }
    }

    public void endRecording() {
        int error = VK11.vkEndCommandBuffer(commandBuffer);
        if(error != VK11.VK_SUCCESS)
            throw new AssertionError("Failed to end recording command buffer");
    }

    public void resetCommandBuffer() {
        VK11.vkResetCommandBuffer(commandBuffer, 0);
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

    public void bindPipelineCmd(int pipelineBindPoint, GraphicsPipeline pipeline) {
        VK11.vkCmdBindPipeline(commandBuffer, pipelineBindPoint, pipeline.getHandle());
    }

    public void bindVertexBufferCmd(MemoryStack stack, VulkanBuffer vertexBuffer) {
        LongBuffer pBuffers = stack.callocLong(1);
        pBuffers.put(0, vertexBuffer.getHandle());

        LongBuffer pOffsets = stack.callocLong(1);
        pOffsets.put(0, 0L);

        VK11.vkCmdBindVertexBuffers(commandBuffer, 0, pBuffers, pOffsets);
    }

    public void bindIndexBufferCmd(VulkanBuffer indexBuffer) {
        VK11.vkCmdBindIndexBuffer(commandBuffer, indexBuffer.getHandle(), 0, VK11.VK_INDEX_TYPE_UINT32);
    }

    public void bindDescriptorSetsCmd(MemoryStack stack, int pipelineBindPoint, long pipelineLayout, DescriptorSet... descriptorSets) {
        LongBuffer pDescriptorSets = stack.callocLong(descriptorSets.length);
        for(int i = 0; i < descriptorSets.length; i++) {
            pDescriptorSets.put(0, descriptorSets[i].getHandle());
        }
        VK11.vkCmdBindDescriptorSets(commandBuffer, pipelineBindPoint, pipelineLayout, 0, pDescriptorSets, null);
    }

    public void pushConstants(long pipelineLayout, int stageFlags, int offset, FloatBuffer pValues) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkCmdPushConstants(commandBuffer, pipelineLayout, stageFlags, offset, pValues);
        }
    }

    public void setViewportCmd(VkViewport.Buffer pViewports) {
        VK11.vkCmdSetViewport(commandBuffer, 0, pViewports);
    }

    public void setScissorCmd(VkRect2D.Buffer pScissors) {
        VK11.vkCmdSetScissor(commandBuffer, 0, pScissors);
    }

    public void drawIndexedCmd(int indexCount, int instanceCount) {
        VK11.vkCmdDrawIndexed(commandBuffer, indexCount, instanceCount, 0, 0, 0);
    }

    public void copyBufferCmd(VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, VkBufferCopy.Buffer pRegions) {
        VK11.vkCmdCopyBuffer(commandBuffer, srcBuffer.getHandle(), dstBuffer.getHandle(), pRegions);
    }

    public void copyBufferToImage(VulkanBuffer buffer, VulkanImage image, VkBufferImageCopy.Buffer pRegions) {
        VK11.vkCmdCopyBufferToImage(commandBuffer, buffer.getHandle(), image.getHandle(), VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, pRegions);
    }

    public void pipelineBarrierCmd(int sourceStage, int destinationStage, VkImageMemoryBarrier.Buffer pBarriers) {
        VK11.vkCmdPipelineBarrier(commandBuffer, sourceStage, destinationStage, 0, null, null, pBarriers);
    }

    public void blitImageCmd(VulkanImage srcImage, int srcImageLayout, VulkanImage dstImage, int dstImageLayout, VkImageBlit.Buffer pBlits, int filter) {
        VK11.vkCmdBlitImage(commandBuffer, srcImage.getHandle(), srcImageLayout, dstImage.getHandle(), dstImageLayout, pBlits, filter);
    }

    public long getHandle() {
        return handle;
    }
    public VkCommandBuffer getCommandBuffer() {
        return commandBuffer;
    }
}