package ca.artemis.vulkan.api.framebuffer;

import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.memory.VulkanImageBundle;

public class SceneFramebufferObject extends FramebufferObject {
    
    private final RenderPass renderPass;
    private VulkanFramebuffer framebuffer;

    public SceneFramebufferObject(VulkanContext context, int width, int height) {
        this.renderPass = new RenderPass.Builder()
            .addColorAttachment(new RenderPass.Attachement()
                .setFormat(VK11.VK_FORMAT_R16G16B16A16_SFLOAT)
                .setSamples(VK11.VK_SAMPLE_COUNT_1_BIT)
                .setLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .setStoreOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                .setStencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .setStencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .setInitialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                .setFinalLayout(VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL))
            .build(context.getDevice());

        this.regenerateFramebuffer(context, width, height);
    }

    public void destroy(VulkanContext context) {
        framebuffer.destroy(context.getDevice());
        renderPass.destroy(context.getDevice());
        super.destroy(context);
    }

    @Override
    public void regenerateFramebuffer(VulkanContext context, int width, int height) {
        if(framebuffer != null)
            framebuffer.destroy(context.getDevice());

        VulkanImageBundle colorAttachment = createColorAttachment(context, width, height, VK11.VK_FORMAT_R16G16B16A16_SFLOAT);
        attachments.put(Attachment.COLOR, colorAttachment);

        this.framebuffer = new VulkanFramebuffer.Builder()
            .addAttachement(attachments.get(Attachment.COLOR).getImageView())
            .setRenderPass(renderPass.getHandle())
            .setWidth(width)
            .setHeight(height)
            .setLayers(1)
            .build(context.getDevice());
    }

    public VulkanFramebuffer getFramebuffer() {
        return framebuffer;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }
}
