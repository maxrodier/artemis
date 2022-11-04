package ca.artemis.engine.platform.window;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import ca.artemis.engine.core.Assertions;
import ca.artemis.engine.core.Window;

public class GLFWWindow extends Window{
    
    private final long handle;
    
    protected GLFWWindow(long handle) {
        this.handle = handle;
    }
    
    @Override
    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    @Override
    public void update() {
        GLFW.glfwPollEvents();
    }

    @Override
    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(handle);
    }
    
    @Override
    public WindowSize getSize() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.callocInt(1), pHeight = stack.callocInt(1);
            GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
            return new WindowSize(pWidth.get(), pHeight.get());
        }
    }
    
    public static class Builder extends Window.Builder {

        @Override
        public Window build() {
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, isResizable? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

            long handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
            Assertions.assertFalse(handle == MemoryUtil.NULL, Assertions.Level.FATAL, "Failed to create GLFW window");
            return new GLFWWindow(handle);
        }
    }
}
