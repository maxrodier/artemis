package ca.artemis.engine.core;

import org.lwjgl.glfw.GLFW;

public class MouseListener {
    
    private static MouseListener instance;

    private double scrollX;
    private double scrollY;

    private double xPos;
    private double yPos;

    private double lastX;
    private double lastY;

    private boolean mouseButtonPressed[] = new boolean[3];
    private boolean isDragging;

    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;

        this.xPos = 0.0;
        this.yPos = 0.0;

        this.lastX = 0.0;
        this.lastY = 0.0;
    }

    public static MouseListener getInstance() {
        if(instance == null) {
            instance = new MouseListener();
        }

        return instance;
    }

    public static void mousePosCallback(long window, double xPos, double yPos) {
        getInstance().lastX = getInstance().xPos;
        getInstance().lastY = getInstance().yPos;
        getInstance().xPos = xPos;
        getInstance().yPos = yPos;

        getInstance().isDragging = getInstance().mouseButtonPressed[0] || getInstance().mouseButtonPressed[1] || getInstance().mouseButtonPressed[2];
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if(button >= getInstance().mouseButtonPressed.length) {
            return;
        }

        if(action == GLFW.GLFW_PRESS) {
            getInstance().mouseButtonPressed[button] = true;
        } else if(action == GLFW.GLFW_RELEASE) {
            getInstance().mouseButtonPressed[button] = false;
            getInstance().isDragging = false;
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        getInstance().scrollX = xOffset;
        getInstance().scrollY = yOffset;
    }

    public static void endFrame() {
        getInstance().scrollX = 0;
        getInstance().scrollY = 0;
        getInstance().lastX = getInstance().xPos;
        getInstance().lastY = getInstance().yPos;
    }

    public static float getX() {
        return (float)getInstance().xPos;
    }

    public static float getY() {
        return (float)getInstance().yPos;
    }

    public static float getDx() {
        return (float)(getInstance().lastX - getInstance().xPos);
    }

    public static float getDy() {
        return (float)(getInstance().lastY - getInstance().yPos);
    }

    public static float getScrollX() {
        return (float)getInstance().scrollX;
    }

    public static float getScrollY() {
        return (float)getInstance().scrollY;
    }

    public static boolean isDragging() {
        return getInstance().isDragging;
    }

    public static boolean mouseButtonDown(int button) {
        if(button >= getInstance().mouseButtonPressed.length) {
            throw new IllegalAccessError("Mouse Button is not supported!");
        }
        return getInstance().mouseButtonPressed[button];
    }
}
