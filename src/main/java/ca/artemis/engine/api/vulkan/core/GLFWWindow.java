package ca.artemis.engine.api.vulkan.core;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import ca.artemis.engine.util.Timer;

public class GLFWWindow {
    
    private static final long RESIZE_DEBOUNCE_DELAY_MS = 25; // Delay in miliseconds before calling resize callback after window is resized
    
    private int width;
    private int height;

    private final long handle;

    private final Timer resizeTimer;
    private final List<Runnable> resizeListeners = new ArrayList<>();

    public GLFWWindow(int width, int height, String title) {
        this.width = width;
        this.height = height;
        
        this.resizeTimer = createResizeTimer(this);
        this.handle = createHandle(this, resizeTimer, width, height, title);
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    private static long createHandle(GLFWWindow window, Timer resizeTimer, int width, int height, String title) {
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        
        long handle = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if(handle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create window");
        }

        GLFW.glfwSetWindowSizeCallback(handle,(_handle, _width, _height) -> {
            window.width = _width;
            window.height = _height;
            resizeTimer.restart();
        });

        return handle;
    }

    private static Timer createResizeTimer(GLFWWindow window) {
        return new Timer(RESIZE_DEBOUNCE_DELAY_MS, window::callResizeListeners);
    }

    public void update() {
        GLFW.glfwPollEvents();
        resizeTimer.update();
    }

    public void addResizeListener(Runnable listener) {
        resizeListeners.add(listener);
    }

    public void callResizeListeners() {
        resizeListeners.forEach(Runnable::run);
    }

    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public boolean isResizing() {
        return resizeTimer.isRunning();
    }

    public long getHandle() {
        return handle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
