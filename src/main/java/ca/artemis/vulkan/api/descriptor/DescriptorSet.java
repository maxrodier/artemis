package ca.artemis.vulkan.api.descriptor;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.api.memory.VulkanSampler;

public class DescriptorSet {
    
    private final long handle;

    public DescriptorSet(DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout) {
        this.handle = createHandle(descriptorPool, descriptorSetLayout);
    }

    public void updateDescriptorBuffer(VulkanBuffer buffer, long range, long offset, int binding, int descriptorType){
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorBufferInfo.Buffer pBufferInfos = VkDescriptorBufferInfo.callocStack(1, stack)
                .buffer(buffer.getHandle())
                .offset(offset)
                .range(range);

            VkWriteDescriptorSet.Buffer pDescriptorWrites = VkWriteDescriptorSet.callocStack(1, stack)
                .sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(handle)
                .dstBinding(binding)
                .dstArrayElement(0)
                .descriptorCount(1)
                .descriptorType(descriptorType)
                .pBufferInfo(pBufferInfos);

            VK11.vkUpdateDescriptorSets(VulkanContext.getContext().getDevice().getHandle(), pDescriptorWrites, null);
        }
    }

    public void updateDescriptorImageBuffer(VulkanImageView imageView, VulkanSampler sampler, int imageLayout, int binding, int descriptorType){
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorImageInfo.Buffer pImageInfos = VkDescriptorImageInfo.callocStack(1, stack)
                .imageView(imageView.getHandle())
                .sampler(sampler.getHandle())
                .imageLayout(imageLayout);

            VkWriteDescriptorSet.Buffer pDescriptorWrites = VkWriteDescriptorSet.callocStack(1, stack)
                .sType(VK11.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(handle)
                .dstBinding(binding)
                .dstArrayElement(0)
                .descriptorCount(1)
                .descriptorType(descriptorType)
                .pImageInfo(pImageInfos);

            VK11.vkUpdateDescriptorSets(VulkanContext.getContext().getDevice().getHandle(), pDescriptorWrites, null);
        }
    }

    private long createHandle(DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetAllocateInfo pAllocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
                .sType(VK11.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool.getHandle())
                .pSetLayouts(stack.callocLong(1).put(0, descriptorSetLayout.getHandle()));
            
            LongBuffer pDescriptorSet = stack.callocLong(1);
            int error = VK11.vkAllocateDescriptorSets(VulkanContext.getContext().getDevice().getHandle(), pAllocateInfo, pDescriptorSet);
            if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create descriptor set");

            return pDescriptorSet.get(0);
        }
    }

    public long getHandle() {
        return handle;
    }
}