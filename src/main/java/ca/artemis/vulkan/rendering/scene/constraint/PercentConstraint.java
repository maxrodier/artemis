package ca.artemis.vulkan.rendering.scene.constraint;

public class PercentConstraint extends Constraint {

    private float percent;
    private Constraint constraint;

    public PercentConstraint(float percent, Constraint constraint) {
        this.percent = percent;
        this.constraint = constraint;
    }

    @Override
    public float getValue() {
        return percent * constraint.getValue() / 100;
    }
}
