package ca.artemis.engine.vulkan.api.context;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.framebuffer.SurfaceSupportDetails;

public class VulkanContext {

    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanMemoryAllocator memoryAllocator;
    private final CommandPool commandPool;
    private SurfaceSupportDetails surfaceSupportDetails;

    public VulkanContext(Window window) {
        initialize();
        this.instance = new VulkanInstance();
        this.surface = new VulkanSurface(instance, window);
        this.deviceManager = new DeviceManager(instance, surface);
        this.physicalDevice = deviceManager.selectPhysicalDevice();
        this.device = new VulkanDevice(physicalDevice);
        this.memoryAllocator = new VulkanMemoryAllocator(instance, physicalDevice, device);
        this.commandPool = new CommandPool(device, physicalDevice.getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.surfaceSupportDetails = new SurfaceSupportDetails(physicalDevice, surface, window);
    }

    public void destroy() {
        surfaceSupportDetails.destroy();
        commandPool.destroy(device);
        memoryAllocator.destroy();
        device.destroy();
        deviceManager.destroy();
        surface.destroy(instance);
        instance.destroy();
    }

    public void updateSurfaceSupportDetails(Window window) {
        this.surfaceSupportDetails.destroy();
        this.surfaceSupportDetails = new SurfaceSupportDetails(physicalDevice, surface, window);
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

    public SurfaceSupportDetails getSurfaceSupportDetails() {
        return surfaceSupportDetails;
    }

    public CommandPool getCommandPool() {
        return commandPool;
    }

    private void initialize() {
        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver");
        }
    }
}
