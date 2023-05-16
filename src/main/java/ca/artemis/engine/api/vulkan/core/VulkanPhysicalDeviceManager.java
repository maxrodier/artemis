package ca.artemis.engine.api.vulkan.core;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

public class VulkanPhysicalDeviceManager {
    
    private final List<VulkanPhysicalDevice> physicalDevices;

    public VulkanPhysicalDeviceManager(VulkanInstance instance) {
        this.physicalDevices = findPhysicalDevices(instance);
    }
    
    public void destroy() {
        physicalDevices.forEach(VulkanPhysicalDevice::destroy);
    }

    private static List<VulkanPhysicalDevice> findPhysicalDevices(VulkanInstance instance) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pPhysicalDeviceCount = stack.callocInt(1);
            int result = VK11.vkEnumeratePhysicalDevices(instance.getHandle(), pPhysicalDeviceCount, null);
            if (result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to find physical devices: " + result);
            }

            PointerBuffer pPhysicalDevices = stack.mallocPointer(pPhysicalDeviceCount.get(0));
            result = VK11.vkEnumeratePhysicalDevices(instance.getHandle(), pPhysicalDeviceCount, pPhysicalDevices);
            if (result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to find physical devices: " + result);
            }

            List<VulkanPhysicalDevice> physicalDevices = new ArrayList<>();
            while (pPhysicalDevices.hasRemaining()) {
                physicalDevices.add(new VulkanPhysicalDevice(new VkPhysicalDevice(pPhysicalDevices.get(), instance.getHandle())));
            }

            return physicalDevices;
        }
    }

    public VulkanPhysicalDevice selectPhysicalDevice(VulkanInstance instance, List<String> requiredExtensions, VkPhysicalDeviceFeatures requiredFeatures, VulkanSurface surface) {
        for (VulkanPhysicalDevice physicalDevice : physicalDevices) {
            // Check if the device has the required extensions
            if (physicalDevice.hasExtensions(requiredExtensions)) {
                // Check if the device supports the required features
                if (physicalDevice.supportsFeatures(requiredFeatures)) {
                    // Check if the device has the necessary queues
                    if (physicalDevice.hasGraphicsQueue() && physicalDevice.hasPresentQueue(surface)) {
                        // The device meets all the requirements
                        return physicalDevice;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to find a suitable physical device");
    }

    public List<VulkanPhysicalDevice> getPhysicalDevices() {
        return physicalDevices;
    }
}
