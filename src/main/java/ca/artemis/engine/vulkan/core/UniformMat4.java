package ca.artemis.engine.vulkan.core;

import ca.artemis.Matrix4f;

public class UniformMat4 extends Uniform<Matrix4f> {

    public UniformMat4(int binding, int offset) {
        super(binding, offset);
    }

    @Override
    public void uploadValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadValue'");
    }
}
