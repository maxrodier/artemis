package ca.artemis.engine.ecs.components;

import ca.artemis.engine.core.ecs.Component;
import ca.artemis.engine.core.math.Matrix4f;
import ca.artemis.engine.core.math.Quaternion;
import ca.artemis.engine.core.math.Vector3f;

public class CameraComponent extends Component {
    
    private Vector3f position;
    private Quaternion rotation;

    public CameraComponent() {
        this.position = new Vector3f();
        this.rotation = new Quaternion();
    }

    public CameraComponent(Vector3f position) {
        this.position = position;
        this.rotation = new Quaternion();
    }
    
    public CameraComponent(Vector3f position, Quaternion rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Matrix4f getViewMatrix() {
        Matrix4f viewMatrix = new Matrix4f().identity();

        viewMatrix.rotation(rotation.conjugate());
        viewMatrix.translation(position.mul(-1));

        return viewMatrix;
    }

    public void rotate(float x, float y) {
        Vector3f rot = new Vector3f(x, y, 0).mul(0.1f);

        rotation.rotate(new Vector3f(0, 1, 0), (float) Math.toRadians(rot.x));
        rotation.rotate(new Vector3f(1, 0, 0), (float) Math.toRadians(rot.y));
    }

    public void rotate(Vector3f axis, float angle) {
        rotation.rotate(axis, angle);
    }

    public void rotate(Quaternion rotation) {
        this.rotation.mul(rotation).normalize();
    }

    public void move(float amt) {
        position.add(rotation.getForward().mul(amt));
    }

    public void move(Vector3f dir, float amt) {
        position.add(dir.mul(amt));
        
        position.x += dir.x * amt;
        position.y += dir.y * amt;
        position.z += dir.z * amt;
    }
}
