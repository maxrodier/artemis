package ca.artemis.engine.master;

import ca.artemis.engine.debugger.Debugger;
import ca.artemis.vulkan.api.context.inputs.Keyboard;
import ca.artemis.vulkan.api.context.inputs.Mouse;
import ca.artemis.vulkan.api.context.window.Window;

public class EngineCreator {
    
    public static Engine init(EngineConfigs configs) {
        Debugger.init();
        //TODO: InitResFolder
        //TODO: InitErrorManager
        Window window = setUpWindow(configs);
        Keyboard keyboard = new Keyboard(window);
        Mouse mouse = new Mouse(window);

        return new Engine(window, keyboard, mouse);
    }

    private static Window setUpWindow(EngineConfigs configs){
		/*Buffer buffer = loadWindowIcon();
		Window window = Window.newWindow(configs.windowWidth, configs.windowHeight, configs.windowTitle)
				.setVsync(configs.vsync).fullscreen(configs.fullscreen).withIcon(buffer).setMSAA(configs.msaa).setFps(configs.fps)
				.create();
		window.addSizeChangeListener( (width, height) -> GL11.glViewport(0, 0, width, height));
		return window;
        */
        return null;
	}
}
