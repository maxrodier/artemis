package ca.artemis;

import ca.artemis.engine.maths.Matrix4f;

public class UniformBufferObject {
    
    public static final int LENGTH = Matrix4f.LENGTH + Matrix4f.LENGTH + Matrix4f.LENGTH;
    public static final int BYTES = Matrix4f.BYTES + Matrix4f.BYTES + Matrix4f.BYTES;

    public Matrix4f model;
    public Matrix4f view;
    public Matrix4f proj;

    public UniformBufferObject(Matrix4f model, Matrix4f view, Matrix4f proj) {
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
