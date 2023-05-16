package ca.artemis.engine.vulkan.core;

import ca.artemis.Vector2f;

public class UniformVec2 extends Uniform<Vector2f> {

    public UniformVec2(int binding, int offset) {
        super(binding, offset);
    }

    @Override
    public void uploadValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadValue'");
    }
}
