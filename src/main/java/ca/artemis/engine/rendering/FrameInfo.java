package ca.artemis.engine.rendering;

import java.nio.IntBuffer;

import ca.artemis.engine.api.vulkan.synchronization.VulkanFence;
import ca.artemis.engine.api.vulkan.synchronization.VulkanSemaphore;

public class FrameInfo {
    
    public int frameIndex;
    public IntBuffer pImageIndex;
    public VulkanSemaphore imageAvailableSemaphore;
    public VulkanSemaphore renderFinishedSemaphore;
    public VulkanFence inFlightFence;
}