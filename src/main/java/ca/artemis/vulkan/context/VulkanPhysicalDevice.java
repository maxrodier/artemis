package ca.artemis.vulkan.context;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class VulkanPhysicalDevice {

    private final VkPhysicalDevice physicalDevice;
    private final VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties;
    private final VkPhysicalDeviceProperties physicalDeviceProperties;
    private final VkPhysicalDeviceFeatures physicalDeviceFeatures;
    private final List<QueueFamily> queueFamilies;

    public VulkanPhysicalDevice(VulkanInstance instance, VulkanSurface surface, long physicalDevice) {
        this.physicalDevice = new VkPhysicalDevice(physicalDevice, instance.getHandle());
        this.physicalDeviceMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        this.physicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
        this.physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
        this.queueFamilies = QueueFamily.getQueueFamilies(surface, this.physicalDevice);

        VK11.vkGetPhysicalDeviceMemoryProperties(this.physicalDevice, this.physicalDeviceMemoryProperties);
        VK11.vkGetPhysicalDeviceProperties(this.physicalDevice, this.physicalDeviceProperties);
        VK11.vkGetPhysicalDeviceFeatures(this.physicalDevice, this.physicalDeviceFeatures);
    }

    public void destroy() {
        physicalDeviceFeatures.free();
        physicalDeviceProperties.free();
        physicalDeviceMemoryProperties.free();
    }

    public int findMemoryType(VkMemoryRequirements memoryRequirements, int memoryPropertyFlags) {
    	int bits = memoryRequirements.memoryTypeBits();
    	for(int i = 0; i < physicalDeviceMemoryProperties.memoryTypeCount(); i++) {
    		if((bits & 1) == 1) {
	    		if((physicalDeviceMemoryProperties.memoryTypes(i).propertyFlags() & memoryPropertyFlags) == memoryPropertyFlags) {
	    			return i;
	    		}
    		}
    		bits >>= 1;
    	}

    	throw new IllegalStateException("No suitable memory was found");
    }

    public VkPhysicalDevice getHandle() {
        return physicalDevice;
    }

    public VkPhysicalDeviceMemoryProperties getMemoryProperties() {
        return physicalDeviceMemoryProperties;
    }

    public VkPhysicalDeviceProperties getProperties() {
        return physicalDeviceProperties;
    }

    public VkPhysicalDeviceFeatures getFeatures() {
        return physicalDeviceFeatures;
    }

    public List<QueueFamily> getQueueFamilies() {
        return queueFamilies;
    }

    public static class QueueFamily {
        
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

        public static List<QueueFamily> getQueueFamilies(VulkanSurface surface, VkPhysicalDevice physicalDevice) {
        	try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pQueueFamilyPropertyCount = stack.callocInt(1);
                VK11.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
            
                VkQueueFamilyProperties.Buffer pQueueFamilyProperties= VkQueueFamilyProperties.callocStack(pQueueFamilyPropertyCount.get(0), stack);
                VK11.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, pQueueFamilyProperties);
                
                IntBuffer pSupportPresentation = stack.callocInt(1);

                List<QueueFamily> queueFamilies = new ArrayList<>();
                for(int i = 0; i < pQueueFamilyPropertyCount.get(0); i++) {
                    int error = KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface.getHandle(), pSupportPresentation);
                    if(error != VK11.VK_SUCCESS) {
                        throw new AssertionError("Failed to find physical device surface support");
                    }
                    queueFamilies.add(new QueueFamily(i, pQueueFamilyProperties.get(i), pSupportPresentation.get(0)));
                }

                return queueFamilies;
        	}
        }
    }
}