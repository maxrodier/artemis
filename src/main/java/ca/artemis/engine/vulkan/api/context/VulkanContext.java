package ca.artemis.engine.vulkan.api.context;

import org.lwjgl.glfw.GLFWVulkan;

import ca.artemis.engine.core.Window;

public class VulkanContext {

    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanMemoryAllocator memoryAllocator;
    //private final CommandPool commandPool;

    public VulkanContext(Window window) {
        initialize();
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
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver");
        }
    }
}
