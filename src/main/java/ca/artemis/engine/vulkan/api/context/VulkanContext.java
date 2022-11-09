package ca.artemis.engine.vulkan.api.context;

import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice.QueueFamily;

public class VulkanContext {
    
    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanMemoryAllocator memoryAllocator;

    private final CommandPool commandPool;

    private static VulkanContext context = null;

    private VulkanContext(Window window) {
        this.instance = new VulkanInstance();
        this.surface = new VulkanSurface(instance, window);
        this.deviceManager = new DeviceManager(instance, surface);
        this.physicalDevice = deviceManager.selectPhysicalDevice();
        this.device = new VulkanDevice(physicalDevice);
        this.memoryAllocator = new VulkanMemoryAllocator(instance, physicalDevice, device);

        this.commandPool = createCommandPool(this);

    }

    public void destroy() {
        memoryAllocator.destroy();
        device.destroy();
        deviceManager.destroy();
        surface.destroy(instance);
        instance.destroy();
    }
    
    private CommandPool createCommandPool(VulkanContext context) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(context.getSurface(), context.getPhysicalDevice().getHandle());
    
            return new CommandPool(device, queueFamilies.get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        }
    }

    public static VulkanContext makeContextCurrent(Window window) {
        if(context != null) {
            throw new AssertionError("Vulkan Context already made current!");
        }
        context = new VulkanContext(window);
        return context;
    }

    public static VulkanContext getContext() {
        return context;
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

    public CommandPool getCommandPool() {
        return commandPool;
    }
}
