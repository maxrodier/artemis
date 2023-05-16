package ca.artemis.engine.api.vulkan.core;

import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;

public class VulkanSurface {
    
    private long handle;

    public VulkanSurface(VulkanInstance instance, GLFWWindow window) {
        this.handle = createHandle(instance, window);
    }

    public void destroy(VulkanInstance instance) {
        KHRSurface.vkDestroySurfaceKHR(instance.getHandle(), handle, null);
    }

    private static long createHandle(VulkanInstance instance, GLFWWindow window) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            int result = GLFWVulkan.glfwCreateWindowSurface(instance.getHandle(), window.getHandle(), null, pSurface);
            if(result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create surface: " + result);
            }
            return pSurface.get();
        }
    }

    public long getHandle() {
        return handle;
    }
}
