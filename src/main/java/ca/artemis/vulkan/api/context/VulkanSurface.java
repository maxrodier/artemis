package ca.artemis.vulkan.api.context;

import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;

public class VulkanSurface {

    private final long handle;

    public VulkanSurface(VulkanInstance instance, GLFWWindow window) {
        this.handle = createHandle(instance, window);
    }

    public void destroy(VulkanInstance instance) {
        KHRSurface.vkDestroySurfaceKHR(instance.getHandle(), handle, null);
    }

    private long createHandle(VulkanInstance instance, GLFWWindow window) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.callocLong(1);
            if(GLFWVulkan.glfwCreateWindowSurface(instance.getHandle(), window.getHandle(), null, pSurface) != VK11.VK_SUCCESS) {
                throw new AssertionError("Failed to create window surface!");
            }
            return pSurface.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}