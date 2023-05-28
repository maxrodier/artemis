package ca.artemis.engine;

import ca.artemis.engine.core.Keyboard;
import ca.artemis.engine.core.Mouse;
import ca.artemis.engine.core.Window;
import ca.artemis.engine.vulkan.api.context.VulkanContext;

public class LowPolyEngine implements AutoCloseable {
    
    private static LowPolyEngine currentInstance;

    private final Window window;
    private final Keyboard keyboard;
    private final Mouse mouse;

    private final VulkanContext context;

    protected LowPolyEngine(Builder builder) {
        this.window = builder.window;
        this.keyboard = builder.keyboard;
        this.mouse = builder.mouse;

        this.context = new VulkanContext(window);
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing Engine resources...");
        
        window.close();
    }

    public void update() {
        keyboard.update();
        mouse.update();
        window.update();
    }

    public Window getWindow() {
        return window;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public VulkanContext getContext() {
        return context;
    }

    public static LowPolyEngine instance() {
        if(currentInstance == null) {
            currentInstance = new Builder().build();
        }

        return currentInstance;
    }

    protected static class Builder {

        private Window window;
        private Keyboard keyboard;
        private Mouse mouse;

        public LowPolyEngine build() {
            this.window = new Window.Builder(800, 600, "Artemis").build();
            this.keyboard = new Keyboard.Builder(window).build();
            this.mouse = new Mouse.Builder(window).build(); 

            return new LowPolyEngine(this);
        }
    }
}
