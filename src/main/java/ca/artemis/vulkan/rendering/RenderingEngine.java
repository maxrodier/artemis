package ca.artemis.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPresentInfoKHR;

import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.commands.SubmitInfo;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class RenderingEngine {
    
    private final VulkanContext context;
    private final CommandPool commandPool;
    private final Swapchain swapchain;
    private final SwapchainRenderer swapchainRenderer;
    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanSemaphore drawCompleteSemaphore;

    public RenderingEngine(VulkanContext context) {
        this.context = context;
        this.commandPool = new CommandPool(this.context.getDevice(), this.context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.swapchain = new Swapchain(this.context, this.commandPool);
        this.swapchainRenderer = new SwapchainRenderer(this.context, this.commandPool, this.swapchain);
        this.imageAcquiredSemaphore = new VulkanSemaphore(context.getDevice());
        this.drawCompleteSemaphore = new VulkanSemaphore(context.getDevice());
    }

    public void mainLoop() {
        int frame = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        
        LongBuffer pWaitSemaphores = MemoryUtil.memCallocLong(1);
        pWaitSemaphores.put(imageAcquiredSemaphore.getHandle());
        pWaitSemaphores.flip();
        
        LongBuffer pSignalSemaphores = MemoryUtil.memCallocLong(1);
        pSignalSemaphores.put(drawCompleteSemaphore.getHandle());
        pSignalSemaphores.flip();
        
        IntBuffer pWaitStages = MemoryUtil.memCallocInt(1);
        pWaitStages.put(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        pWaitStages.flip();

        VulkanFence fence = new VulkanFence(context.getDevice());
        SubmitInfo submitInfo = new SubmitInfo(fence)
            .setWaitSemaphores(pWaitSemaphores, 1)
            .setWaitDstStageMask(pWaitStages)
            .setSignalSemaphores(pSignalSemaphores);

        while (!context.getWindow().isCloseRequested()) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                frame++;
                currentTime = System.nanoTime();
                if(currentTime - lastTime >= 1000000000L) {
                    lastTime += 1000000000L;
                    System.out.println(frame);
                    frame = 0;
                }
                
                GLFW.glfwPollEvents();

                fence.waitFor(context.getDevice());
                
                IntBuffer pImageIndex = stack.callocInt(1);
                KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);

                submitInfo.setCommandBuffers(stack.callocPointer(1).put(swapchainRenderer.getDrawCommandBuffer(pImageIndex.get(0))).flip());
                submitInfo.submit(context.getDevice(), context.getDevice().getGraphicsQueue());

                LongBuffer pSwapchains = stack.callocLong(1);
                pSwapchains.put(swapchain.getHandle());
                pSwapchains.flip();

                VkPresentInfoKHR pPresentInfo = VkPresentInfoKHR.callocStack(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(pSignalSemaphores)
                    .swapchainCount(1)
                    .pSwapchains(pSwapchains)
                    .pImageIndices(pImageIndex);

                KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), pPresentInfo);
            }
        }

        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
        submitInfo.destroy();
        fence.destroy(context.getDevice());
        MemoryUtil.memFree(pWaitStages);
        MemoryUtil.memFree(pSignalSemaphores);
        MemoryUtil.memFree(pWaitSemaphores);
    }

    public void destroy() {
        drawCompleteSemaphore.destroy(context.getDevice());
        imageAcquiredSemaphore.destroy(context.getDevice());
        swapchainRenderer.destroy(context);
        swapchain.destroy(context);
        commandPool.destroy(context.getDevice());
    }
}