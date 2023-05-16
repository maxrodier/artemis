package ca.artemis.engine.api.vulkan.core;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class VulkanPhysicalDevice {
    
    private final VkPhysicalDevice handle;
    private final VkPhysicalDeviceProperties physicalDeviceProperties;
    private final VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties;
    private final VkPhysicalDeviceFeatures physicalDeviceFeatures;
    private final List<String> physicalDeviceExtensionProperties;
    private final List<VkQueueFamilyProperties> queueFamilyProperties;

    public VulkanPhysicalDevice(VkPhysicalDevice handle) {
        this.handle = handle;
        this.physicalDeviceProperties = findPhysicalDeviceProperties(handle);
        this.physicalDeviceMemoryProperties = findPhysicalDeviceMemoryProperties(handle);
        this.physicalDeviceFeatures = findPhysicalDeviceFeatures(handle);
        this.physicalDeviceExtensionProperties = findPhysicalDeviceExtensionProperties(handle);
        this.queueFamilyProperties = findQueueFamilyProperties(handle);
    }

    public void destroy() {
        physicalDeviceProperties.free();
        physicalDeviceMemoryProperties.free();
        physicalDeviceFeatures.free();
    }

    private static VkPhysicalDeviceProperties findPhysicalDeviceProperties(VkPhysicalDevice handle) {
        VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
        VK11.vkGetPhysicalDeviceProperties(handle, physicalDeviceProperties);
        return physicalDeviceProperties;
    }

    private static VkPhysicalDeviceMemoryProperties findPhysicalDeviceMemoryProperties(VkPhysicalDevice handle) {
        VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        VK11.vkGetPhysicalDeviceMemoryProperties(handle, physicalDeviceMemoryProperties);
        return physicalDeviceMemoryProperties;
    }

    private static VkPhysicalDeviceFeatures findPhysicalDeviceFeatures(VkPhysicalDevice handle) {
        VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
        VK11.vkGetPhysicalDeviceFeatures(handle, physicalDeviceFeatures);
        return physicalDeviceFeatures;
    }

    private static List<String> findPhysicalDeviceExtensionProperties(VkPhysicalDevice handle) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            List<String> extensionProperties = new ArrayList<>();

            IntBuffer pExtensionCount = stack.callocInt(1);
            VK11.vkEnumerateDeviceExtensionProperties(handle, (CharSequence) null, pExtensionCount, null);

            VkExtensionProperties.Buffer pExtensions = VkExtensionProperties.calloc(pExtensionCount.get(0));
            VK11.vkEnumerateDeviceExtensionProperties(handle, (CharSequence) null, pExtensionCount, pExtensions);
            while (pExtensions.hasRemaining()) {
                extensionProperties.add(pExtensions.get().extensionNameString());
            }
            pExtensions.free();

            return extensionProperties;
        }
    }

    private static List<VkQueueFamilyProperties> findQueueFamilyProperties(VkPhysicalDevice handle) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            List<VkQueueFamilyProperties> queueFamilyProperties = new ArrayList<>();

            IntBuffer pQueueFamilyPropertyCount = stack.callocInt(1);
            VK11.vkGetPhysicalDeviceQueueFamilyProperties(handle, pQueueFamilyPropertyCount, null);

            VkQueueFamilyProperties.Buffer pQueueFamilyProperties = VkQueueFamilyProperties.calloc(pQueueFamilyPropertyCount.get(0));
            VK11.vkGetPhysicalDeviceQueueFamilyProperties(handle, pQueueFamilyPropertyCount, pQueueFamilyProperties);
            while (pQueueFamilyProperties.hasRemaining()) {
                queueFamilyProperties.add(pQueueFamilyProperties.get());
            }
            pQueueFamilyProperties.free();

            return queueFamilyProperties;
        }
    }

    public boolean hasExtensions(List<String> extensions) {
        for(String extension : extensions) {
            for(String physicalDeviceExtensionProperty : physicalDeviceExtensionProperties) {
                if(physicalDeviceExtensionProperty.equals(extension)) {
                    return true;
                }
            }
        }
        return true;
    }

    public boolean supportsFeatures(VkPhysicalDeviceFeatures requiredFeatures) {
        //TODO: implement support for features check
        return true;
    }

    public int findGraphicsQueueFamily() {
        int graphicsQueueFamily = getGraphicsQueueFamily();
        if(graphicsQueueFamily == -1) {
            throw new RuntimeException("Failed to find a suitable graphics queue family");
        }
        return graphicsQueueFamily;
    }

    public boolean hasGraphicsQueue() {
        return getGraphicsQueueFamily() != -1;
    }

    private int getGraphicsQueueFamily() {
        for(int i = 0; i < queueFamilyProperties.size(); i++) {
            VkQueueFamilyProperties queueFamily = queueFamilyProperties.get(i);
            if((queueFamily.queueFlags() & VK11.VK_QUEUE_GRAPHICS_BIT) != 0) {
                return i;
            }
        }
        return -1;
    }

    public int findPresentQueueFamily(VulkanSurface surface) {
        int presentQueueFamily = getPresentQueueFamily(surface);
        if(presentQueueFamily == -1) {
            throw new RuntimeException("Failed to find a suitable present queue family");
        }
        return presentQueueFamily;
    }

    public boolean hasPresentQueue(VulkanSurface surface) {
        return getPresentQueueFamily(surface) != -1;
    }

    private int getPresentQueueFamily(VulkanSurface surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pSupported = stack.mallocInt(1);
            for(int i = 0; i < queueFamilyProperties.size(); i++) {
                VkQueueFamilyProperties queueFamily = queueFamilyProperties.get(i);
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(handle, i, surface.getHandle(), pSupported);
                if((queueFamily.queueFlags() & VK11.VK_QUEUE_GRAPHICS_BIT) != 0 && pSupported.get(0) == VK11.VK_TRUE) {
                    return i;
                }
            }
            return -1;
        }
    }

    public VkPhysicalDevice getHandle() {
        return handle;
    }

    public VkPhysicalDeviceProperties getPhysicalDeviceProperties() {
        return physicalDeviceProperties;
    }

    public VkPhysicalDeviceMemoryProperties getPhysicalDeviceMemoryProperties() {
        return physicalDeviceMemoryProperties;
    }

    public VkPhysicalDeviceFeatures getPhysicalDeviceFeatures() {
        return physicalDeviceFeatures;
    }

    public List<String> getPhysicalDeviceExtensionProperties() {
        return physicalDeviceExtensionProperties;
    }

    public List<VkQueueFamilyProperties> getQueueFamilyProperties() {
        return queueFamilyProperties;
    }
}
