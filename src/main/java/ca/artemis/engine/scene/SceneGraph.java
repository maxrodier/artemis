package ca.artemis.engine.scene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;

public class SceneGraph {
    
    private final VulkanContext context;

    private final Node root = new RootNode();

    private final List<SecondaryCommandBuffer> drawCommandBuffers = new ArrayList<>();

    public SceneGraph(VulkanContext context) {
        this.context = context;
    }

    public void destroy() {
        root.destroy();
    }

    public void add(Node node) {
        root.add(node);
    }

    public void remove(Node node) {
        root.remove(node);
    }

    public void update(VulkanContext context, MemoryStack stack) {
        root.updateAll(context, stack);
    }

    public List<SecondaryCommandBuffer> getDrawCommandBuffers() {
        drawCommandBuffers.clear();
        root.populateDrawCommandBuffers(drawCommandBuffers);
        return drawCommandBuffers;
    }

    private class RootNode extends Node {
        @Override
        public Matrix4f getTransformation() {
            Matrix4f projection = new Matrix4f().initOrthographic(0, context.getSurfaceCapabilities().currentExtent().width(), 0, context.getSurfaceCapabilities().currentExtent().height(), -1, 1);
            return projection.mul(getTransform().getTransformation());
        }
    } 
}
