package ca.artemis.vulkan.rendering.renderer;

import org.lwjgl.system.MemoryStack;

import ca.artemis.vulkan.api.synchronization.VulkanSemaphore;

public abstract class Renderer {
    
    protected final VulkanSemaphore waitSemaphore;
    protected final VulkanSemaphore signalSemaphore;

    public Renderer(VulkanSemaphore waitSemaphore) {
        this.waitSemaphore = waitSemaphore;
        this.signalSemaphore = new VulkanSemaphore();
    }

    protected void destroy() {
        signalSemaphore.destroy();
    }

    public abstract void draw(MemoryStack stack);

    public VulkanSemaphore getSignalSemaphore() {
        return signalSemaphore;
    }
}
