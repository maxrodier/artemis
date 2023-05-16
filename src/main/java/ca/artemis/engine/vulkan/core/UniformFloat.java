package ca.artemis.engine.vulkan.core;

public class UniformFloat extends Uniform<Float> {
    
    public UniformFloat(int binding, int offset) {
        super(binding, offset);
    }

    @Override
    public void uploadValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadValue'");
    }
}
