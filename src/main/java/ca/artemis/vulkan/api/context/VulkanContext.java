package ca.artemis.vulkan.api.context;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;

public class VulkanContext {
    
    private static VulkanContext context = null;

    private final GLFWWindow window;
    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanMemoryAllocator memoryAllocator;
    //private final CommandPool commandPool;

    private VulkanContext() {
        initialize();
        this.window = new GLFWWindow();
        this.instance = new VulkanInstance();
        this.surface = new VulkanSurface(instance, window);
        this.deviceManager = new DeviceManager(instance, surface);
        this.physicalDevice = deviceManager.selectPhysicalDevice();
        this.device = new VulkanDevice(physicalDevice);
        this.memoryAllocator = new VulkanMemoryAllocator(instance, physicalDevice, device);
    }

    public void destroy() {
        memoryAllocator.destroy();
        device.destroy();
        deviceManager.destroy();
        surface.destroy(instance);
        instance.destroy();
        window.destroy();
        GLFW.glfwTerminate();
    }

    public GLFWWindow getWindow() {
        return window;
    }

    public VulkanInstance getInstance() {
        return instance;
    }

    public VulkanSurface getSurface() {
        return surface;
    }

    public VulkanPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public VulkanDevice getDevice() {
        return device;
    }

    public VulkanMemoryAllocator getMemoryAllocator() {
        return memoryAllocator;
    }

    private void initialize() {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver");
        }
    }

    public static VulkanContext create() {
        if(context == null) {
            context = new VulkanContext();
        }
        return context;
    }
}
