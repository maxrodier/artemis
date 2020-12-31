package ca.artemis.vulkan.rendering.renderer;

import org.lwjgl.system.MemoryStack;

import ca.artemis.vulkan.api.memory.VulkanImageView;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.fbo.SceneFramebufferObject;
import ca.artemis.vulkan.fbo.FramebufferObject.Attachment;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class SceneRenderer extends Renderer {

    private final VulkanFence renderFence;
    private final SceneFramebufferObject sceneFramebufferObject;

    public SceneRenderer(VulkanContext context, VulkanSemaphore waitSemaphore) {
        super(context.getDevice(), waitSemaphore);
        this.renderFence = new VulkanFence(context.getDevice());
        this.sceneFramebufferObject = new SceneFramebufferObject(context);
    }

    public void destroy(VulkanContext context) {
        this.sceneFramebufferObject.destroy(context);
        this.renderFence.destroy(context.getDevice());
        super.destroy(context.getDevice());
    }

    @Override
    public void draw(VulkanDevice device, MemoryStack stack) {

    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }

    public VulkanImageView getDisplayImage() {
        return sceneFramebufferObject.getAttachment(Attachment.COLOR).getImageView();
    }
}
