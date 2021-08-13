package ca.artemis.vulkan.api.context;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

public class DeviceManager {

    private List<VulkanPhysicalDevice> physicalDevices;

    public DeviceManager(VulkanInstance instance, VulkanSurface surface) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pPhysicalDeviceCount = stack.callocInt(1);
            VK11.vkEnumeratePhysicalDevices(instance.getHandle(), pPhysicalDeviceCount, null);
    
            PointerBuffer pPhysicalDevices = stack.callocPointer(pPhysicalDeviceCount.get(0));
            VK11.vkEnumeratePhysicalDevices(instance.getHandle(), pPhysicalDeviceCount, pPhysicalDevices);
            
            this.physicalDevices = new ArrayList<>();
            for(int i = 0; i < pPhysicalDeviceCount.get(0); i++) {
                this.physicalDevices.add(new VulkanPhysicalDevice(instance, surface, pPhysicalDevices.get(i)));
            }
        }
    }

    public void destroy() {
        for(VulkanPhysicalDevice physicalDevice : physicalDevices) {
            physicalDevice.destroy();
        }
    }

    public VulkanPhysicalDevice getPhysicalDevice() {
        return physicalDevices.get(0); //TODO: Select right Physical Device
    }
}