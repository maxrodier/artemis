package ca.artemis.vulkan.api.framebuffer;

import org.lwjgl.vulkan.VK11;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.memory.VulkanImageBundle;

public class SceneFramebufferObject extends FramebufferObject {
    
    private final VulkanFramebuffer framebuffer;
    private final RenderPass renderPass;

    public SceneFramebufferObject() {
        VulkanImageBundle colorAttachment = createColorAttachment(Configuration.windowWidth, Configuration.windowHeight, VK11.VK_FORMAT_R16G16B16A16_SFLOAT);
        attachments.put(Attachment.COLOR, colorAttachment);

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
            .build();

        this.framebuffer = new VulkanFramebuffer.Builder()
            .addAttachement(attachments.get(Attachment.COLOR).getImageView())
            .setRenderPass(renderPass.getHandle())
            .setWidth(Configuration.windowWidth)
            .setHeight(Configuration.windowHeight)
            .setLayers(1)
            .build();
    }

    public void destroy() {
        framebuffer.destroy();
        renderPass.destroy();
        super.destroy();
    }

    public VulkanFramebuffer getFramebuffer() {
        return framebuffer;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }
}
