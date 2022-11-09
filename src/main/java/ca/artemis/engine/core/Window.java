package ca.artemis.engine.core;

import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.scenes.LevelEditorScene;
import ca.artemis.engine.core.scenes.LevelScene;
import ca.artemis.engine.core.scenes.Scene;
import ca.artemis.engine.core.scenes.TestScene;
import ca.artemis.engine.core.utils.Time;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.rendering.RenderingEngine;

public class Window {
    
    private static Window instance = null;
    private static Scene currentScene = null;

    private int width;
    private int height;

    private String title;

    private long handle = 0;

    private VulkanContext context; //TODO: I don't like this ...
    private RenderingEngine renderingEngine;

    private Window() {
        this.width = 1600;
        this.height = 900;
        this.title = "Mario";
    }

    public static Window createInstance() {
        if(instance != null) {
            throw new AssertionError("Window Instance already created!");
        }
        instance = new Window();
        return instance;
    }

    public static void changeScene(int newScene) {
        switch(newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            case 2:
                currentScene = new TestScene();
                break;
            default:
                throw new AssertionError("Unknown Scene: " + newScene);
        }

        currentScene.init();
        currentScene.start();
    }

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();
        cleanup();
    }

    public void init() {
        //Setup and error callback
        GLFWErrorCallback.createPrint(System.err).set();

        //Initialize GLFW
        if(!GLFW.glfwInit()) {
            throw new AssertionError("Could not initialize GLFW");
        }

        if (!GLFWVulkan.glfwVulkanSupported()) {
            throw new AssertionError("Could not find a compatible Vulkan installable client driver");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        //GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_FALSE);

        handle = GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (handle == MemoryUtil.NULL) {
            throw new AssertionError("Could not create GLFW window");
        }

        GLFW.glfwSetCursorPosCallback(handle, MouseListener::mousePosCallback);
        GLFW.glfwSetMouseButtonCallback(handle, MouseListener::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(handle, MouseListener::mouseScrollCallback);
        GLFW.glfwSetKeyCallback(handle, KeyListener::keyCallback);

        context = VulkanContext.makeContextCurrent(this);
        renderingEngine = RenderingEngine.createInstance(context, this);

        //GLFW.glfwShowWindow(handle);

        Window.changeScene(0);
    }

    public void loop() {
        float beginTime = Time.getTime();
        float endTime = Time.getTime();
        float dt = -1.0f;

        while(!GLFW.glfwWindowShouldClose(handle)) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                GLFW.glfwPollEvents();

                IntBuffer pImageIndex = renderingEngine.prepareRender(stack, context);

                currentScene.update(dt, renderingEngine.getCurrentFrame());

                renderingEngine.render(stack, context, pImageIndex);

                endTime = Time.getTime();
                dt = endTime - beginTime;
                beginTime = endTime;
            }
        }
        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());

    }

    public void cleanup() {
        context.destroy();

        Callbacks.glfwFreeCallbacks(handle);
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public long getHandle() {
        return handle;
    }
}
