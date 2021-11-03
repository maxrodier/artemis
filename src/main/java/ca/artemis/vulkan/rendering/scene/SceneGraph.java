package ca.artemis.vulkan.rendering.scene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;

public class SceneGraph {
    
    private final Node root = new Node();
    private final List<SecondaryCommandBuffer> drawCommandBuffers = new ArrayList<>();

    public SceneGraph(int x, int y, int width, int height) {
        // this.root.setX(new PixelConstraint(x));
        // this.root.setY(new PixelConstraint(y));
        // this.root.setWidth(new PixelConstraint(width));
        // this.root.setHeight(new PixelConstraint(height));
    }

    public List<SecondaryCommandBuffer> getDrawCommandBuffers() {
        drawCommandBuffers.clear();
        root.populateDrawCommandBuffers(drawCommandBuffers);
        return drawCommandBuffers;
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

    public void destroy() {
        root.destroy();
    }
}
