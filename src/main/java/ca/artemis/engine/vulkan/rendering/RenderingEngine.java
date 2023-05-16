package ca.artemis.engine.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.framebuffer.Swapchain;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.engine.vulkan.rendering.renderers.SwapchainRenderer;

public class RenderingEngine {
    
    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    private final VulkanContext context;
    private final SwapchainRenderer swapchainRenderer;

    private List<VulkanSemaphore> imageAvailableSemaphores; //One per frame in flight
    private List<VulkanSemaphore> renderFinishedSemaphores; //One per frame in flight
    private List<VulkanFence> inFlightFences; //One per frame in flight

    private int currentFrame = 0;
    private boolean framebufferResized = false;

    public RenderingEngine(Window window) {
        this.context = new VulkanContext(window);
        this.swapchainRenderer = new SwapchainRenderer(this.context, window);
        createSynchronizationObjects(this.context.getDevice());
        setFramebufferSizeCallback(window);
    }

    public void destroy() {
        for(VulkanFence fence : inFlightFences) {
            fence.destroy(context.getDevice());
        }
        for(VulkanSemaphore semaphore : renderFinishedSemaphores) {
            semaphore.destroy(context.getDevice());
        }
        for(VulkanSemaphore semaphore : imageAvailableSemaphores) {
            semaphore.destroy(context.getDevice());
        }

        swapchainRenderer.destroy(context);
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

    private void setFramebufferSizeCallback(Window window) {
        GLFW.glfwSetFramebufferSizeCallback(window.getId(), new GLFWFramebufferSizeCallbackI() {
            @Override
            public void invoke(long window, int width, int height) {
                framebufferResized = true;
            }
        });
    }

    public FrameInfo prepareRender(MemoryStack stack, Window window) {
        FrameInfo frameInfo = new FrameInfo();
        frameInfo.frameIndex = currentFrame;
        frameInfo.imageAvailableSemaphore = imageAvailableSemaphores.get(currentFrame);
        frameInfo.renderFinishedSemaphore = renderFinishedSemaphores.get(currentFrame);
        frameInfo.inFlightFence = inFlightFences.get(currentFrame);

        frameInfo.pImageIndex = swapchainRenderer.acquireNextSwapchainImage(stack, context, window, frameInfo);
        return frameInfo;    
    }

    public void render(MemoryStack stack, Window window, FrameInfo frameInfo) {
        LongBuffer pWaitSemaphores = stack.callocLong(1);
        pWaitSemaphores.put(0, imageAvailableSemaphores.get(currentFrame).getHandle());

        IntBuffer pWaitDstStageMask = stack.callocInt(1);
        pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, renderFinishedSemaphores.get(currentFrame).getHandle());

        swapchainRenderer.draw(stack, context, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFences.get(currentFrame), frameInfo.pImageIndex.get(0), currentFrame);
        present(stack, window, swapchainRenderer.getSwapchain(), pSignalSemaphores, frameInfo.pImageIndex);

        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
    }

    private void present(MemoryStack stack, Window window, Swapchain swapchain, LongBuffer pSignalSemaphores, IntBuffer pImageIndex) {
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pSignalSemaphores);

        LongBuffer pSwapChains = stack.callocLong(1);
        pSwapChains.put(0, swapchainRenderer.getSwapchain().getHandle());

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(pSwapChains);
        presentInfo.pImageIndices(pImageIndex);

        int result = KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), presentInfo);
        if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR || framebufferResized) {
            System.out.println("Should recrate swapchain");
            framebufferResized = false;
            swapchainRenderer.recreateSwapchain(context, window);
        } else if (result != VK11.VK_SUCCESS) {
            throw new RuntimeException("failed to present swap chain image!");
        }
    }

    public VulkanContext getContext() {
        return context;
    }

    public SwapchainRenderer getSwapchainRenderer() {
        return swapchainRenderer;
    }
}
