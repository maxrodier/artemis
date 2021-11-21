package ca.artemis.vulkan.rendering;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.scene.SceneGraph;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.framebuffer.Swapchain;
import ca.artemis.vulkan.rendering.renderer.PostProcessingRenderer;
import ca.artemis.vulkan.rendering.renderer.SceneRenderer;
import ca.artemis.vulkan.rendering.renderer.SwapchainRenderer;

public class RenderingEngine {
    
    private final VulkanContext context;

    private final Swapchain swapchain;

    private final SceneRenderer sceneRenderer;
    private final PostProcessingRenderer postProcessingRenderer;
    private final SwapchainRenderer swapchainRenderer;

    private SceneGraph sceneGraph;

    public RenderingEngine() {
        this.context = VulkanContext.create();

        //Vector3f(36f/255f, 10f/255f, 48f/255f)
        Vector3f clearColour = new Vector3f(210f/255f, 210f/255f, 210f/255f);

        this.swapchain = new Swapchain();
        this.sceneRenderer = new SceneRenderer(clearColour);
        this.postProcessingRenderer = new PostProcessingRenderer(this.sceneRenderer.getSignalSemaphore(), this.sceneRenderer.getDisplayImage());
        this.swapchainRenderer = new SwapchainRenderer(this.swapchain, this.postProcessingRenderer.getSignalSemaphore(), this.postProcessingRenderer.getDisplayImage());
    }

    public void destroy() {
        this.swapchainRenderer.destroy();
        this.postProcessingRenderer.destroy();
        this.sceneRenderer.destroy();
        this.swapchain.destroy();
        this.context.destroy();
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

                sceneRenderer.getRenderFence().waitFor();

                sceneGraph.update(stack);
                sceneRenderer.update(sceneGraph);
                
                swapchainRenderer.getRenderFence().waitFor();

                sceneRenderer.draw(stack);
                postProcessingRenderer.draw(stack);
                swapchainRenderer.draw(stack);

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
