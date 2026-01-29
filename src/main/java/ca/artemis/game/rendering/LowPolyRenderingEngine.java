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
import ca.artemis.engine.rendering.ui.UiRenderer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;

public class LowPolyRenderingEngine extends RenderingEngine {
    
    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    private static LowPolyRenderingEngine currentInstance;

    private final VulkanContext context;

    private final EntityRenderer entityRenderer;
    private final UiRenderer uiRenderer;
    private final SwapchainRenderer swapchainRenderer;

    private int currentFrameIndex;

    private int lastWindowWidth = -1;
    private int lastWindowHeight = -1;

    private LowPolyRenderingEngine() {
        this.context = LowPolyEngine.instance().getContext();
        this.entityRenderer = new EntityRenderer(context.getDevice(), context.getPhysicalDevice(), null);
        this.uiRenderer = new UiRenderer(context.getDevice(), context.getPhysicalDevice(), entityRenderer);
        this.swapchainRenderer = new SwapchainRenderer(context.getDevice(), context.getPhysicalDevice(), uiRenderer);
        
        // Add resize listener for window resize events
        addResizeListener(context, LowPolyEngine.instance().getWindow(), this);
    }

    @Override
    public void close() throws Exception {
        swapchainRenderer.close();
        uiRenderer.close();
        entityRenderer.close();
    }

    private static void addResizeListener(VulkanContext context, Window window, LowPolyRenderingEngine engine) {
        window.addResizeListener(() -> {
            // Trigger regeneration when window is resized
            engine.regenerate();
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
        uiRenderer.render(stack, context);
        swapchainRenderer.render(stack, context);
        present(stack);

        currentFrameIndex = (currentFrameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
        entityRenderer.getRenderData().setFrameIndex(currentFrameIndex);
        uiRenderer.getRenderData().setFrameIndex(currentFrameIndex);
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
        
        // Check if window size has actually changed
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.callocInt(1);
            IntBuffer height = stack.callocInt(1);
            GLFW.glfwGetFramebufferSize(window.getId(), width, height);
            
            int currentWidth = width.get(0);
            int currentHeight = height.get(0);
            
            // Skip regeneration if size hasn't changed
            if (currentWidth == lastWindowWidth && currentHeight == lastWindowHeight) {
                return;
            }
            
            lastWindowWidth = currentWidth;
            lastWindowHeight = currentHeight;
        }
        
        System.out.println("Regenerating rendering resources due to resize");

        // Wait for device to be idle before recreation
        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

        // Wait for window to have valid dimensions
        waitForValidWindowSize(window);

        // Update surface support details with new window size
        context.updateSurfaceSupportDetails(window);
        
        // Regenerate all rendering components
        try {
            entityRenderer.regenerate();
            uiRenderer.regenerate();
            swapchainRenderer.regenerate();
            System.out.println("Successfully regenerated rendering resources");
        } catch (Exception e) {
            System.err.println("Failed to regenerate rendering resources: " + e.getMessage());
            throw new RuntimeException("Resize regeneration failed", e);
        }
    }

    private void waitForValidWindowSize(Window window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.callocInt(1);
            IntBuffer height = stack.callocInt(1);
            
            // Keep checking until window has valid (non-zero) dimensions
            while (!window.isCloseRequested()) {
                width.clear();
                height.clear();
                GLFW.glfwGetFramebufferSize(window.getId(), width, height);
                
                // If dimensions are valid, break out of loop
                if (width.get(0) > 0 && height.get(0) > 0) {
                    break;
                }
                
                // Wait for window events and check again
                GLFW.glfwWaitEvents();
            }
        }
    }

    public VulkanContext getContext() {
        return context;
    }

    public EntityRenderer getEntityRenderer() {
        return entityRenderer;
    }

    public UiRenderer getUiRenderer() {
        return uiRenderer;
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
