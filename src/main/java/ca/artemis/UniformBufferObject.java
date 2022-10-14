package ca.artemis;

public class UniformBufferObject {
    
    public static final int LENGTH = mat4.LENGTH + mat4.LENGTH + mat4.LENGTH;
    public static final int BYTES = mat4.BYTES + mat4.BYTES + mat4.BYTES;

    public mat4 model;
    public mat4 view;
    public mat4 proj;

    public UniformBufferObject(mat4 model, mat4 view, mat4 proj) {
        this.model = model;
        this.view = view;
        this.proj = proj;
    }

    public float[] getMemoryLayout() {
        float[] result = new float[LENGTH];
        int pos = 0;
        
        for (float element : model.getMemoryLayout()) {
            result[pos] = element;
            pos++;
        }

        for (float element : view.getMemoryLayout()) {
            result[pos] = element;
            pos++;
        }

        for (float element : proj.getMemoryLayout()) {
            result[pos] = element;
            pos++;
        }
		
		return result;	
	}
}
