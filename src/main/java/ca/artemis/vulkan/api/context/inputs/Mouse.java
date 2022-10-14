package ca.artemis.vulkan.api.context.inputs;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

import ca.artemis.vulkan.api.context.window.Window;

public class Mouse {
    
    private final Window window;

	private Set<Integer> buttonsDown = new HashSet<Integer>();
	private Set<Integer> buttonsClickedThisFrame = new HashSet<Integer>();
	private Set<Integer> buttonsReleasedThisFrame = new HashSet<Integer>();
	
	private float x, y;
	private float dx, dy;
	private float scroll;
	private float lastX, lastY;

    public Mouse(Window window) {
        this.window = window;
		addMoveListener(this.window.getHandle());
		addClickListener(this.window.getHandle());
		addScrollListener(this.window.getHandle());
    }

    public void update(){
		buttonsClickedThisFrame.clear();
		buttonsReleasedThisFrame.clear();
		updateDeltas();
		this.scroll = 0;
	}

    public boolean isButtonDown(int button){
		return buttonsDown.contains(button);
	}
	
	public boolean isClickEvent(int button){
		return buttonsClickedThisFrame.contains(button);
	}
	
	public boolean isReleaseEvent(int button){
		return buttonsReleasedThisFrame.contains(button);
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public float getDx(){
		return dx;
	}
	
	public float getDy(){
		return dy;
	}
	
	public float getScroll(){
		return scroll;
	}

    private void reportButtonClick(int button){
		buttonsClickedThisFrame.add(button);
		buttonsDown.add(button);
	}
	
	private void reportButtonRelease(int button){
		buttonsReleasedThisFrame.add(button);
		buttonsDown.remove((Integer)button);
	}

    private void updateDeltas(){
		this.dx = x - lastX;
		this.dy = y - lastY;
		this.lastX = x;
		this.lastY = y;
	}

    private void addMoveListener(long handle){
		GLFW.glfwSetCursorPosCallback(handle, new GLFWCursorPosCallbackI() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                Mouse.this.x = (float) (xpos / Mouse.this.window.getScreenCoordWidth());
                Mouse.this.y = (float) (ypos / Mouse.this.window.getScreenCoordHeight());
            }
        });
	}

	private void addClickListener(long handle){
		GLFW.glfwSetMouseButtonCallback(handle, new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if(action == GLFW.GLFW_PRESS){
                    Mouse.this.reportButtonClick(button);
                }else if(action == GLFW.GLFW_RELEASE){
                    Mouse.this.reportButtonRelease(button);
                }
            }
        });
	}
	
	private void addScrollListener(long handle){
		GLFW.glfwSetScrollCallback(handle, new GLFWScrollCallbackI() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {                
			    Mouse.this.scroll = (float) yoffset;
            }
		});
	}
}
