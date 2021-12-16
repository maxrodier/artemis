package ca.artemis.engine.scene;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.rendering.mesh.Transform;

public class Node {

    private Node parent;
    private List<Node> children = new ArrayList<>();
    private Transform transform = new Transform();

    public void destroy() {
        for(Node child: children) {
            child.destroy();
        }
    }

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

    public void updateAll(MemoryStack stack) {
        update(stack);
        for(Node child : children)
            child.updateAll(stack);
    }

    protected void update(MemoryStack stack) { }

    public void populateDrawCommandBuffers(List<SecondaryCommandBuffer> drawCommandBuffers) {
        for(Node child : children) {
            child.populateDrawCommandBuffers(drawCommandBuffers);
        }
    }

    public Node getParent() {
        return parent;
    }

    protected void setParent(Node parent) {
        this.parent = parent;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getTransformation() {
        if(parent != null) {
            return parent.getTransformation().mul(transform.getTransformation());
        }
        return transform.getTransformation();
    }

}