package ca.artemis.vulkan.rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.Configuration;
import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;
import ca.artemis.vulkan.rendering.renderer.SwapchainRenderer;

public class RenderingEngine {
    
    private final VulkanContext context;
    private final CommandPool commandPool;
    
    private final Swapchain swapchain;

    private final SceneRenderer sceneRenderer;
    private final SwapchainRenderer swapchainRenderer;



    public RenderingEngine(VulkanContext context) {
        this.context = context;
        this.commandPool = new CommandPool(this.context.getDevice(), this.context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.swapchain = new Swapchain(this.context, this.commandPool);

        this.sceneRenderer = new SceneRenderer(this.context, null);
        this.swapchainRenderer = new SwapchainRenderer(this.context.getDevice(), this.swapchain, sceneRenderer.getSignalSemaphore());
    }

    public void mainLoop() {
        long frame = 0;

        long lastTime = System.nanoTime();
        long currentTime;

        while (!context.getWindow().isCloseRequested()) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                currentTime = System.nanoTime();
                if(currentTime - lastTime >= 1000000000L) {
                    lastTime += 1000000000L;
                    System.out.println(frame);
                    frame = 0;
                }
                
                GLFW.glfwPollEvents();

                sceneRenderer.getRenderFence().waitFor(context.getDevice());

                //Update UBO HERE

                swapchainRenderer.getRenderFence().waitFor(context.getDevice());
                sceneRenderer.draw(context.getDevice(), stack);

                //DO POST PROCESSING HERE

                swapchainRenderer.draw(context.getDevice(), stack);

                frame++;
            }
        }

        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
    }
}
