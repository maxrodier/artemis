package ca.artemis.vulkan.rendering.scene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.Configuration;
import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.mesh.Transform;

public class Node {

    private Node parent;
    private List<Node> children = new ArrayList<>();
    private Transform transform = new Transform();

    public void add(Node child) {
        if(child.getParent() != null) {
            child.getParent().remove(child);
        }
        child.setParent(this);
        children.add(child);
    }

    public void remove(Node child) {
        child.setParent(null);
        children.remove(child);
    }

    public void updateAll(VulkanContext context, MemoryStack stack) {
        update(context, stack);
        for(Node child : children)
            child.updateAll(context, stack);
    }

    protected void update(VulkanContext context, MemoryStack stack) { }

    public void populateDrawCommandBuffers(List<SecondaryCommandBuffer> drawCommandBuffers) {
        for(Node child : children) {
            child.populateDrawCommandBuffers(drawCommandBuffers);
        }
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getTransformation() {
        if(parent != null) {
            return parent.getTransformation().mul(transform.getTransformation());
        }
        Matrix4f projection = new Matrix4f().initOrthographic(0, Configuration.windowWidth, 0, Configuration.windowHeight, -1, 1);
        return projection.mul(transform.getTransformation());
    }
}
