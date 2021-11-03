package ca.artemis.vulkan.rendering.scene.constraint;

public class FloatConstraint extends Constraint {
    
    private float value;

    public FloatConstraint(float value) {
        this.value = value;
    }

    @Override
    public float getValue() {
        return value;
    }
}
