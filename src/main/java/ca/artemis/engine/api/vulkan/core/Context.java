package ca.artemis.engine.api.vulkan.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;

public class Context {
    
    private static Context INSTANCE;

    private Context() {
        initialize();
    }

    public void destroy() {
        GLFW.glfwTerminate();
    }

    private static void initialize() {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver");
        }
    }

    public static Context getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Context();
        }
        return INSTANCE;
    }
}
