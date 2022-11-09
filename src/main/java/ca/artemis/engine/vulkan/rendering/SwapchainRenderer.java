package ca.artemis.engine.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.framebuffer.SurfaceSupportDetails;
import ca.artemis.engine.vulkan.api.framebuffer.Swapchain;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;

public class SwapchainRenderer extends Renderer {
    
    private SurfaceSupportDetails surfaceSupportDetails;
    private Swapchain swapchain;
    
    public SwapchainRenderer(VulkanContext context, Window window) {
        this.surfaceSupportDetails = new SurfaceSupportDetails(context.getPhysicalDevice(), context.getSurface(), window); //TODO: I don't like that we need to pass a window here //Could we move code to surface??
        super.renderPass = createRenderPass(context.getDevice(), surfaceSupportDetails); 
        this.swapchain = new Swapchain(context.getDevice(), context.getSurface(), super.renderPass, surfaceSupportDetails);
    }

    public void destroy(VulkanDevice device) {
        swapchain.destroy(device);
        renderPass.destroy(device);
        surfaceSupportDetails.destroy();
    }

    private RenderPass createRenderPass(VulkanDevice device, SurfaceSupportDetails surfaceSupportDetails) {
        return new RenderPass.Builder()
            .addColorAttachment(new RenderPass.Attachement()
                .setFormat(surfaceSupportDetails.getSurfaceFormat().format())
                .setSamples(VK11.VK_SAMPLE_COUNT_1_BIT)
                .setLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .setStoreOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                .setStencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .setStencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .setInitialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                .setFinalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
            .build(device);
    }

    public SurfaceSupportDetails getSurfaceSupportDetails() {
        return surfaceSupportDetails;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public Swapchain getSwapchain() {
        return swapchain;
    }

    public void regenerateRenderer(VulkanContext context, Window window) { //TODO: Verify renderpass compatibilty
        swapchain.destroy(context.getDevice());
        renderPass.destroy(context.getDevice());
        surfaceSupportDetails.destroy();

        this.surfaceSupportDetails = new SurfaceSupportDetails(context.getPhysicalDevice(), context.getSurface(), window);
        this.renderPass = createRenderPass(context.getDevice(), surfaceSupportDetails);
        this.swapchain = new Swapchain(context.getDevice(), context.getSurface(), renderPass, surfaceSupportDetails);
    }

    @Override
    public void draw(MemoryStack stack, VulkanContext context, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int imageIndex, int frameIndex) {
        VulkanFramebuffer framebuffer = swapchain.getFramebuffer(imageIndex);
        recordPrimaryCommandBuffer(stack, framebuffer, framebuffer.getWidth(), framebuffer.getHeight(), frameIndex);
        submitPrimaryCommandBuffer(stack, context, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFence, frameIndex);
    }

    public IntBuffer acquireNextSwapchainImage(MemoryStack stack, VulkanContext context, VulkanFence inFlightFence, VulkanSemaphore imageAvailableSemaphore) {
        LongBuffer pFence = stack.callocLong(1);
        pFence.put(0, inFlightFence.getHandle());
        VK11.vkWaitForFences(context.getDevice().getHandle(), pFence, true, Long.MAX_VALUE);

        IntBuffer pImageIndex = stack.callocInt(1);
        int result = KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAvailableSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
            System.out.println("Should recrate swapchain");
            //recreate swapchain
            //return
        } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("Failed to acquire swapchain image!");
        }

        VK11.vkResetFences(context.getDevice().getHandle(), pFence);

        return pImageIndex;
    }
}
