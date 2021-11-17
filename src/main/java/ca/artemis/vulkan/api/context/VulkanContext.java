package ca.artemis.vulkan.api.context;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import ca.artemis.vulkan.api.commands.CommandPool;

public class VulkanContext {

    private static VulkanContext context = null;

    private final GLFWWindow window;
    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final CommandPool commandPool;
    private final VulkanMemoryAllocator memoryAllocator;
    private final VkSurfaceCapabilitiesKHR surfaceCapabilities;
    private final VkSurfaceFormatKHR.Buffer surfaceFormats;
    
    private VulkanContext() {
        this.initialize();
        this.window = new GLFWWindow();
        this.instance = new VulkanInstance();
        this.surface = new VulkanSurface(this.instance, this.window);
        this.deviceManager = new DeviceManager(this.instance, this.surface);
        this.physicalDevice = this.deviceManager.getPhysicalDevice();
        this.device = new VulkanDevice(this.physicalDevice);
        this.commandPool = new CommandPool(this.device, this.physicalDevice.getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
        this.memoryAllocator = new VulkanMemoryAllocator(this.instance, this.physicalDevice, this.device);
        this.surfaceCapabilities = VulkanSurface.fetchSurfaceCapabilities(this.physicalDevice, this.surface);
        this.surfaceFormats = VulkanSurface.fetchSurfaceFormats(this.physicalDevice, this.surface);
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

    public CommandPool getCommandPool() {
        return commandPool;
    }

    public VulkanMemoryAllocator getMemoryAllocator() {
        return memoryAllocator;
    }

    public VkSurfaceCapabilitiesKHR getSurfaceCapabilities() {
        return surfaceCapabilities;
    }

    public VkSurfaceFormatKHR.Buffer getSurfaceFormats() {
        return surfaceFormats;
    }

    public static VulkanContext create() {
        if(context == null)
            context = new VulkanContext();
        return context;
    }

    public void destroy() {
        surfaceFormats.free();
        surfaceCapabilities.free();
        memoryAllocator.destroy();
        commandPool.destroy(device);
        device.destroy();
        deviceManager.destroy();
        surface.destroy(instance);
        instance.destroy();
        window.destroy();
    }
}