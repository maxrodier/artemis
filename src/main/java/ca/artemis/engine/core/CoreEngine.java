package ca.artemis.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.rendering.FrameInfo;
import ca.artemis.vulkan.rendering.RenderingEngine;

public class CoreEngine {
    
    private Game game;
    private GLFWWindow window;
    private RenderingEngine renderingEngine;

    public CoreEngine(Game game) {
        this.game = game;
    }

    public void run() {
        initialize();
        mainLoop();
        cleanup();
    }

    private void initialize() {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        this.window = new GLFWWindow();
        this.renderingEngine = new RenderingEngine(this.window);

        this.game.init(this.renderingEngine);
    }

    private void mainLoop() {
        while(!GLFW.glfwWindowShouldClose(window.getHandle())) {
            GLFW.glfwPollEvents();

            try(MemoryStack stack = MemoryStack.stackPush()) {
                FrameInfo frameInfo = renderingEngine.prepareRender(stack, window);
                if(frameInfo.pImageIndex == null) {
                    return;
                }
    
                game.update(stack, renderingEngine, frameInfo);
    
                renderingEngine.render(stack, window, frameInfo);
            }
        }
        VK11.vkDeviceWaitIdle(renderingEngine.getContext().getDevice().getHandle());
    }

    private void cleanup() {
        game.destroy(renderingEngine.getContext());
        renderingEngine.destroy();
        window.destroy();
        GLFW.glfwTerminate();
    }
}
