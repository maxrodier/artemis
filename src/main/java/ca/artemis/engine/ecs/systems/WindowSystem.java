package ca.artemis.engine.ecs.systems;

import ca.artemis.engine.api.vulkan.core.Context;
import ca.artemis.engine.api.vulkan.core.GLFWWindow;
import ca.artemis.engine.api.vulkan.core.VulkanInstance;
import ca.artemis.engine.api.vulkan.core.VulkanSurface;
import ca.artemis.engine.core.EngineSettings;
import ca.artemis.engine.core.ecs.BaseSystem;

public class WindowSystem extends BaseSystem {

    private GLFWWindow window;
    private VulkanInstance instance;
    private VulkanSurface surface;
    
    public WindowSystem() {
        super();
    }

    @Override
    public void init(EngineSettings settings) {    
        Context.getInstance();
        window = new GLFWWindow(800, 600, "Artemis Engine");
        instance = new VulkanInstance();
        surface = new VulkanSurface(instance, window);
    }

    @Override
    public void destroy() {
        surface.destroy(instance);
        instance.destroy();
        window.destroy();
        Context.getInstance().destroy();
    }

    @Override
    public void update(float delta) {
        window.update();
    }

    public boolean isCloseRequested() {
        return window.isCloseRequested();
    }

    public boolean isResizing() {
        return window.isResizing();
    }

    public GLFWWindow getWindow() {
        return window;
    }

    public VulkanInstance getInstance() {
        return instance;
    }

    public VulkanSurface getSurface() {
        return surface;
    }
}
