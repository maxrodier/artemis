package ca.artemis.engine.api.vulkan.descriptor;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.memory.VulkanBuffer;
import ca.artemis.engine.api.vulkan.memory.VulkanImageView;
import ca.artemis.engine.api.vulkan.memory.VulkanSampler;

public class DescriptorSet {
    
    private final long handle;

    public DescriptorSet(VulkanDevice device, DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout) {
        this.handle = createHandle(device, descriptorPool, descriptorSetLayout);
    }

    public void updateDescriptorBuffer(VulkanDevice device, VulkanBuffer buffer, long range, long offset, int binding, int descriptorType){
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorBufferInfo.Buffer pBufferInfos = VkDescriptorBufferInfo.calloc(1, stack)
                .buffer(buffer.getHandle())
                .offset(offset)
                .range(range);

            VkWriteDescriptorSet.Buffer pDescriptorWrites = VkWriteDescriptorSet.calloc(1, stack)
                .sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(handle)
                .dstBinding(binding)
                .dstArrayElement(0)
                .descriptorCount(1)
                .descriptorType(descriptorType)
                .pBufferInfo(pBufferInfos);

            VK11.vkUpdateDescriptorSets(device.getHandle(), pDescriptorWrites, null);
        }
    }

    public void updateDescriptorImageBuffer(VulkanDevice device, VulkanImageView imageView, VulkanSampler sampler, int imageLayout, int binding, int descriptorType){
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorImageInfo.Buffer pImageInfos = VkDescriptorImageInfo.calloc(1, stack)
                .imageView(imageView.getHandle())
                .sampler(sampler.getHandle())
                .imageLayout(imageLayout);

            VkWriteDescriptorSet.Buffer pDescriptorWrites = VkWriteDescriptorSet.calloc(1, stack)
                .sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(handle)
                .dstBinding(binding)
                .dstArrayElement(0)
                .descriptorCount(1)
                .descriptorType(descriptorType)
                .pImageInfo(pImageInfos);

            VK11.vkUpdateDescriptorSets(device.getHandle(), pDescriptorWrites, null);
        }
    }

    private long createHandle(VulkanDevice device, DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo pAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool.getHandle())
                .pSetLayouts(stack.callocLong(1).put(0, descriptorSetLayout.getHandle()));
            
            LongBuffer pDescriptorSet = stack.callocLong(1);
            int error = VK11.vkAllocateDescriptorSets(device.getHandle(), pAllocateInfo, pDescriptorSet);
            if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create descriptor set");

            return pDescriptorSet.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}