package ca.artemis.engine.api.vulkan.core;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

public class VulkanInstance {
    
    private final VkInstance handle;

    public VulkanInstance() {
        this.handle = createHandle();
    }

    public void destroy() {
        VK11.vkDestroyInstance(handle, null);
    }

    private VkInstance createHandle() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ppRequiredExtentions = GLFWVulkan.glfwGetRequiredInstanceExtensions();

    		ByteBuffer[] pEnabledLayerNames = {
    				stack.UTF8("VK_LAYER_KHRONOS_validation")
    		};
    		PointerBuffer ppEnabledLayerNames = stack.callocPointer(pEnabledLayerNames.length);
    		for(ByteBuffer pEnabledLayerName : pEnabledLayerNames) {
    			ppEnabledLayerNames.put(pEnabledLayerName);
            }
    		ppEnabledLayerNames.flip();

            VkApplicationInfo applicationInfo = VkApplicationInfo.calloc()
                .sType(VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8("My Vulkan Application"))
                .applicationVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(stack.UTF8("My Vulkan Engine"))
                .engineVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                .apiVersion(VK11.VK_API_VERSION_1_0);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc()
                .sType(VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(MemoryUtil.NULL)
                .pApplicationInfo(applicationInfo)
                .ppEnabledExtensionNames(ppRequiredExtentions)
                .ppEnabledLayerNames(ppEnabledLayerNames);
        
            PointerBuffer pHandle = stack.mallocPointer(1);
            int result = VK11.vkCreateInstance(createInfo, null, pHandle);
            if (result != VK11.VK_SUCCESS) {
                throw new RuntimeException("Failed to create vulkan instance: " + result);
            }

            VkInstance handle = new VkInstance(pHandle.get(), createInfo);

            applicationInfo.free();
            createInfo.free();

            return handle;
        }
    }

    public VkInstance getHandle() {
        return handle;
    }
}
