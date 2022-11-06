package ca.artemis.engine.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.framebuffer.Swapchain;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;

public class RenderingEngine {

    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    private SwapchainRenderer swapchainRenderer;

    private List<VulkanSemaphore> imageAvailableSemaphores; //One per frame in flight
    private List<VulkanSemaphore> renderFinishedSemaphores; //One per frame in flight
    private List<VulkanFence> inFlightFences; //One per frame in flight

    private int currentFrame = 0;

    public RenderingEngine() {
        VulkanContext context = VulkanContext.getContext();

        createSynchronizationObjects(context.getDevice());
    }

    public void destroy() {
        VulkanContext context = VulkanContext.getContext();

        for(VulkanFence fence : inFlightFences) {
            fence.destroy(context.getDevice());
        }
        for(VulkanSemaphore semaphore : renderFinishedSemaphores) {
            semaphore.destroy(context.getDevice());
        }
        for(VulkanSemaphore semaphore : imageAvailableSemaphores) {
            semaphore.destroy(context.getDevice());
        }
    }

    private void createSynchronizationObjects(VulkanDevice device) {
        this.imageAvailableSemaphores = new ArrayList<>();
        this.renderFinishedSemaphores = new ArrayList<>();
        this.inFlightFences = new ArrayList<>();

        for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            imageAvailableSemaphores.add(new VulkanSemaphore(device));
            renderFinishedSemaphores.add(new VulkanSemaphore(device));
            inFlightFences.add(new VulkanFence(device));
        }
    }

    public void render() {
        try(MemoryStack statck = MemoryStack.stackPush()) {
            VulkanContext context = VulkanContext.getContext();

            IntBuffer pImageIndex = swapchainRenderer.acquireNextSwapchainImage(statck, context, inFlightFences.get(currentFrame), imageAvailableSemaphores.get(currentFrame));

            LongBuffer pSignalSemaphores = statck. 
            context.getSimpleDraw().draw(stack, context, context.getPresenter(), context.getPresenter().getCurrentImageIndex());

            present(statck, context, null, pImageIndex);

            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
        }
    }

    private static void present(MemoryStack stack, VulkanContext context, Swapchain swapchain, LongBuffer pSignalSemaphores, IntBuffer pImageIndex) {
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pSignalSemaphores);

        LongBuffer pSwapChains = stack.callocLong(1);
        pSwapChains.put(0, swapchain.getHandle());

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(pSwapChains);
        presentInfo.pImageIndices(pImageIndex);

        int result = KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), presentInfo);
        if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR ) {
            System.out.println("Should recrate swapchain");
            //recreate swapchain
        } else if (result != VK11.VK_SUCCESS) {
            throw new RuntimeException("failed to present swap chain image!");
        }
    }
}
