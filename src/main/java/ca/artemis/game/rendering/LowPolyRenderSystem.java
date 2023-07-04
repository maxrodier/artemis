package ca.artemis.game.rendering;

import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.rendering.entity.EntityRenderDataComponent;
import ca.artemis.engine.scenes.Component;
import ca.artemis.engine.scenes.GameObject;
import ca.artemis.engine.scenes.Scene;

public class LowPolyRenderSystem {
    
    private LowPolyRenderingEngine lowPolyRenderingEngine = LowPolyRenderingEngine.instance();

    private int frameIndex = 0;

    //We update renderList of renderers except for the swapchainRenderer. The swapchainRenderer does not need to be updated has it only render a texture for presentation.
    public void update(MemoryStack stack, Scene activeScene) {
        frameIndex = (frameIndex + 1) % LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; //We increase frameIndex at the begining of the rendersystem update. This is to ensure that we have correct frameIndex during render.

        //We update the renderingEngine -> We are acquiring the next available image in the swapchain.
        if(!lowPolyRenderingEngine.update(stack, frameIndex)) {
            return;
        }

        //If we were able to acquire a swapchain image, we update all the renderDataComponents. Else we sould have returned and regenerated the swapchain to retry acquiring a swapchain image.
        List<GameObject> gameObjects = activeScene.getGameObjects();
        for(GameObject gameObject : gameObjects) {
            for(Component component : gameObject.getComponents()) {
                if(component.getClass().isAssignableFrom(EntityRenderDataComponent.class)) {
                    EntityRenderDataComponent entityRenderDataComponent = EntityRenderDataComponent.class.cast(component);
                    entityRenderDataComponent.update(stack, frameIndex);
                    LowPolyRenderingEngine.instance().getEntityRenderer().addToExecutionList(frameIndex, entityRenderDataComponent);
                }
            }
        }
    }

    public void render(MemoryStack stack) {
        lowPolyRenderingEngine.render(stack, frameIndex);
    }
}
