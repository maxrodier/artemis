package ca.artemis.engine.api.vulkan.descriptor;

public class PushConstantRange {

    private final int stageFlags;
    private final int offset;
    private final int size;

    public PushConstantRange(int stageFlags, int offset, int size) {
        this.stageFlags = stageFlags;
        this.offset = offset;
        this.size = size;
    }

    public int getStageFlags() {
        return stageFlags;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }
}