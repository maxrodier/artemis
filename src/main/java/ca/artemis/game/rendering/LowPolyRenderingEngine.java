package ca.artemis.game.rendering;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.core.Window;
import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.rendering.entity.EntityRenderer;
import ca.artemis.engine.rendering.swapchain.SwapchainRenderer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;

public class LowPolyRenderingEngine extends RenderingEngine {
    
    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    private static LowPolyRenderingEngine currentInstance;

    private final VulkanContext context;

    private final EntityRenderer entityRenderer;
    private final SwapchainRenderer swapchainRenderer;

    private int currentFrameIndex;

    private LowPolyRenderingEngine() {
        this.context = LowPolyEngine.instance().getContext();
        this.entityRenderer = new EntityRenderer(context.getDevice(), context.getPhysicalDevice(), null);
        this.swapchainRenderer = new SwapchainRenderer(context.getDevice(), context.getPhysicalDevice(), context.getSurface(), context.getSurfaceSupportDetails(), entityRenderer);
        
        //TempStuff
        addResizeListener(context, LowPolyEngine.instance().getWindow(), this);
    }

    @Override
    public void close() throws Exception {
        swapchainRenderer.close();
        entityRenderer.close();
    }

    //TempStuff
    private static void addResizeListener(VulkanContext context, Window window, LowPolyRenderingEngine lowPolyRenderingEngine) {
        window.addResizeListener(() -> {
            //lowPolyRenderingEngine.regenerate();
        });
    }

    public boolean update(MemoryStack stack) {
        if(swapchainRenderer.acquireNextSwapchainImage(stack, context) == KHRSwapchain.VK_SUBOPTIMAL_KHR){
            regenerate();
            return false;
        }
        return true;
    }

    public void render(MemoryStack stack) {
        entityRenderer.render(stack, context);  
        swapchainRenderer.render(stack, context);
        present(stack);

        currentFrameIndex = (currentFrameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
        entityRenderer.getRenderData().setFrameIndex(currentFrameIndex);
        swapchainRenderer.getRenderData().setFrameIndex(currentFrameIndex);
    }

    private void present(MemoryStack stack) {
        int result = swapchainRenderer.present(stack, context);
        if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            regenerate();
        }
    }

    private void regenerate() {
        VulkanContext context = LowPolyEngine.instance().getContext();
        Window window = LowPolyEngine.instance().getWindow();
        
        System.out.println("Regenerate");

        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

        while(window.isResizing()) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.callocInt(1), height = stack.callocInt(1);
                GLFW.glfwGetFramebufferSize(window.getId(), width, height);
                while (width.get(0) == 0 || height.get(0) == 0) {
                    width.clear(); height.clear();
                    GLFW.glfwGetFramebufferSize(window.getId(), width, height);
                    GLFW.glfwWaitEvents();
                }
            }
            window.update();
        }

        context.updateSurfaceSupportDetails(window);
        entityRenderer.regenerate();
        swapchainRenderer.regenerate();
    }

    public VulkanContext getContext() {
        return context;
    }

    public EntityRenderer getEntityRenderer() {
        return entityRenderer;
    }

    public SwapchainRenderer getSwapchainRenderer() {
        return swapchainRenderer;
    }
    
    public static LowPolyRenderingEngine instance() {
        if(currentInstance == null) {
            currentInstance = new LowPolyRenderingEngine();
        }

        return currentInstance;
    }
}
