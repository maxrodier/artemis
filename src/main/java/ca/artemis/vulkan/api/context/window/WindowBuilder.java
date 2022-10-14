package ca.artemis.vulkan.api.context.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;

public class WindowBuilder {

    private final int width;
	private final int height;
	private final String title;

	private boolean fullscreen = false;
	private boolean vsync = true;
	private GLFWImage.Buffer icon = null;
	private int minWidth = 120;
	private int minHeight = 120;
	private int fps = 100;
	private int maxWidth = GLFW.GLFW_DONT_CARE;
	private int maxHeight = GLFW.GLFW_DONT_CARE;
	private int samples = 0;

    protected WindowBuilder(int width, int height, String title) {
		this.width = width;
		this.height = height;
		this.title = title;
	}

    /* 
    public Window create() {
		GLFWErrorCallback.createPrint(System.err).set();
		GLFW.glfwInit();
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		setWindowHints(vidMode);
		long handle = createWindow(vidMode);
		applyWindowSettings(windowId);
		return new GLFWWindow(windowId, width, height, fps, fullscreen, vsync);
	}
    */
}
