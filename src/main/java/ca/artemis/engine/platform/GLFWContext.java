package ca.artemis.engine.platform;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;

import ca.artemis.engine.core.Assertions;
import ca.artemis.engine.core.Logger;

public class GLFWContext {
    
    public static GLFWContext context = null;

    private GLFWContext() {
        Assertions.assertTrue(GLFW.glfwInit(), Assertions.Level.FATAL, "Failed to initialize GLFW!");
        Assertions.assertTrue(GLFWVulkan.glfwVulkanSupported(), Assertions.Level.FATAL, "Failed to find a compatible Vulkan installable client driver!");
    }

    public static void create() {
        if(context == null) {
            context = new GLFWContext();
            Logger.logTrace("GLFWContext created!");
        }
    }

    public static void destroy() {
        if(context != null) {
            GLFW.glfwTerminate();
            context = null;
            Logger.logTrace("GLFWContext destroyed!");
        }
    }
}
