package ca.artemis.engine.vulkan.api.context;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkQueue;

public class VulkanDevice {
    
    private final VkDevice handle;
    private final VkQueue graphicsQueue;

    public VulkanDevice(VulkanPhysicalDevice physicalDevice) {
        this.handle = createHandle(physicalDevice);
        this.graphicsQueue = createGraphicsQueue(this.handle, physicalDevice);
    }

    public void destroy() {
        VK11.vkDestroyDevice(handle, null);
    }

    public VkDevice getHandle() {
        return handle;
    }

    public VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    private static VkDevice createHandle(VulkanPhysicalDevice physicalDevice) {
    	try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDeviceQueueCreateInfo.Buffer ppQueueCreateInfos = VkDeviceQueueCreateInfo.callocStack(1, stack);
            ppQueueCreateInfos.get(0)
                .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .flags(0)
                .queueFamilyIndex(physicalDevice.getQueueFamilies().get(0).getIndex()) //TODO: Select right Queue family
                .pQueuePriorities(stack.callocFloat(1).put(1.0f).flip());
            
            ByteBuffer[] pEnabledExtensionNames = {
                stack.UTF8(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)
            };
            PointerBuffer ppEnabledExtensionNames = stack.callocPointer(pEnabledExtensionNames.length);
            for (ByteBuffer pEnabledExtensionName : pEnabledExtensionNames) {
            	ppEnabledExtensionNames.put(pEnabledExtensionName);
            }
            ppEnabledExtensionNames.flip();
            
            VkDeviceCreateInfo pCreateInfo = VkDeviceCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .flags(0)
                .pQueueCreateInfos(ppQueueCreateInfos)
                .ppEnabledExtensionNames(ppEnabledExtensionNames)
                .pEnabledFeatures(physicalDevice.getFeatures());
            
            PointerBuffer pDevice = stack.callocPointer(1);
            if(VK11.vkCreateDevice(physicalDevice.getHandle(), pCreateInfo, null, pDevice) != VK11.VK_SUCCESS) {
                throw new AssertionError("Failed to create logical device");
            }
            return new VkDevice(pDevice.get(0), physicalDevice.getHandle(), pCreateInfo);
    	}
    }

    private static VkQueue createGraphicsQueue(VkDevice handle, VulkanPhysicalDevice physicalDevice) {
    	try(MemoryStack stack = MemoryStack.stackPush()) {
	        PointerBuffer pGraphicsQueue = stack.callocPointer(1);
	        VK11.vkGetDeviceQueue(handle, physicalDevice.getQueueFamilies().get(0).getIndex(), 0, pGraphicsQueue);
	
	        return new VkQueue(pGraphicsQueue.get(0), handle);
    	}
    }
}