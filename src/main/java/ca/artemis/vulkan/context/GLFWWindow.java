package ca.artemis.vulkan.context;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import ca.artemis.Configuration;

public class GLFWWindow {

    private final long handle;

    public GLFWWindow() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);

        this.handle = GLFW.glfwCreateWindow(Configuration.windowWidth, Configuration.windowHeight,
                Configuration.windowTitle, MemoryUtil.NULL, MemoryUtil.NULL);
        if (this.handle == MemoryUtil.NULL)
            throw new AssertionError("Could not create GLFW window");

        setIcon();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
    }

    public void setIcon() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            
            String[] icons = Configuration.icons;
            GLFWImage.Buffer buffer = GLFWImage.callocStack(icons.length, stack);

            for(int i = 0; i < icons.length; i++) {
                BufferedImage bufferedImage = ImageIO.read(Paths.get(getClass().getClassLoader().getResource(icons[i]).getPath()).toUri().toURL());

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
}