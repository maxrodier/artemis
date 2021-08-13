package ca.artemis.vulkan.rendering.renderer;

import org.lwjgl.system.MemoryStack;

import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;

public abstract class Renderer {
    
    protected final VulkanSemaphore waitSemaphore;
    protected final VulkanSemaphore signalSemaphore;

    public Renderer(VulkanDevice device, VulkanSemaphore waitSemaphore) {
        this.waitSemaphore = waitSemaphore;
        this.signalSemaphore = new VulkanSemaphore(device);
    }

    protected void destroy(VulkanDevice device) {
        signalSemaphore.destroy(device);
    }

    public abstract void draw(VulkanDevice device, MemoryStack stack);

    public VulkanSemaphore getSignalSemaphore() {
        return signalSemaphore;
    }
}
