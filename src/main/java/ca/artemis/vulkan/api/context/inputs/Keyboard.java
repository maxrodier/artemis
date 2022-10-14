package ca.artemis.vulkan.api.context.inputs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import ca.artemis.vulkan.api.context.window.Window;

public class Keyboard {
    
	private Set<Integer> keysPressedThisFrame = new HashSet<Integer>();
	private Set<Integer> keysRepeatedThisFrame = new HashSet<Integer>();
	private Set<Integer> keysReleasedThisFrame = new HashSet<Integer>();
	private Set<Integer> keysDown = new HashSet<Integer>();
	private List<Integer> charsThisFrame = new ArrayList<Integer>();

    public Keyboard(Window window) {
		addKeyListener(window.getHandle());
		addTextListener(window.getHandle());
    }

    public void update() {
		keysPressedThisFrame.clear();
		keysReleasedThisFrame.clear();
		keysRepeatedThisFrame.clear();
		charsThisFrame.clear();;
	}

    public boolean isKeyDown(int key) {
		return keysDown.contains(key);
	}

	public List<Integer> getChars() {
		return charsThisFrame;
	}

	public boolean keyPressEvent(int key) {
		return keysPressedThisFrame.contains(key);
	}

	public boolean keyPressEvent(int key, boolean checkRepeats) {
		return keysPressedThisFrame.contains(key) || (checkRepeats && keysRepeatedThisFrame.contains(key));
	}

	public boolean keyReleaseEvent(int key) {
		return keysReleasedThisFrame.contains(key);
	}

    private void reportKeyPress(int key) {
		keysDown.add(key);
		keysPressedThisFrame.add(key);
	}

	private void reportKeyRelease(int key) {
		keysDown.remove(((Integer) key));
		keysReleasedThisFrame.add(key);
	}

    private void addKeyListener(long handle) {
        GLFW.glfwSetKeyCallback(handle, new GLFWKeyCallbackI() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(action == GLFW.GLFW_PRESS) {
                    Keyboard.this.reportKeyPress(key);
                } else if(action == GLFW.GLFW_RELEASE) {
                    Keyboard.this.reportKeyRelease(key);
                } else if(action == GLFW.GLFW_REPEAT) {
                    Keyboard.this.keysRepeatedThisFrame.add(key);
                }
            }
        });
    }

    private void addTextListener(long handle) {
        GLFW.glfwSetCharCallback(handle, new GLFWCharCallbackI() {
            @Override
            public void invoke(long window, int codepoint) {
                Keyboard.this.charsThisFrame.add(codepoint);
            }            
        });
    }
}
