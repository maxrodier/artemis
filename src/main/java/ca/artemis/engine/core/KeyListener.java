package ca.artemis.engine.core;

import org.lwjgl.glfw.GLFW;

public class KeyListener {
    
    private static KeyListener instance;
    
    private boolean keyPressed[] = new boolean[512];

    private KeyListener() { }

    public static KeyListener getInstance() {
        if(instance == null) {
            instance = new KeyListener();
        }

        return instance;
    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if(key >= getInstance().keyPressed.length) {
            return;
        }

        if(action == GLFW.GLFW_PRESS) {
            getInstance().keyPressed[key] = true;
        } else if(action == GLFW.GLFW_RELEASE) {
            getInstance().keyPressed[key] = false;
        }
    }

    public static boolean isKeyPressed(int key) {
        if(key >= getInstance().keyPressed.length) {
            throw new IllegalAccessError("Key is not supported!");
        }
        return getInstance().keyPressed[key];
    }
}
