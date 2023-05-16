package ca.artemis.engine.ecs.components;

import java.util.ArrayList;
import java.util.List;

import ca.artemis.engine.api.vulkan.memory.VulkanBuffer;
import ca.artemis.engine.core.ecs.Component;
import ca.artemis.engine.core.math.Matrix4f;
import ca.artemis.engine.core.math.Quaternion;
import ca.artemis.engine.core.math.Vector3f;

public class TransformComponent extends Component {
    
    private Vector3f position;
    private Quaternion rotation;
    private Vector3f scale;

    private List<VulkanBuffer> uniformBuffers = new ArrayList<>(); //One per frame in flight

    public TransformComponent() {
        this.position = new Vector3f();
        this.rotation = new Quaternion();
        this.scale = new Vector3f(1, 1, 1);
    }
    
    public TransformComponent(Vector3f position) {
        this.position = position;
        this.rotation = new Quaternion();
        this.scale = new Vector3f(1, 1, 1);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public VulkanBuffer getUniformBuffer(int frameIndex) {
        return uniformBuffers.get(frameIndex);
    }

    public Matrix4f getTransformation() {
        Matrix4f translationMatrix = new Matrix4f().translation(position);
        Matrix4f rotationMatrix = new Matrix4f().rotation(rotation);
        Matrix4f scaleMatrix = new Matrix4f().scaling(scale);

        return translationMatrix.mul(rotationMatrix.mul(scaleMatrix));
    }
}
