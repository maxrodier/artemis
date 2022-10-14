package ca.artemis;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportDetails {

    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer presentModes;

    public VkSurfaceCapabilitiesKHR getCapabilities() {
        return capabilities;
    }

    public VkSurfaceFormatKHR chooseSwapSurfaceFormat() {
        for(int i = 0; i < formats.limit(); i++) {
            VkSurfaceFormatKHR format = formats.get(i);
            if(format.format() == VK11.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return format;
            }
        }

        return formats.get(0);
    }

    public Integer chooseSwapPresentMode() {
        for(int i = 0; i < presentModes.limit(); i++) {
            int presentMode = presentModes.get(i);
            if(presentMode == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
                return presentMode;
            }
        }

        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
    }

    public VkExtent2D chooseSwapExtent(long window) {
        if(capabilities.currentExtent().width() != Integer.MAX_VALUE) {
            return capabilities.currentExtent();
        } else {
            IntBuffer width = IntBuffer.allocate(1), height = IntBuffer.allocate(1);
            GLFW.glfwGetFramebufferSize(window, width, height);

            VkExtent2D actualExtent = VkExtent2D.calloc();
            actualExtent.width(clamp(width.get(0), capabilities.minImageExtent().width(), capabilities.maxImageExtent().width()));
            actualExtent.height(clamp(height.get(0), capabilities.minImageExtent().height(), capabilities.maxImageExtent().height()));

            return actualExtent;
        }
    }

    private static <T extends Comparable<T>> T clamp(T val, T min, T max) {
        if (val.compareTo(min) < 0) return min;
        else if (val.compareTo(max) > 0) return max;
        else return val;
    }

    public static SwapChainSupportDetails querySwapChainSupport(long surface, VkPhysicalDevice device) {
        SwapChainSupportDetails details = new SwapChainSupportDetails();

        details.capabilities = VkSurfaceCapabilitiesKHR.calloc();
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pSurfaceFormatCount = stack.callocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pSurfaceFormatCount, null);
            
            details.formats = VkSurfaceFormatKHR.calloc(pSurfaceFormatCount.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, pSurfaceFormatCount, details.formats);

            IntBuffer pSurfacePresentModesCount = stack.callocInt(1);
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pSurfacePresentModesCount, null);

            MemoryUtil.memCallocInt(pSurfacePresentModesCount.get(0));
            details.presentModes = MemoryUtil.memCallocInt(pSurfacePresentModesCount.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, pSurfacePresentModesCount, details.presentModes);
        }

        return details;
    }
}
