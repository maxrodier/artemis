package ca.artemis.engine.api.vulkan.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;

public class VulkanDevice {
    
    private final VulkanPhysicalDevice physicalDevice;
    private final int graphicsQueueIndex;
    private final int presentQueueIndex;
    private final VkDevice handle;
    private final VkQueue graphicsQueue;
    private final VkQueue presentQueue;
    
    public VulkanDevice(VulkanPhysicalDevice physicalDevice, VulkanSurface surface, List<String> requiredExtensions, VkPhysicalDeviceFeatures requiredFeatures) {
        this.physicalDevice = physicalDevice;
        this.graphicsQueueIndex = physicalDevice.findGraphicsQueueFamily();
        this.presentQueueIndex = physicalDevice.findPresentQueueFamily(surface);
        this.handle = createHandle(physicalDevice, requiredExtensions, requiredFeatures, graphicsQueueIndex, presentQueueIndex);
        this.graphicsQueue = createQueue(handle, physicalDevice, graphicsQueueIndex);
        this.presentQueue = createQueue(handle, physicalDevice, presentQueueIndex);
    }

    public void destroy() {
        VK11.vkDestroyDevice(handle, null);
    }

    private static VkDevice createHandle(VulkanPhysicalDevice physicalDevice, List<String> requiredExtensions, VkPhysicalDeviceFeatures requiredFeatures, int graphicsQueueIndex, int presentQueueIndex) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            Set<Integer> uniqueQueueFamilies = new HashSet<>();
            uniqueQueueFamilies.add(graphicsQueueIndex);
            uniqueQueueFamilies.add(presentQueueIndex);

            VkDeviceQueueCreateInfo.Buffer pQueueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.size(), stack);
            for(int queueFamilyIndex: uniqueQueueFamilies) {
                pQueueCreateInfos.put(VkDeviceQueueCreateInfo.calloc(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(queueFamilyIndex)
                    .pQueuePriorities(stack.floats(1.0f)));
            }
            pQueueCreateInfos.flip();

            PointerBuffer ppEnabledExtensionNames = stack.mallocPointer(requiredExtensions.size());
            for(int i = 0; i < requiredExtensions.size(); i++) {
                ppEnabledExtensionNames.put(i, stack.UTF8(requiredExtensions.get(i)));
            }

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pQueueCreateInfos(pQueueCreateInfos)
                .ppEnabledExtensionNames(ppEnabledExtensionNames)
                .pEnabledFeatures(requiredFeatures);

            PointerBuffer pDevice = stack.mallocPointer(1);
            int result = VK11.vkCreateDevice(physicalDevice.getHandle(), deviceCreateInfo, null, pDevice);
            if(result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device: " + result);
            }

            return new VkDevice(pDevice.get(0), physicalDevice.getHandle(), deviceCreateInfo);
        }
    }

    private static VkQueue createQueue(VkDevice handle, VulkanPhysicalDevice physicalDevice, int queueFamilyIndex) {
    	try(MemoryStack stack = MemoryStack.stackPush()) {
	        PointerBuffer pGraphicsQueue = stack.callocPointer(1);
	        VK11.vkGetDeviceQueue(handle, queueFamilyIndex, 0, pGraphicsQueue);
	
	        return new VkQueue(pGraphicsQueue.get(0), handle);
    	}
    }

    public void waitIdle() {
        int result = VK11.vkDeviceWaitIdle(handle);
        if(result != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to wait for device to become idle: " + result);
        }
    }

    public VulkanPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public int getGraphicsQueueIndex() {
        return graphicsQueueIndex;
    }

    public int getPresentQueueIndex() {
        return presentQueueIndex;
    }

    public VkDevice getHandle() {
        return handle;
    }

    public VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public VkQueue getPresentQueue() {
        return presentQueue;
    }
}
