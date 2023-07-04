package ca.artemis.engine.maths;

public class Matrix4f {
    
    public static final int LENGTH = 16;
    public static final int BYTES = LENGTH * Float.BYTES;

    private float[] m;

	public Matrix4f() {
		this.m = new float[16];
	}

	public Matrix4f(float[] m) {
		if(m.length != 16) {
			throw new RuntimeException("Float array need to be length of 16 to create a Matrix4f out of it.");
		}
		this.m = m;
	}
	
	public Matrix4f initIdentity() {
		m[0] = 1.0f; m[4] = 0.0f; m[8]  = 0.0f; m[12] = 0.0f;
		m[1] = 0.0f; m[5] = 1.0f; m[9]  = 0.0f; m[13] = 0.0f;
		m[2] = 0.0f; m[6] = 0.0f; m[10] = 1.0f; m[14] = 0.0f;
		m[3] = 0.0f; m[7] = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initTranslation(float x, float y, float z) {
		m[0] = 1.0f; m[4] = 0.0f; m[8]  = 0.0f; m[12] = x;
		m[1] = 0.0f; m[5] = 1.0f; m[9]  = 0.0f; m[13] = y;
		m[2] = 0.0f; m[6] = 0.0f; m[10] = 1.0f; m[14] = z;
		m[3] = 0.0f; m[7] = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initScaling(float x, float y, float z) {
		m[0] = x;	 m[4] = 0.0f; m[8]  = 0.0f; m[12] = 0.0f;
		m[1] = 0.0f; m[5] = y;	  m[9]  = 0.0f; m[13] = 0.0f;
		m[2] = 0.0f; m[6] = 0.0f; m[10] = z;	m[14] = 0.0f;
		m[3] = 0.0f; m[7] = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initRotation(Vector3f f, Vector3f u, Vector3f r) {
		m[0] = r.x;  m[4] = r.y;  m[8]  = r.z; 	m[12] = 0.0f;
		m[1] = u.x;  m[5] = u.y;  m[9]  = u.z; 	m[13] = 0.0f;
		m[2] = f.x;  m[6] = f.y;  m[10] = f.z; 	m[14] = 0.0f;
		m[3] = 0.0f; m[7] = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		
		float tanHalfFOV = (float) Math.tan(fov / 2);
		float zRange = zFar - zNear;

		m[0] = 1.0f / (tanHalfFOV * aspectRatio); m[4] = 0.0f; 				 m[8]  = 0.0f; 			 m[12] = 0.0f;
		m[1] = 0.0f; 							  m[5] = -1.0f / tanHalfFOV; m[9]  = 0.0f;			 m[13] = 0.0f;
		m[2] = 0.0f; 							  m[6] = 0.0f; 				 m[10] = zFar / zRange; m[14] = -zNear * zFar / zRange;
		m[3] = 0.0f; 							  m[7] = 0.0f; 				 m[11] = 1.0f;			 m[15] = 0.0f;
		
		return new Matrix4f().initScaling(1, 1, 1).mul(this);
	}

	public Matrix4f initOrthographic(float left, float right, float bottom, float top, float near, float far) {
		float width = right - left;
		float height = top - bottom;
		float depth = far - near;

		m[0] = 2/width; m[4] = 0;		 m[8]  = 0;		   m[12] = -(right + left)/width;
		m[1] = 0;		m[5] = 2/height; m[9]  = 0;		   m[13] = -(top + bottom)/height;
		m[2] = 0;		m[6] = 0;		 m[10] = -2/depth; m[14] = -(far + near)/depth;
		m[3] = 0;		m[7] = 0;		 m[11] = 0;		   m[15] = 1;

		return this;
	}
	
	public Matrix4f mul(Matrix4f r) {
		Matrix4f res = new Matrix4f();
		
		for(int i = 0; i < 4; i++) { //Iterate over rows
			for(int j = 0; j < 4; j++) { //Iterate over columns
				float sum = 0.0f;
				for (int k = 0; k < 4; k++) {
					sum += m[k * 4 + i] * r.get(k, j);
				}
            	res.set(i, j, sum);
			}
		}

		return res;
	}
	
	public void set(int i, int j, float value) {
		this.m[i + j * 4] = value;
	}
	
	public float get(int i, int j) {
		return m[i + j* 4];
	}

	public float[] getFloats() {
		return m;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 4; i++) { //Iterate over rows
			for(int j = 0; j < 4; j++) { //Iterate over columns
				sb.append("[" + m[i + j * 4] + "] ");
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
