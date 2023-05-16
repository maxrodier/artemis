package ca.artemis.vulkan.rendering;

import java.nio.IntBuffer;

import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;

public class FrameInfo {
    
    public int frameIndex;
    public IntBuffer pImageIndex;
    public VulkanSemaphore imageAvailableSemaphore;
    public VulkanSemaphore renderFinishedSemaphore;
    public VulkanFence inFlightFence;
}
