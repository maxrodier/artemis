package ca.artemis.engine.vulkan.core;

public class UniformBoolean extends Uniform<Boolean> {
    
    public UniformBoolean(int binding, int offset) {
        super(binding, offset);
    }

    @Override
    public void uploadValue() {

    }
}
