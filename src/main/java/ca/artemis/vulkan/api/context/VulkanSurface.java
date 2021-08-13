package ca.artemis.vulkan.api.context;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

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
            int error = GLFWVulkan.glfwCreateWindowSurface(instance.getHandle(), window.getHandle(), null, pSurface);
            if(error != VK11.VK_SUCCESS) {
                throw new AssertionError("Failed to create window surface");
            }
    
            return pSurface.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }

    public static VkSurfaceCapabilitiesKHR fetchSurfaceCapabilities(VulkanPhysicalDevice physicalDevice, VulkanSurface surface) {
        VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getHandle(), surface.getHandle(), surfaceCapabilities);
        return surfaceCapabilities;
    }

    public static VkSurfaceFormatKHR.Buffer fetchSurfaceFormats(VulkanPhysicalDevice physicalDevice, VulkanSurface surface) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pSurfaceFormatCount = stack.callocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfaceFormatCount, null);
            
            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(pSurfaceFormatCount.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfaceFormatCount, surfaceFormats);
            return surfaceFormats;
        }
    }
}