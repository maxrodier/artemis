package ca.artemis.engine.core;

import ca.artemis.engine.core.math.Matrix4f;
import ca.artemis.engine.core.math.Vector2f;
import ca.artemis.engine.core.math.Vector3f;

public class Camera {

    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    public Vector2f position;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f().initIdentity();
        this.viewMatrix = new Matrix4f().initIdentity();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.initOrthographic(0.0f, 32.0f * 40.0f, 0.0f, 32.0f * 21.0f, 0.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        viewMatrix.setLookAt(new Vector3f(position.x, position.y, 20.0f), cameraFront.add(position.x, position.y, 0), cameraUp);

        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}
