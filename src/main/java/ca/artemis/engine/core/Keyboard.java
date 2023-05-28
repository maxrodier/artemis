package ca.artemis.engine.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

public class Keyboard {

    private Set<Integer> keysDown = new HashSet<>();
    private Set<Integer> keysPressed = new HashSet<>();
    private Set<Integer> keysRepeated = new HashSet<>();
    private Set<Integer> keysReleased = new HashSet<>();
    private List<Character> chars = new ArrayList<>();

    protected Keyboard(Builder builder) {
        addKeyListener(builder.window.getId());
        addTextListener(builder.window.getId());
    }

    private void addKeyListener(long id) {
        GLFW.glfwSetKeyCallback(id, (window, key, scancode, action, mods) -> {
            if(action == GLFW.GLFW_PRESS) {
                keysDown.add(key);
                keysPressed.add(key);
            } else if(action == GLFW.GLFW_REPEAT) {
                keysRepeated.add(key);
            } else if(action == GLFW.GLFW_RELEASE) {
                keysDown.remove(key);
                keysReleased.add(key);
            } 
        });
    }

    private void addTextListener(long id) {
        GLFW.glfwSetCharCallback(id, (window, codepoint) -> {
            for(Character character: Character.toChars(codepoint)) {
                chars.add(character);
            }
        });
    }

    public void update() {
        keysPressed.clear();
        keysRepeated.clear();
        keysReleased.clear();
        chars.clear();
    }

    public boolean isKeyDown(int key) {
        return keysDown.contains(key);
    }

    public boolean isKeyPressed(int key) {
        return keysPressed.contains(key);
    }

    public boolean isKeyRepeated(int key) {
        return keysRepeated.contains(key);
    }

    public boolean isKeyReleased(int key) {
        return keysReleased.contains(key);
    }

    public List<Character> getChars() {
        return chars;
    }

    public static class Builder {

        private final Window window;

        public Builder(Window window) {
            this.window = window;
        }

        public Keyboard build() {
            return new Keyboard(this);
        }
    }
}
