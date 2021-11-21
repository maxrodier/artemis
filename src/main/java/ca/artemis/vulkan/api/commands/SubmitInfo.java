package ca.artemis.vulkan.api.commands;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;

public class SubmitInfo {
    
    private VkSubmitInfo handle;
    private final VulkanFence fence;

    private LongBuffer pWaitSemaphores;
    private IntBuffer pWaitDstStageMask;
    private LongBuffer pSignalSemaphores;
    private PointerBuffer pCommandBuffers;
    
    public SubmitInfo() {
        this(null);
    }

    public SubmitInfo(VulkanFence fence) {
        this.handle = VkSubmitInfo.calloc().sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);
        this.fence = fence;
    }

    public void destroy() {
        handle.free();
    }

    public void submit(VkQueue queue) {
        if (fence != null){
			fence.reset();
		}
		
		int error = VK11.vkQueueSubmit(queue, handle,fence == null ? VK11.VK_NULL_HANDLE : fence.getHandle());
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to submit to queue"); 
    }

    public SubmitInfo setWaitSemaphores(VulkanSemaphore... waitSemaphores) {
        if(pWaitSemaphores != null) {
            MemoryUtil.memFree(pWaitSemaphores);
        }

        pWaitSemaphores = MemoryUtil.memAllocLong(waitSemaphores.length);
        for(VulkanSemaphore semaphore: waitSemaphores) {
            pWaitSemaphores.put(semaphore.getHandle());
        }
        pWaitSemaphores.flip();

        handle.waitSemaphoreCount(waitSemaphores.length);
        handle.pWaitSemaphores(pWaitSemaphores);

        return this;
    }

    public SubmitInfo setWaitDstStageMask(int... waitDstStageMasks) {
        if(pWaitDstStageMask != null) {
            MemoryUtil.memFree(pWaitDstStageMask);
        }

        pWaitDstStageMask = MemoryUtil.memAllocInt(waitDstStageMasks.length);
        pWaitDstStageMask.put(0, waitDstStageMasks);

        handle.pWaitDstStageMask(pWaitDstStageMask);

        return this;
    }

    public SubmitInfo setSignalSemaphores(VulkanSemaphore... signalSemaphores) {
        if(pSignalSemaphores != null) {
            MemoryUtil.memFree(pSignalSemaphores);
        }

        pSignalSemaphores = MemoryUtil.memAllocLong(signalSemaphores.length);
        for(VulkanSemaphore semaphore: signalSemaphores) {
            pSignalSemaphores.put(semaphore.getHandle());
        }
        pSignalSemaphores.flip();

        handle.pSignalSemaphores(pSignalSemaphores);

        return this;
    }

    public SubmitInfo setCommandBuffers(CommandBuffer... commandBuffers) {
        if(pCommandBuffers != null) {
            MemoryUtil.memFree(pCommandBuffers);
        }

        pCommandBuffers = MemoryUtil.memAllocPointer(commandBuffers.length);
        for(CommandBuffer commandBuffer: commandBuffers) {
            pCommandBuffers.put(commandBuffer.getHandle());
        }
        pCommandBuffers.flip();

        handle.pCommandBuffers(pCommandBuffers);

        return this;
    }

    public VulkanFence getFence() {
        return fence;
    }
}