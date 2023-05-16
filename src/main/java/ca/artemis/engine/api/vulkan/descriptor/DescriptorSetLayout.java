package ca.artemis.engine.api.vulkan.descriptor;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;

public class DescriptorSetLayout {

    private final long handle;

    private DescriptorSetLayout(long handle) {
        this.handle = handle;
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyDescriptorSetLayout(device.getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public static class Builder {

        private List<DescriptorSetLayoutBinding> descriptorSetLayoutBindings = new ArrayList<>();

        public DescriptorSetLayout build(VulkanDevice device) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorSetLayoutBinding.Buffer pLayoutBindings = VkDescriptorSetLayoutBinding.calloc(descriptorSetLayoutBindings.size(), stack);
                for(int i = 0; i < descriptorSetLayoutBindings.size(); i++) {
                    DescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get(i);
                    pLayoutBindings.get(i)
                        .binding(descriptorSetLayoutBinding.binding)
                        .descriptorType(descriptorSetLayoutBinding.descriptorType)
                        .descriptorCount(descriptorSetLayoutBinding.descriptorCount)
                        .stageFlags(descriptorSetLayoutBinding.stageFlags);
                }

                VkDescriptorSetLayoutCreateInfo pLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                    .pBindings(pLayoutBindings);

                LongBuffer pDescriptorSetLayout = stack.callocLong(1);
                int result = VK11.vkCreateDescriptorSetLayout(device.getHandle(), pLayoutCreateInfo, null, pDescriptorSetLayout);
                if(result != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create descriptor set layout: " + result);
                }
    
                return new DescriptorSetLayout(pDescriptorSetLayout.get(0));
            }
        }

        public Builder addDescriptorSetLayoutBinding(int binding, int descriptorType, int descriptorCount, int stageFlags) {
            this.descriptorSetLayoutBindings.add(new DescriptorSetLayoutBinding(binding, descriptorType, descriptorCount, stageFlags));
            return this;
        }

        private class DescriptorSetLayoutBinding {

            private int binding;
            private int descriptorType;
            private int descriptorCount;
            private int stageFlags;

            public DescriptorSetLayoutBinding(int binding, int descriptorType, int descriptorCount, int stageFlags) {
                this.binding = binding;
                this.descriptorType = descriptorType;
                this.descriptorCount = descriptorCount;
                this.stageFlags = stageFlags;
            }
        }
    }
}