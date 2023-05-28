package ca.artemis.engine.core;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

public class Mouse {

    private final Set<Integer> buttonsDown = new HashSet<>();
    private final Set<Integer> buttonsPressed = new HashSet<>();
    private final Set<Integer> buttonsReleased = new HashSet<>();

    private float x = 0;
    private float y = 0;
    private float lx = 0;
    private float ly = 0;
    private float dx = 0;
    private float dy = 0;


    protected Mouse(Builder builder) {
        addCursorPosListener(builder.window.getId());
        addMouseButtonListener(builder.window.getId());
    }

    public void update() {
        buttonsPressed.clear();
        buttonsReleased.clear();

        this.dx = x - lx;
        this.dy = y - ly;
        this.lx = x;
        this.ly = y;
    }

    private void addCursorPosListener(long id) {
        GLFW.glfwSetCursorPosCallback(id, (window, xpos, ypos) -> {
            this.x = (float) xpos;
            this.y = (float) ypos;
        });
    }

    private void addMouseButtonListener(long id) {
        GLFW.glfwSetMouseButtonCallback(id, (window, button, action, mods) -> {
            if(action == GLFW.GLFW_PRESS) {
                buttonsPressed.add(button);
                buttonsDown.add(button);
            } else if(action == GLFW.GLFW_RELEASE) {
                buttonsReleased.add(button);
                buttonsDown.remove(button);
            }
        });
    }

    public boolean isButtonDown(int button) {
        return buttonsDown.contains(button);
    }

    public boolean isButtonPressed(int button) {
        return buttonsPressed.contains(button);
    }

    public boolean isButtonReleased(int button) {
        return buttonsReleased.contains(button);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public static class Builder {

        private Window window;

        public Builder(Window window) {
            this.window = window;
        }

        public Mouse build() {
            return new Mouse(this);
        }
    }
}
