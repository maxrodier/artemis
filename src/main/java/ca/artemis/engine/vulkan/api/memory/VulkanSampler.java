package ca.artemis.engine.vulkan.api.memory;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;

public class VulkanSampler {

    private final long handle;

    public VulkanSampler(long handle) {
        this.handle = handle;
    }

    public long getHandle() {
        return handle;
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroySampler(device.getHandle(), handle, null);
    }
    
    public static class Builder {

    	private int magFilter = VK11.VK_FILTER_LINEAR;
    	private int minFilter = VK11.VK_FILTER_LINEAR;
    	private int addressModeU = VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT;
    	private int addressModeV = VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT;
    	private int addressModeW = VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT;
    	private boolean anisotropyEnable = false;
    	private int maxAnisotropy = 1;
    	private int borderColor = VK11.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
    	private boolean unnormalizedCoordinates = false;
    	private boolean compareEnable = false;
    	private int compareOp = VK11.VK_COMPARE_OP_ALWAYS;
    	private int mipmapMode = VK11.VK_SAMPLER_MIPMAP_MODE_LINEAR;
    	private float mipLodBias = 0.0f;
    	private int minLod = 0;
        private int maxLod = 1;
        
        public VulkanSampler build(VulkanDevice device) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.create()
                    .sType(VK11.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                    .magFilter(magFilter)
                    .minFilter(minFilter)
                    .addressModeU(addressModeU)
                    .addressModeV(addressModeV)
                    .addressModeW(addressModeW)
                    .anisotropyEnable(anisotropyEnable)
                    .maxAnisotropy(maxAnisotropy)
                    .borderColor(borderColor)
                    .unnormalizedCoordinates(unnormalizedCoordinates)
                    .compareEnable(compareEnable)
                    .compareOp(compareOp)
                    .mipmapMode(mipmapMode)
                    .mipLodBias(mipLodBias)
                    .minLod(minLod)
                    .maxLod(maxLod);

                LongBuffer pSampler = stack.callocLong(1);
                int error =  VK11.vkCreateSampler(device.getHandle(), samplerInfo, null, pSampler);
                if(error != VK11.VK_SUCCESS) 
                    throw new AssertionError("Failed to create pipeline layout");

                return new VulkanSampler(pSampler.get(0));
            }
        }

        public Builder setAddressModeU(int addressModeU) {
            this.addressModeU = addressModeU;
            return this;
        }

        public Builder setAddressModeV(int addressModeV) {
            this.addressModeV = addressModeV;
            return this;
        }

        public Builder setAddressModeW(int addressModeW) {
            this.addressModeW = addressModeW;
            return this;
        }

        public Builder setMaxLod(int maxLoad) {
            this.maxLod = maxLoad;
            return this;
        }
    }
}