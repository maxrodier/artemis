package ca.artemis.engine.master;

import ca.artemis.vulkan.api.context.inputs.Keyboard;
import ca.artemis.vulkan.api.context.inputs.Mouse;
import ca.artemis.vulkan.api.context.window.Window;

public class Engine {

    public final Window window;
	public final Keyboard keyboard;
	public final Mouse mouse;
	//public final Resources resources;
	//public final StateManager stateManager;
	
	//private final FrameTimer timer;

	private boolean closeFlag = false;

    protected Engine(Window window, Keyboard keyboard, Mouse mouse/*, FrameTimer timer, StateManager stateManager, Resources resources */) {
		this.window = window;
		this.keyboard = keyboard;
		this.mouse = mouse;
		//this.timer = timer;
		//this.resources = resources;
		//this.stateManager = stateManager;
	}

    public void update() {
		//Ui.update(getDeltaSeconds());
		//BackgroundLoader.doTopGlRequests();
		keyboard.update();
		mouse.update();
		window.update();
		//timer.update();
		//stateManager.updateState();
	}

	public void cleanUp() {
		//BackgroundLoader.cleanUp();
		//Ui.cleanUp();
		window.destroy();
	}
	
	public void requestClose() {
		this.closeFlag = true;
	}
	
	public boolean isCloseRequested() {
		return closeFlag || window.closeButtonPressed();
	}
}
