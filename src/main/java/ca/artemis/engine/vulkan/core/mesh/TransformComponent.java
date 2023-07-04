package ca.artemis.engine.vulkan.core.mesh;

import ca.artemis.engine.maths.Matrix4f;
import ca.artemis.engine.maths.Quaternion;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.scenes.Component;

public class TransformComponent extends Component {

    public Vector3f position;
    public Quaternion rotation;
    public Vector3f scaling;
    
    public TransformComponent() {
        this(new Vector3f(0, 0, 0));
    }

    public TransformComponent(Vector3f position) {
        this(position, new Quaternion(0, 0, 0, 1));
    }

    public TransformComponent(Vector3f position, Quaternion rotation) {
        this(position, rotation, new Vector3f(1, 1, 1));
    }

    public TransformComponent(Vector3f position, Quaternion rotation, Vector3f scaling) {
        this.position = position;
        this.rotation = rotation;
        this.scaling = scaling;
    }

    public Matrix4f getTransformationMatrix() {
        return new Matrix4f().initTranslation(position.x, position.y, position.z)
            .mul(new Matrix4f().initScaling(scaling.x, scaling.y, scaling.z)
            .mul(rotation.toRotationMatrix()));
    }
}
