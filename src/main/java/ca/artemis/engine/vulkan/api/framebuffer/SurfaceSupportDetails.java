
package ca.artemis.engine.vulkan.api.framebuffer;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.engine.vulkan.api.context.VulkanSurface;

public class SurfaceSupportDetails {

        private final VkSurfaceFormatKHR.Buffer surfaceFormats;
        private final IntBuffer surfacePresentModes;

        private final VkSurfaceCapabilitiesKHR surfaceCapabilities;
        private final VkSurfaceFormatKHR surfaceFormat;
        private final VkExtent2D surfaceExtent;
        private final int surfacePresentMode;

        public SurfaceSupportDetails(VulkanPhysicalDevice physicalDevice, VulkanSurface surface, Window window) {
            this.surfaceFormats = fetchSurfaceFormats(physicalDevice, surface); 
            this.surfacePresentModes = fetchSurfacePresentModes(physicalDevice, surface);

            this.surfaceCapabilities = fetchSurfaceCapabilities(physicalDevice, surface);
            this.surfaceFormat = chooseSurfaceFormat(surfaceFormats);
            this.surfaceExtent = chooseSurfaceExtent(window.getId(), surfaceCapabilities);
            this.surfacePresentMode = chooseSurfacePresentMode(surfacePresentModes);
        }

        public void destroy() {
            surfaceExtent.free();
            surfaceCapabilities.free();

            MemoryUtil.memFree(surfacePresentModes);
            surfaceFormats.free();
        }

        public VkSurfaceCapabilitiesKHR getSurfaceCapabilities() {
            return surfaceCapabilities;
        }

        public VkSurfaceFormatKHR getSurfaceFormat() {
            return surfaceFormat;
        }

        public VkExtent2D getSurfaceExtent() {
            return surfaceExtent;
        }

        public int getSurfacePresentMode() {
            return surfacePresentMode;
        }

        private static VkSurfaceCapabilitiesKHR fetchSurfaceCapabilities(VulkanPhysicalDevice physicalDevice, VulkanSurface surface) {
            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
            KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.getHandle(), surface.getHandle(), surfaceCapabilities);
            return surfaceCapabilities;
        }

        private static VkSurfaceFormatKHR chooseSurfaceFormat(VkSurfaceFormatKHR.Buffer formats) {
            for(int i = 0; i < formats.limit(); i++) {
                VkSurfaceFormatKHR format = formats.get(i);
                if(format.format() == VK11.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    return format;
                }
            }
            return formats.get(0);
        }
    
        private static VkExtent2D chooseSurfaceExtent(long window, VkSurfaceCapabilitiesKHR surfaceCapabilities) {
            if(surfaceCapabilities.currentExtent().width() != Integer.MAX_VALUE) {
                VkExtent2D actualExtent = VkExtent2D.calloc();
                actualExtent.width(surfaceCapabilities.currentExtent().width());
                actualExtent.height(surfaceCapabilities.currentExtent().height());
                return actualExtent;
            } else {
                IntBuffer width = IntBuffer.allocate(1), height = IntBuffer.allocate(1);
                GLFW.glfwGetFramebufferSize(window, width, height);
    
                VkExtent2D actualExtent = VkExtent2D.calloc();
                actualExtent.width(clamp(width.get(0), surfaceCapabilities.minImageExtent().width(), surfaceCapabilities.maxImageExtent().width()));
                actualExtent.height(clamp(height.get(0), surfaceCapabilities.minImageExtent().height(), surfaceCapabilities.maxImageExtent().height()));
    
                return actualExtent;
            }
        }
    
        private static int chooseSurfacePresentMode(IntBuffer surfacePresentModes) {
            for(int i = 0; i < surfacePresentModes.limit(); i++) {
                int presentMode = surfacePresentModes.get(i);
                if(presentMode == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
                    return presentMode;
                }
            }
    
            return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
        }
    
        private static VkSurfaceFormatKHR.Buffer fetchSurfaceFormats(VulkanPhysicalDevice physicalDevice, VulkanSurface surface) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pSurfaceFormatCount = stack.callocInt(1);
                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfaceFormatCount, null);
                
                VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(pSurfaceFormatCount.get(0));
                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfaceFormatCount, surfaceFormats);
                return surfaceFormats;
            }
        }
    
        private static IntBuffer fetchSurfacePresentModes(VulkanPhysicalDevice physicalDevice, VulkanSurface surface) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
    
                IntBuffer pSurfacePresentModesCount = stack.callocInt(1);
                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfacePresentModesCount, null);
            
                MemoryUtil.memCallocInt(pSurfacePresentModesCount.get(0));
                IntBuffer surfacePresentModes = MemoryUtil.memCallocInt(pSurfacePresentModesCount.get(0));
                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.getHandle(), surface.getHandle(), pSurfacePresentModesCount, surfacePresentModes);
                return surfacePresentModes;
            }
        }
    
        private static <T extends Comparable<T>> T clamp(T val, T min, T max) {
            if (val.compareTo(min) < 0) return min;
            else if (val.compareTo(max) > 0) return max;
            else return val;
        }
    }