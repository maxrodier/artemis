package ca.artemis.engine.core;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.core.ecs.Entity;
import ca.artemis.engine.ecs.systems.ResourcesManagerSystem;
import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;
import ca.artemis.engine.ecs.systems.WindowSystem;
import ca.artemis.engine.rendering.FrameInfo;

public class ArtemisEngine {
    
    public List<Entity> entities = new ArrayList<>();

    public WindowSystem windowSystem;
    public VulkanRenderingSystem renderingSystem;
    public ResourcesManagerSystem resourcesManagerSystem;

    private int nextEntityId = 0;

    public ArtemisEngine() {
        windowSystem = new WindowSystem();
        renderingSystem = new VulkanRenderingSystem(windowSystem);
        resourcesManagerSystem = new ResourcesManagerSystem(renderingSystem);
    }

    public Entity createEntity() {
        Entity entity = new Entity(nextEntityId++);
        entities.add(entity);
        return entity;
    }

    public void run() {
        EngineSettings engineSettings = new EngineSettings();
        windowSystem.init(engineSettings);
        renderingSystem.init(engineSettings);
        resourcesManagerSystem.init(engineSettings);

        windowSystem.initComponents();
        renderingSystem.initComponents();
        resourcesManagerSystem.initComponents();

        mainLoop();
        destroy();
    }

    private void mainLoop() {
        while(!windowSystem.isCloseRequested()) {
            windowSystem.update(0.0f);

            if(!windowSystem.isResizing()) {
                try(MemoryStack stack = MemoryStack.stackPush()) {
                    FrameInfo frameInfo = renderingSystem.prepareRender(stack);
                    if(frameInfo.pImageIndex == null) {
                        return;
                    }
                    renderingSystem.get
                    renderingSystem.render(stack, frameInfo);
                }
            }
        }
        renderingSystem.waitIdle();
    }

    public void destroy() {
        resourcesManagerSystem.destroy();
        renderingSystem.destroy();
        windowSystem.destroy();
    }
}
