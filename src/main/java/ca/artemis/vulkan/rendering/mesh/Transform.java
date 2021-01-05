package ca.artemis.vulkan.rendering.mesh;

import ca.artemis.math.Matrix4f;
import ca.artemis.math.Quaternion;
import ca.artemis.math.Vector3f;

public class Transform {

    private Vector3f position;
    private Quaternion rotation;
    private Vector3f scaling;

    public Transform() {
		position = new Vector3f(0.0f, 0.0f, 0.0f);
		rotation = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
		scaling = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public void rotate(Vector3f axis, float angle) {
        rotation = new Quaternion(axis, angle).mul(rotation).normalized();
    }

    public Matrix4f getTransformation() {
        Matrix4f translationMatrix = new Matrix4f().initTranslation(position.getX(), position.getY(), position.getZ());
        Matrix4f rotationMatrix = rotation.toRotationMatrix();
        Matrix4f scalingMatrix = new Matrix4f().initScaling(scaling.getX(), scaling.getY(), scaling.getZ());

        return translationMatrix.mul(rotationMatrix.mul(scalingMatrix));
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

    public Vector3f getScaling() {
        return scaling;
    }

    public void setScaling(Vector3f scaling) {
        this.scaling = scaling;
    }
}
