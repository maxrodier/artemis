package ca.artemis.vulkan.rendering.scene;

public class PixelConstraint extends Constraint {
    
    private int value;

    public PixelConstraint(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
