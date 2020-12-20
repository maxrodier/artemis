package ca.artemis.vulkan.commands;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.rendering.Swapchain;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class PresentInfo {
    
    private VkPresentInfoKHR handle;

    private LongBuffer pWaitSemaphores;
    private LongBuffer pSwapchains;
    
    public PresentInfo() {
        this.handle = VkPresentInfoKHR.calloc().sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
    }

    public void destroy(VulkanDevice device) {
        if(pSwapchains != null) {
            MemoryUtil.memFree(pSwapchains);
        }
        if(pWaitSemaphores != null) {
            MemoryUtil.memFree(pWaitSemaphores);
        }

        handle.free();
    }

    public void present(VulkanDevice device) {
        int error = KHRSwapchain.vkQueuePresentKHR(device.getGraphicsQueue(), handle);
        if (error != VK11.VK_SUCCESS) {
            throw new AssertionError("Failed to submit present info");
        }
    }

    public PresentInfo setWaitSemaphores(VulkanSemaphore... waitSemaphores) {
        if(pWaitSemaphores != null) {
            MemoryUtil.memFree(pWaitSemaphores);
        }

        pWaitSemaphores = MemoryUtil.memAllocLong(waitSemaphores.length);
        for(VulkanSemaphore semaphore: waitSemaphores) {
            pWaitSemaphores.put(semaphore.getHandle());
        }
        pWaitSemaphores.flip();

        handle.pWaitSemaphores(pWaitSemaphores);

        return this;
    }

    public PresentInfo setSwapchains(Swapchain... swapchains) {
        if(pSwapchains != null) {
            MemoryUtil.memFree(pSwapchains);
        }

        pSwapchains = MemoryUtil.memAllocLong(swapchains.length);
        for(Swapchain swapchain: swapchains) {
            pSwapchains.put(swapchain.getHandle());
        }
        pSwapchains.flip();

        handle.swapchainCount(swapchains.length);
        handle.pSwapchains(pSwapchains);

        return this;
    }

    public PresentInfo setImageIndexPointer(IntBuffer pImageIndex) {
        handle.pImageIndices(pImageIndex);
        
        return this;
    }
}