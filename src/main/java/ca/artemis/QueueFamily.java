package ca.artemis;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class QueueFamily {
        
    private final int index;
    private final int count;
    private final int flags;
    private final int supportPresentation;

    public QueueFamily(int index, VkQueueFamilyProperties properties, int supportPresentation) {
        this.index = index;
        this.count = properties.queueCount();
        this.flags = properties.queueFlags();
        this.supportPresentation = supportPresentation;
    }

    public int getIndex() {
        return index;
    }

    public int getCount() {
        return count;
    }

    public int getFlags() {
        return flags;
    }

    public int getSupportPresentation() {
        return supportPresentation;
    }

    public static List<QueueFamily> getQueueFamilies(long surface, VkPhysicalDevice physicalDevice) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pQueueFamilyPropertyCount = stack.callocInt(1);
            VK11.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
        
            VkQueueFamilyProperties.Buffer pQueueFamilyProperties= VkQueueFamilyProperties.callocStack(pQueueFamilyPropertyCount.get(0), stack);
            VK11.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, pQueueFamilyProperties);
            
            IntBuffer pSupportPresentation = stack.callocInt(1);

            List<QueueFamily> queueFamilies = new ArrayList<>();
            for(int i = 0; i < pQueueFamilyPropertyCount.get(0); i++) {
                int error = KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, pSupportPresentation);
                if(error != VK11.VK_SUCCESS) {
                    throw new AssertionError("Failed to find physical device surface support");
                }
                queueFamilies.add(new QueueFamily(i, pQueueFamilyProperties.get(i), pSupportPresentation.get(0)));
            }

            return queueFamilies;
        }
    }
}