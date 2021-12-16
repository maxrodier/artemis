package ca.artemis.engine.scene3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.lwjgl.system.MemoryStack;

import ca.artemis.math.Matrix4f;
import ca.artemis.vulkan.rendering.mesh.Transform;

@JsonDeserialize(builder = Node.Builder.class)
public class Node {
    
    private String id;
    private List<Node> children;
    private Node parent;
    private Transform transform = new Transform();

    protected Node(Builder<?> builder) {
        this.id = builder.id;
        this.children = builder.children;
        for(Node child: this.children) {
            child.setParent(this);
        }
    }

    public void destroy() {
        for(Node child: children) {
            child.destroy();
        }
    }

    public void updateAll(MemoryStack stack) {
        update(stack);
        for(Node child: children)
            child.updateAll(stack);
    }

    protected void update(MemoryStack stack) { }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }
    
    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Node getParent() {
        return parent;
    }

    private void setParent(Node parent) {
        this.parent = parent;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4f getTransformation() {
        if(parent != null)
            return parent.getTransformation().mul(transform.getTransformation());
        return transform.getTransformation();
    }

    public static class Builder<T extends Builder<T>> {

        private String id;
        private List<Node> children = new ArrayList<>();

        @JsonProperty("class")
        public Builder<T> setClass(String cls) {
            //Used for deserilization
            return this;
        }

        @JsonProperty("id")
        public Builder<T> setId(String id) {
            this.id = id;
            return this;
        }

        @JsonProperty("children")
        @JsonDeserialize(using = ChildrenDeserializer.class)
        public Builder<T> setChildren(List<Node> children) {
            this.children = children;
            return this;
        }

        public Node build() {
            return new Node(this);
        }
    }
}