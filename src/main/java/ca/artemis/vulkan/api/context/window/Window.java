package ca.artemis.vulkan.api.context.window;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;

import ca.artemis.Configuration;

public class Window {

    private final long handle;

	private int pixelWidth, pixelHeight;
	private int desiredWidth, desiredHeight;
	private int widthScreenCoords, heightScreenCoords;

	private boolean fullscreen;
	private boolean vsync;
	private int fps;

    private final List<WindowSizeListener> listeners = new ArrayList<>();

    public Window(long handle, int desiredWidth, int desiredHeight, boolean fullscreen, boolean vsync, int fps) {
		this.handle = handle;
		this.desiredWidth = desiredWidth;
		this.desiredHeight = desiredHeight;
		this.fullscreen = fullscreen;
		this.vsync = vsync;
		this.fps = fps;

        this.getInitialWindowSizes();
		this.addScreenSizeListener();
		this.addPixelSizeListener();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    public void update() {
    }

    public boolean closeButtonPressed() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public long getHandle() {
        return handle;
    }

    public int getScreenCoordWidth() {
		return widthScreenCoords;
	}

	public int getScreenCoordHeight() {
		return heightScreenCoords;
	}

	public int getFps() {
		return fps;
	}

	public float getAspectRatio() {
		return (float) pixelWidth / pixelHeight;
	}

	public int getPixelWidth() {
		return pixelWidth;
	}

	public int getPixelHeight() {
		return pixelHeight;
	}

    private void addScreenSizeListener() {
		GLFW.glfwSetWindowSizeCallback(this.handle, new GLFWWindowSizeCallbackI() {
            @Override
            public void invoke(long window, int width, int height) {
                if(Window.this.validSizeChange(width, height, Window.this.widthScreenCoords, Window.this.heightScreenCoords)) {
                    Window.this.widthScreenCoords = width;
                    Window.this.heightScreenCoords = height;
                }
            }
        });
	}

	private void addPixelSizeListener() {
		GLFW.glfwSetFramebufferSizeCallback(this.handle, new GLFWFramebufferSizeCallbackI() {
            @Override
            public void invoke(long window, int width, int height) {
                if(Window.this.validSizeChange(width, height, Window.this.pixelWidth, Window.this.pixelHeight)) {
                    Window.this.pixelWidth = width;
                    Window.this.pixelHeight = height;
                    Window.this.notifyListeners();
                }
            }
        });
	}

    private void getInitialWindowSizes() {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer widthBuff = stack.callocInt(1);
			IntBuffer heightBuff = stack.callocInt(1);
			this.getInitialScreenSize(widthBuff, heightBuff);
            widthBuff.clear();
            heightBuff.clear();
			this.getInitialPixelSize(widthBuff, heightBuff);
		}
	}

	private void getInitialScreenSize(IntBuffer widthBuff, IntBuffer heightBuff) {
		GLFW.glfwGetWindowSize(this.handle, widthBuff, heightBuff);
		this.widthScreenCoords = widthBuff.get(0);
		this.heightScreenCoords = heightBuff.get(0);
	}

	private void getInitialPixelSize(IntBuffer widthBuff, IntBuffer heightBuff) {
		GLFW.glfwGetFramebufferSize(this.handle, widthBuff, heightBuff);
		this.pixelWidth = widthBuff.get(0);
		this.pixelHeight = heightBuff.get(0);
	}

    private boolean validSizeChange(int newWidth, int newHeight, int oldWidth, int oldHeight) {
		if(newWidth == 0 || newHeight == 0) {
			return false;
		}
		return newWidth != oldWidth || newHeight != oldHeight;
	}

    private void notifyListeners() {
        for(WindowSizeListener listener : this.listeners) {
            listener.sizeChanged(pixelWidth, pixelHeight);
        }
    }
}