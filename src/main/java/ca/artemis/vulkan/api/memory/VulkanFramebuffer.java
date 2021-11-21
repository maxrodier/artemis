package ca.artemis.vulkan.api.memory;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import ca.artemis.vulkan.api.context.VulkanContext;

public class VulkanFramebuffer {

    public final long handle;

    public VulkanFramebuffer(long handle) {
        this.handle = handle;
    }

    public void destroy() {
        VK11.vkDestroyFramebuffer(VulkanContext.getContext().getDevice().getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public static class Builder {

        private List<VulkanImageView> attachements = new ArrayList<>();
        private long renderPass;
        private int width;
        private int height;
        private int layers;

        public VulkanFramebuffer build() {
            try(MemoryStack stack = MemoryStack.stackPush()) {

                LongBuffer pAttachements = stack.callocLong(attachements.size());
                for(int i = 0; i < attachements.size(); i++) {
                    pAttachements.put(i, attachements.get(i).getHandle());
                }

                VkFramebufferCreateInfo pCreateInfo = VkFramebufferCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .pAttachments(pAttachements)
                    .renderPass(renderPass)
                    .width(width)
                    .height(height)
                    .layers(layers);
    
                LongBuffer pFramebuffer = stack.callocLong(1);
                int error = VK11.vkCreateFramebuffer(VulkanContext.getContext().getDevice().getHandle(), pCreateInfo, null, pFramebuffer);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create framebuffer");
    
                return new VulkanFramebuffer(pFramebuffer.get(0));
            }
        }

        public Builder addAttachement(VulkanImageView imageView) {
            attachements.add(imageView);
            return this;
        }
        
        public Builder setRenderPass(long renderPass) {
            this.renderPass = renderPass;
            return this; 
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setLayers(int layers) {
            this.layers = layers;
            return this;
        }
    }
}