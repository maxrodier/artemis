package ca.artemis.vulkan.api.context.window;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import ca.artemis.Configuration;
import ca.artemis.engine.utils.FileUtils;

public class GLFWWindow {

    private final long handle;

	private int pixelWidth, pixelHeight;
	private int desiredWidth, desiredHeight;
	private int widthScreenCoords, heightScreenCoords;

	private boolean fullscreen;
	private boolean vsync;
	private int fps;

    private final List<WindowSizeListener> listeners = new ArrayList<>();

    public GLFWWindow() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);

        this.handle = GLFW.glfwCreateWindow(Configuration.windowWidth, Configuration.windowHeight, Configuration.windowTitle, MemoryUtil.NULL, MemoryUtil.NULL);
        if (this.handle == MemoryUtil.NULL)
            throw new AssertionError("Could not create GLFW window");

        this.setIcon();

        this.getInitialWindowSizes();
		this.addScreenSizeListener();
		this.addPixelSizeListener();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    public void update() {
    }

    public void setIcon() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            
            String[] icons = Configuration.icons;
            GLFWImage.Buffer buffer = GLFWImage.callocStack(icons.length, stack);

            for(int i = 0; i < icons.length; i++) {
                BufferedImage bufferedImage = FileUtils.getBufferedImage(icons[i]);

                if(bufferedImage.getType() != BufferedImage.TYPE_4BYTE_ABGR)
                    throw new AssertionError("Icon must be of type ARGB");

                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();

                ByteBuffer byteBuffer = stack.calloc(bufferedImage.getRaster().getDataBuffer().getSize());
                for( int h = 0; h < height; h++ ) {
                   for( int w = 0; w < width; w++ ) {
                      int argb = bufferedImage.getRGB( w, h );
                      byteBuffer.put( (byte) ( 0xFF & ( argb >> 16 ) ) );
                      byteBuffer.put( (byte) ( 0xFF & ( argb >> 8 ) ) );
                      byteBuffer.put( (byte) ( 0xFF & ( argb ) ) );
                      byteBuffer.put( (byte) ( 0xFF & ( argb >> 24 ) ) );
                   }
                }
                byteBuffer.flip();
    
                GLFWImage icon = GLFWImage.callocStack(stack);
                icon.set(width, height, byteBuffer);
                buffer.put(i, icon);
            }

            GLFW.glfwSetWindowIcon(handle, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCloseRequested() {
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
                if(GLFWWindow.this.validSizeChange(width, height, GLFWWindow.this.widthScreenCoords, GLFWWindow.this.heightScreenCoords)) {
                    GLFWWindow.this.widthScreenCoords = width;
                    GLFWWindow.this.heightScreenCoords = height;
                }
            }
        });
	}

	private void addPixelSizeListener() {
		GLFW.glfwSetFramebufferSizeCallback(this.handle, new GLFWFramebufferSizeCallbackI() {
            @Override
            public void invoke(long window, int width, int height) {
                if(GLFWWindow.this.validSizeChange(width, height, GLFWWindow.this.pixelWidth, GLFWWindow.this.pixelHeight)) {
                    GLFWWindow.this.pixelWidth = width;
                    GLFWWindow.this.pixelHeight = height;
                    GLFWWindow.this.notifyListeners();
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