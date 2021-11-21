package ca.artemis.engine.scene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.Configuration;
import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;

public class SceneGraph {
    
    private final Node root = new RootNode();

    private final List<SecondaryCommandBuffer> drawCommandBuffers = new ArrayList<>();

    public SceneGraph(int x, int y, int width, int height) {

    }

    public void destroy(CommandPool commandPool) {
        root.destroy(commandPool);
    }

    public void add(Node node) {
        root.add(node);
    }

    public void remove(Node node) {
        root.remove(node);
    }

    public void update(MemoryStack stack) {
        root.updateAll(stack);
    }

    public List<SecondaryCommandBuffer> getDrawCommandBuffers() {
        drawCommandBuffers.clear();
        root.populateDrawCommandBuffers(drawCommandBuffers);
        return drawCommandBuffers;
    }

    private class RootNode extends Node {
        @Override
        public Matrix4f getTransformation() {
            Matrix4f projection = new Matrix4f().initOrthographic(0, Configuration.windowWidth, 0, Configuration.windowHeight, -1, 1);
            return projection.mul(getTransform().getTransformation());
        }
    } 
}
