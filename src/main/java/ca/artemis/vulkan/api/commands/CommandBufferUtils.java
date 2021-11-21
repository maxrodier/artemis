package ca.artemis.vulkan.api.commands;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanImage;

public class CommandBufferUtils {

    public static void copyBuffer(VkQueue queue, CommandPool commandPool, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int size) {
        try(MemoryStack stack = MemoryStack.stackPush()) {

            VkBufferCopy.Buffer pRegions = VkBufferCopy.callocStack(1, stack);
            pRegions.get(0)
                    .size(size);
            
            PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
            commandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            commandBuffer.copyBufferCmd(srcBuffer, dstBuffer, pRegions);
            commandBuffer.endRecording();
            singleCommandBufferSubmit(queue, commandBuffer, stack);
            commandBuffer.destroy(commandPool);
        }
    }

    public static void copyBufferToImage(VkQueue queue, CommandPool commandPool, VulkanBuffer buffer, VulkanImage image) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceLayers subresourceLayers = VkImageSubresourceLayers.callocStack(stack)
                .aspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
                .mipLevel(0)
                .baseArrayLayer(0)
                .layerCount(1);
        
            VkBufferImageCopy.Buffer pRegions = VkBufferImageCopy.callocStack(1, stack);
            pRegions.get(0)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                .imageSubresource(subresourceLayers)
                .imageOffset(e -> e.set(0, 0, 0))
                .imageExtent(e -> e.set(image.getWidth(), image.getHeight(), 1));
            
            PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
            commandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            commandBuffer.copyBufferToImage(buffer, image, pRegions);
            commandBuffer.endRecording();
            singleCommandBufferSubmit(queue, commandBuffer, stack);
            commandBuffer.destroy(commandPool);
        }
    }

    public static void transitionImageLayout(VkQueue queue, CommandPool commandPool, VulkanImage image, int format, int oldLayout, int newLayout, int mipLevels) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange pSubresourceRange = VkImageSubresourceRange.callocStack(stack)
                .baseMipLevel(0)
                .levelCount(mipLevels)
                .baseArrayLayer(0)
                .layerCount(1);

            VkImageMemoryBarrier.Buffer pBarriers = VkImageMemoryBarrier.callocStack(1, stack);
            pBarriers.get(0)
                .sType(VK11.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .oldLayout(oldLayout)
                .newLayout(newLayout)
                .srcQueueFamilyIndex(VK11.VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK11.VK_QUEUE_FAMILY_IGNORED)
                .image(image.getHandle())
                .subresourceRange(pSubresourceRange);
            
            if(newLayout == VK11.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                pBarriers.get(0).subresourceRange().aspectMask(VK11.VK_IMAGE_ASPECT_DEPTH_BIT);
            } else {
                pBarriers.get(0).subresourceRange().aspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT);
            }

            int sourceStage;
            int destinationStage;
            
            if(oldLayout == VK11.VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                pBarriers.get(0)
                    .srcAccessMask(0) 
                    .dstAccessMask(VK11.VK_ACCESS_TRANSFER_WRITE_BIT);
                sourceStage = VK11.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK11.VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if(oldLayout == VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                pBarriers.get(0)
                    .srcAccessMask(VK11.VK_ACCESS_TRANSFER_WRITE_BIT) 
                    .dstAccessMask(VK11.VK_ACCESS_SHADER_READ_BIT); 
                sourceStage = VK11.VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK11.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else if(oldLayout == VK11.VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK11.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                pBarriers.get(0)
                    .srcAccessMask(0)
                    .dstAccessMask(VK11.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK11.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                sourceStage = VK11.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK11.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT; 
            } else {
                throw new IllegalStateException("Unsupported layout transition");
            }

            PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
            commandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            commandBuffer.pipelineBarrierCmd(sourceStage, destinationStage, pBarriers);
            commandBuffer.endRecording();
            singleCommandBufferSubmit(queue, commandBuffer, stack);
            commandBuffer.destroy(commandPool);

        }
    }

    public static void generateMipmaps(VkQueue queue, CommandPool commandPool, VulkanImage image, int mipLevels) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier.Buffer pBarriers = VkImageMemoryBarrier.callocStack(1, stack);
            VkImageMemoryBarrier pBarrier = pBarriers.get(0);
            pBarrier
                .sType(VK11.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .image(image.getHandle())
                .srcQueueFamilyIndex(VK11.VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK11.VK_QUEUE_FAMILY_IGNORED)
                .subresourceRange()
                    .aspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseArrayLayer(0)
                    .layerCount(1)
                    .levelCount(1);

            int mipWidth = image.getWidth();
            int mipHeight = image.getHeight();

            PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(commandPool);
            commandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            for(int i = 1; i < mipLevels; i++) {
                pBarrier
                    .oldLayout(VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                    .newLayout(VK11.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
                    .srcAccessMask(VK11.VK_ACCESS_TRANSFER_WRITE_BIT)
                    .dstAccessMask(VK11.VK_ACCESS_TRANSFER_READ_BIT)
                    .subresourceRange().baseMipLevel(i - 1);
                commandBuffer.pipelineBarrierCmd(VK11.VK_PIPELINE_STAGE_TRANSFER_BIT, VK11.VK_PIPELINE_STAGE_TRANSFER_BIT, pBarriers);
            
                VkOffset3D.Buffer pSrcOffsets = VkOffset3D.callocStack(2, stack);
                pSrcOffsets.get(0)
                    .set(0, 0, 0);
                    pSrcOffsets.get(1)
                    .set(mipWidth, mipHeight, 1);
                VkImageSubresourceLayers pSrcSubresourceLayers = VkImageSubresourceLayers.callocStack(stack)
                    .aspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(i-1)
                    .baseArrayLayer(0)
                    .layerCount(1);

                VkOffset3D.Buffer pDstOffsets = VkOffset3D.callocStack(2, stack);
                pDstOffsets.get(0)
                    .set(0, 0, 0);
                pDstOffsets.get(1)
                    .set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1);
                VkImageSubresourceLayers pDstSubresourceLayers = VkImageSubresourceLayers.callocStack(stack)
                    .aspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(i)
                    .baseArrayLayer(0)
                    .layerCount(1);

                VkImageBlit.Buffer pBlits = VkImageBlit.callocStack(1, stack);;
                pBlits.get(0)
                    .srcOffsets(pSrcOffsets)
                    .srcSubresource(pSrcSubresourceLayers)
                    .dstOffsets(pDstOffsets)
                    .dstSubresource(pDstSubresourceLayers);
                commandBuffer.blitImageCmd(image, VK11.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, image, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, pBlits, VK11.VK_FILTER_LINEAR);
            
                pBarrier
                    .oldLayout(VK11.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
                    .newLayout(VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .srcAccessMask(VK11.VK_ACCESS_TRANSFER_READ_BIT)
                    .dstAccessMask(VK11.VK_ACCESS_SHADER_READ_BIT);
                commandBuffer.pipelineBarrierCmd(VK11.VK_PIPELINE_STAGE_TRANSFER_BIT, VK11.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, pBarriers);
                
                if (mipWidth > 1) mipWidth /= 2;
                if (mipHeight > 1) mipHeight /= 2;
            }
             
            pBarrier
                .oldLayout(VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                .newLayout(VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                .srcAccessMask(VK11.VK_ACCESS_TRANSFER_WRITE_BIT)
                .dstAccessMask(VK11.VK_ACCESS_SHADER_READ_BIT)
                .subresourceRange().baseMipLevel(mipLevels - 1);
            commandBuffer.pipelineBarrierCmd(VK11.VK_PIPELINE_STAGE_TRANSFER_BIT, VK11.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, pBarriers);
            commandBuffer.endRecording();
            singleCommandBufferSubmit(queue, commandBuffer, stack);
            commandBuffer.destroy(commandPool);
        }
    } 

    private static void singleCommandBufferSubmit(VkQueue queue, CommandBuffer commandBuffer, MemoryStack stack) {
    	VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack)
    		.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO)
    		.pCommandBuffers(stack.callocPointer(1).put(0, commandBuffer.getHandle()));
    		
		VK11.vkQueueSubmit(queue, submitInfo, VK11.VK_NULL_HANDLE);
		VK11.vkQueueWaitIdle(queue);
    }
}