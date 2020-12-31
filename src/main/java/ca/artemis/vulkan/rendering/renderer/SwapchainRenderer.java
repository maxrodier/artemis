package ca.artemis.vulkan.rendering.renderer;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.commands.PresentInfo;
import ca.artemis.vulkan.commands.SubmitInfo;
import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.rendering.Swapchain;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class SwapchainRenderer extends Renderer {

    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanFence renderFence;

    private final IntBuffer pImageIndex;
    private final SubmitInfo submitInfo;
    private final PresentInfo presentInfo;

    public SwapchainRenderer(VulkanDevice device, Swapchain swapchain, VulkanSemaphore waitSemaphore) {
        super(device, waitSemaphore);

        this.imageAcquiredSemaphore = new VulkanSemaphore(device);
        this.renderFence = new VulkanFence(device);

        this.pImageIndex = MemoryUtil.memCallocInt(1);
        this.submitInfo = new SubmitInfo(this.renderFence)
            .setWaitSemaphores(this.waitSemaphore, this.imageAcquiredSemaphore)
            .setWaitDstStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            .setSignalSemaphores(this.signalSemaphore);
        this.presentInfo = new PresentInfo()
            .setWaitSemaphores(this.signalSemaphore)
            .setSwapchains(swapchain)
            .setImageIndexPointer(pImageIndex);
    }

    public void destroy(VulkanDevice device) {
        this.presentInfo.destroy(device);
        this.submitInfo.destroy();
        MemoryUtil.memFree(this.pImageIndex);
        this.renderFence.destroy(device);
        this.imageAcquiredSemaphore.destroy(device);
        super.destroy(device);
    }

    @Override
    public void draw(VulkanDevice device, MemoryStack stack) {

    }

    public VulkanFence getRenderFence() {
        return renderFence;
    }
}
