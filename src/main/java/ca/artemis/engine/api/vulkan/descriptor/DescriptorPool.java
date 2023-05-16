package ca.artemis.engine.api.vulkan.descriptor;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;

public class DescriptorPool {

    private final long handle;

    private DescriptorPool(long handle) {
        this.handle = handle;
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyDescriptorPool(device.getHandle(), handle, null);   
    }

    public long getHandle() {
        return handle;
    }

    public static class Builder {

        private int maxSets;
        private List<PoolSize> poolSizes = new ArrayList<>();

        public DescriptorPool build(VulkanDevice device) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorPoolSize.Buffer pPoolSizes = VkDescriptorPoolSize.calloc(poolSizes.size(), stack);
                for(int i = 0; i < poolSizes.size(); i++) {
                    PoolSize poolSize = poolSizes.get(i);
                    pPoolSizes.get(i)
                        .type(poolSize.type)
                        .descriptorCount(poolSize.descriptorCount);
                }

                VkDescriptorPoolCreateInfo pCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(pPoolSizes)
                    .maxSets(maxSets);

                LongBuffer pDescriptorPool = MemoryUtil.memAllocLong(1);
                int result = VK11.vkCreateDescriptorPool(device.getHandle(), pCreateInfo, null, pDescriptorPool);
                if(result != VK11.VK_SUCCESS) {
                    throw new RuntimeException("Failed to create descriptor pool: " + result);
                }
                
                return new DescriptorPool(pDescriptorPool.get(0));
            }
        }

        public Builder addPoolSize(int type, int descriptorCount) {
            this.poolSizes.add(new PoolSize(type, descriptorCount));
            return this;
        }

        public Builder setMaxSets(int maxSets) {
            this.maxSets = maxSets;
            return this;
        }

        private class PoolSize {
            public final int type;
            public final int descriptorCount;

            public PoolSize(int type, int descriptorCount) {
                this.type = type;
                this.descriptorCount = descriptorCount;
            }
        }
    }
}