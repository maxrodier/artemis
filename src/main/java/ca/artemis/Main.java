package ca.artemis;

import ca.artemis.engine.core.Window;
import ca.artemis.engine.platform.GLFWContext;
import ca.artemis.engine.platform.window.GLFWWindow;

public class Main {
    
    public static void main(String[] args) {
        GLFWContext.create();
        Window window = new GLFWWindow.Builder()
            .setWidth(Configuration.WINDOW_WIDTH)
            .setHeight(Configuration.WINDOW_HEIGHT)
            .setTitle(Configuration.WINDOW_TITLE)
            .isResizable(true)
            .build();

        while(!window.isCloseRequested()) {
            window.update();
        }

        window.destroy();
        GLFWContext.destroy();
    }
}
