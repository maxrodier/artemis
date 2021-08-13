package ca.artemis.vulkan.api.framebuffer;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import ca.artemis.vulkan.api.context.VulkanDevice;

public class RenderPass {

    private final long handle;

    public RenderPass(long handle) {
        this.handle = handle;
    }

    public void destroy(VulkanDevice device) {
        VK11.vkDestroyRenderPass(device.getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    public static class Builder {
        
        private List<Attachement> colorAttachments = new ArrayList<>();
        private Attachement depthAttachement;

        public RenderPass build(VulkanDevice device) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                int size = depthAttachement == null ? colorAttachments.size() : colorAttachments.size() + 1;
                VkAttachmentDescription.Buffer pAttachments = VkAttachmentDescription.callocStack(size, stack);
                for(int i = 0; i < colorAttachments.size(); i++) {
                    buildAttachement(pAttachments.get(i), colorAttachments.get(0));
                }
                if(depthAttachement != null)
                    buildAttachement(pAttachments.get(colorAttachments.size()), depthAttachement);

                VkAttachmentReference.Buffer pColorAttachments = VkAttachmentReference.callocStack(colorAttachments.size(), stack);
                for(int i = 0; i < colorAttachments.size(); i++) {
                    buildAttachementReference(pColorAttachments.get(i), i, VK11.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }
                
                VkAttachmentReference pDepthAttachement = null;
                if(depthAttachement != null) {
                    pDepthAttachement = VkAttachmentReference.callocStack(stack);
                    buildAttachementReference(pDepthAttachement, colorAttachments.size(), VK11.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);    
                }

                //TODO : Externalize subpasses and subpasses dependencies 
                VkSubpassDescription.Buffer pSubpasses = VkSubpassDescription.callocStack(1, stack);
                pSubpasses.get(0)
                    .pipelineBindPoint(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(colorAttachments.size())
                    .pColorAttachments(pColorAttachments)
                    .pDepthStencilAttachment(pDepthAttachement);

                VkSubpassDependency.Buffer pDependencies = VkSubpassDependency.callocStack(1, stack)
                    .srcSubpass(VK11.VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)        	
                    .srcStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstAccessMask(VK11.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK11.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        
                
                VkRenderPassCreateInfo pCreateInfo = VkRenderPassCreateInfo.callocStack(stack)
                    .sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(pAttachments)
                    .pSubpasses(pSubpasses)
                    .pDependencies(pDependencies);

                LongBuffer pRenderPass = stack.callocLong(1);
                int error = VK11.vkCreateRenderPass(device.getHandle(), pCreateInfo, null, pRenderPass);
                if(error != VK11.VK_SUCCESS)
                    throw new AssertionError("Failed to create render pass");
                
                return new RenderPass(pRenderPass.get(0));
            }
        }

        private void buildAttachement(VkAttachmentDescription description, Attachement attachement) {
            description
                .format(attachement.getFormat())
                .samples(attachement.getSamples())
                .loadOp(attachement.getLoadOp())
                .storeOp(attachement.getStoreOp())
                .stencilLoadOp(attachement.getStencilLoadOp())
                .stencilStoreOp(attachement.getStencilStoreOp())
                .initialLayout(attachement.getInitialLayout())
                .finalLayout(attachement.getFinalLayout());
        }

        private void buildAttachementReference(VkAttachmentReference reference, int index, int layout) {
            reference
                .attachment(index)
                .layout(layout);
        }

        public Builder addColorAttachment(Attachement attachement) {
            colorAttachments.add(attachement);
            return this;
        }

        public Builder setDepthAttachment(Attachement attachement) {
            depthAttachement = attachement;
            return this;
        }
    }

    
    public static class Attachement {

        private int format;
        private int samples;
        private int loadOp;
        private int storeOp;
        private int stencilLoadOp;
        private int stencilStoreOp;
        private int initialLayout;
        private int finalLayout;

        public Attachement setFormat(int format) {
            this.format = format;
            return this;
        }

        private int getFormat() {
            return format;
        }

        public Attachement setSamples(int samples) {
            this.samples = samples;
            return this;
        }

        private int getSamples() {
            return samples;
        }

        public Attachement setLoadOp(int loadOp) {
            this.loadOp = loadOp;
            return this;
        }

        private int getLoadOp() {
            return loadOp;
        }

        public Attachement setStoreOp(int storeOp) {
            this.storeOp = storeOp;
            return this;
        }

        private int getStoreOp() {
            return storeOp;
        }

        public Attachement setStencilLoadOp(int stencilLoadOp) {
            this.stencilLoadOp = stencilLoadOp;
            return this;
        }

        private int getStencilLoadOp() {
            return stencilLoadOp;
        }

        public Attachement setStencilStoreOp(int stencilStoreOp) {
            this.stencilStoreOp = stencilStoreOp;
            return this;
        }

        private int getStencilStoreOp() {
            return stencilStoreOp;
        }

        public Attachement setInitialLayout(int initialLayout) {
            this.initialLayout = initialLayout;
            return this;
        }

        private int getInitialLayout() {
            return initialLayout;
        }

        public Attachement setFinalLayout(int finalLayout) {
            this.finalLayout = finalLayout;
            return this;
        }

        private int getFinalLayout() {
            return finalLayout;
        }
    }
}