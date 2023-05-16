package ca.artemis.engine.vulkan.core;

public abstract class Uniform<T> {
    
    private final int binding;
    private final int offset;

    protected T value;

    protected Uniform(int binding, int offset) {
        this.binding = binding;
        this.offset = offset;
    }

    public abstract void uploadValue();
    
    public int getBinding() {
        return binding;
    }

    public int getOffset() {
        return offset;
    }

    public T getValue() {
        return value;
    }

}
