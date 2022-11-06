package ca.artemis.engine.vulkan.api.context;

import java.nio.LongBuffer;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice.QueueFamily;
import ca.artemis.engine.vulkan.rendering.Presenter;
import ca.artemis.engine.vulkan.rendering.SimpleDraw;

public class VulkanContext {
    
    private final VulkanInstance instance;
    private final VulkanSurface surface;
    private final DeviceManager deviceManager;
    private final VulkanPhysicalDevice physicalDevice;
    private final VulkanDevice device;
    private final VulkanMemoryAllocator memoryAllocator;

    private final long commandPool;

    private final SimpleDraw simpleDraw; //TODO: Refactor
    private final Presenter presenter; //TODO: Move this to resource management

    private static VulkanContext context = null;

    private VulkanContext(Window window) {
        this.instance = new VulkanInstance();
        this.surface = new VulkanSurface(instance, window);
        this.deviceManager = new DeviceManager(instance, surface);
        this.physicalDevice = deviceManager.selectPhysicalDevice();
        this.device = new VulkanDevice(physicalDevice);
        this.memoryAllocator = new VulkanMemoryAllocator(instance, physicalDevice, device);

        this.commandPool = createCommandPool(this);

        this.simpleDraw = new SimpleDraw(this);
        this.presenter = new Presenter(this, window);
    }

    public void destroy() {
        presenter.destroy(this);
        simpleDraw.destroy(this);
        memoryAllocator.destroy();
        device.destroy();
        deviceManager.destroy();
        surface.destroy(instance);
        instance.destroy();
    }
    
    private long createCommandPool(VulkanContext context) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(context.getSurface(), context.getPhysicalDevice().getHandle());
    
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            poolInfo.queueFamilyIndex(queueFamilies.get(0).getIndex());
    
            LongBuffer pCommandPool = stack.callocLong(1);
            if(VK11.vkCreateCommandPool(context.getDevice().getHandle(), poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                throw new RuntimeException("failed to create graphics command pool!");
            }
            return pCommandPool.get(0);
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

    public SimpleDraw getSimpleDraw() {
        return simpleDraw;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    public long getCommandPool() {
        return commandPool;
    }
}
