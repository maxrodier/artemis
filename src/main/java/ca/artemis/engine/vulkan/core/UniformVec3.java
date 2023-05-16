package ca.artemis.engine.vulkan.core;

import ca.artemis.Vector3f;

public class UniformVec3 extends Uniform<Vector3f> {

    public UniformVec3(int binding, int offset) {
        super(binding, offset);
    }

    @Override
    public void uploadValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadValue'");
    }
}
