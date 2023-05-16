package ca.artemis.engine.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import ca.artemis.FileUtils;

public class Window implements AutoCloseable {
    
    private final long id;

    private int desiredWidth;
    private int desiredHeight;

    
    private boolean fullscreen;

    protected Window(Builder builder) {
        this.id = builder.id;

        this.desiredWidth = builder.width;
        this.desiredHeight = builder.height;

        this.fullscreen = builder.fullscreen;
    }

    @Override
    public void close() throws Exception {
        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);
        GLFW.glfwTerminate();
    }

    public void update() {
        GLFW.glfwPollEvents();
    }

    public void toggleFullscreen() {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);

        if(fullscreen) {
		    GLFW.glfwSetWindowMonitor(id, MemoryUtil.NULL, 0, 0, desiredWidth, desiredHeight, vidMode.refreshRate());
		    GLFW.glfwSetWindowPos(id, (vidMode.width() - desiredWidth) / 2, (vidMode.height() - desiredHeight) / 2);
        } else {
            GLFW.glfwSetWindowMonitor(id, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        }
        fullscreen = !fullscreen;
    }

    public long getId() {
        return id;
    }

    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(id);
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public static class Builder {

        private long id;

        private int width;
        private int height;
        private String title;

        private boolean fullscreen = false;
        private String[] icons = { "icons/icon_512.png" };
        private int minWidth = 120;
        private int minHeight = 120;
        private int maxWidth = GLFW.GLFW_DONT_CARE;
        private int maxHeight = GLFW.GLFW_DONT_CARE;

        public Builder(int width, int height, String title) {
            this.width = width;
            this.height = height;
            this.title = title;
        }

        public Builder fullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            return this;
        }

        public Builder minWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Builder minHeight(int minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Window build() {
            if(!GLFW.glfwInit()) {
                throw new RuntimeException("Could not intialize GLFW");
            }

            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            
            if(fullscreen) {
                this.id = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), title, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
            } else {
                this.id = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
                GLFW.glfwSetWindowPos(id, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
            }
            
            GLFW.glfwSetWindowSizeLimits(id, minWidth, minHeight, maxWidth, maxHeight);
            loadIcons();

            return new Window(this);
        }

        private void loadIcons() {
            try (MemoryStack stack = MemoryStack.stackPush()) {
            
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
    
                GLFW.glfwSetWindowIcon(id, buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
