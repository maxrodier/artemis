package ca.artemis.vulkan.context;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
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
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer ppRequiredExtentions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
    
    		ByteBuffer[] pEnabledLayerNames = {
    				stack.UTF8("VK_LAYER_KHRONOS_validation")
    		};
    		PointerBuffer ppEnabledLayerNames = stack.callocPointer(pEnabledLayerNames.length);
    		for(ByteBuffer pEnabledLayerName : pEnabledLayerNames)
    			ppEnabledLayerNames.put(pEnabledLayerName);
    		ppEnabledLayerNames.flip();
            
            VkApplicationInfo pApplicationInfo = VkApplicationInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8("Application Name"))
                .applicationVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(stack.UTF8("Engine Name"))
                .engineVersion(VK11.VK_MAKE_VERSION(1, 0, 0))
                .apiVersion(VK11.VK_API_VERSION_1_1);
    
            VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(pApplicationInfo)
                .ppEnabledLayerNames(ppEnabledLayerNames)
                .ppEnabledExtensionNames(ppRequiredExtentions);
    
            PointerBuffer pInstance = stack.callocPointer(1);
            int error = VK11.vkCreateInstance(pCreateInfo, null, pInstance);
            if(error != VK11.VK_SUCCESS)
                throw new AssertionError("Failed to create vulkan instance");
    
            return new VkInstance(pInstance.get(0), pCreateInfo);
        }
    }

    public VkInstance getHandle() {
        return handle;
    }
}