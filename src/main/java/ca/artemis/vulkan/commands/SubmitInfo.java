package ca.artemis.vulkan.commands;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.synchronization.VulkanFence;

public class SubmitInfo {
    
    private VkSubmitInfo handle;
    private final VulkanFence fence;
    
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

    public void submit(VulkanDevice device, VkQueue queue) {
        if (fence != null){
			fence.reset(device);
		}
		
		int error = VK11.vkQueueSubmit(queue, handle,fence == null ? VK11.VK_NULL_HANDLE : fence.getHandle());
        if(error != VK11.VK_SUCCESS) 
            throw new AssertionError("Failed to submit to queue"); 
    }

    public SubmitInfo setWaitSemaphores(LongBuffer pWaitSemaphores, int count) {
        handle.waitSemaphoreCount(count);
        handle.pWaitSemaphores(pWaitSemaphores);

        return this;
    }

    public SubmitInfo setWaitDstStageMask(IntBuffer pWaitDstStageMask) {
        handle.pWaitDstStageMask(pWaitDstStageMask);

        return this;
    }

    public SubmitInfo setSignalSemaphores(LongBuffer pSignalSemaphores) {
        handle.pSignalSemaphores(pSignalSemaphores);

        return this;
    }

    public SubmitInfo setCommandBuffers(PointerBuffer pCommandBuffers) {
        handle.pCommandBuffers(pCommandBuffers);

        return this;
    }

    public VulkanFence getFence() {
        return fence;
    }
}