package ca.artemis.vulkan.rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.framebuffer.Swapchain;
import ca.artemis.vulkan.rendering.renderer.PostProcessingRenderer;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;
import ca.artemis.vulkan.rendering.renderer.SwapchainRenderer;

public class RenderingEngine {
    
    private final VulkanContext context;

    private Swapchain swapchain;
    private SceneRenderer sceneRenderer;
    private PostProcessingRenderer postProcessingRenderer;
    private SwapchainRenderer swapchainRenderer;

    private SceneGraph sceneGraph;

    public RenderingEngine(VulkanContext context) {
        this.context = context;
        this.createRenderingEngine();
    }

    public void destroy() {
        this.swapchainRenderer.destroy(context);
        this.postProcessingRenderer.destroy(context);
        this.sceneRenderer.destroy(context);
        this.swapchain.destroy(context);
    }

    public void createRenderingEngine() {
        this.swapchain = new Swapchain(context);
        this.sceneRenderer = new SceneRenderer(context);
        this.postProcessingRenderer = new PostProcessingRenderer(context, sceneRenderer.getSignalSemaphore(), this.sceneRenderer.getDisplayImage());
        this.swapchainRenderer = new SwapchainRenderer(context, this, this.swapchain, postProcessingRenderer.getSignalSemaphore(), this.postProcessingRenderer.getDisplayImage());
    }

    public void recreateRenderingEngine() {
        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

        context.updateSurfaceCapabilities();
        context.updateSurfaceFormats();
        swapchain.regenerateSwapchain(context);
        sceneRenderer.recreateRenderer(context);
        postProcessingRenderer.recreateRenderer(context);
        swapchainRenderer.recreateRenderer(context);
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

                sceneGraph.update(context, stack);
                sceneRenderer.update(context, sceneGraph);
                
                swapchainRenderer.getRenderFence().waitFor(context.getDevice());

                sceneRenderer.draw(context.getDevice(), stack);
                postProcessingRenderer.draw(context.getDevice(), stack);
                swapchainRenderer.draw(context.getDevice(), stack);

                frame++;
            }
        }

        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
    }



    public SceneRenderer getSceneRenderer() {
        return sceneRenderer;
    }

    public void setSceneGraph(SceneGraph sceneGraph) {
        this.sceneGraph = sceneGraph;
    }
}
