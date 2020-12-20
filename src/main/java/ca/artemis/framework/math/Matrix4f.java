package ca.artemis.framework.math;

public class Matrix4f {

	public static int SIZE = 16;
	public static int BYTES = SIZE * Float.BYTES;
	
	public float[][] m = new float[4][4];
	
	public Matrix4f initIdentity() {
		m[0][0] = 1.0f; m[0][1] = 0.0f; m[0][2] = 0.0f; m[0][3] = 0.0f;
		m[1][0] = 0.0f; m[1][1] = 1.0f; m[1][2] = 0.0f; m[1][3] = 0.0f;
		m[2][0] = 0.0f; m[2][1] = 0.0f; m[2][2] = 1.0f; m[2][3] = 0.0f;
		m[3][0] = 0.0f; m[3][1] = 0.0f; m[3][2] = 0.0f; m[3][3] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initTranslation(float x, float y, float z) {
		m[0][0] = 1.0f; m[0][1] = 0.0f; m[0][2] = 0.0f; m[0][3] = x;
		m[1][0] = 0.0f; m[1][1] = 1.0f; m[1][2] = 0.0f; m[1][3] = y;
		m[2][0] = 0.0f; m[2][1] = 0.0f; m[2][2] = 1.0f; m[2][3] = z;
		m[3][0] = 0.0f; m[3][1] = 0.0f; m[3][2] = 0.0f; m[3][3] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initScaling(float x, float y, float z) {
		m[0][0] = x; 	m[0][1] = 0.0f; m[0][2] = 0.0f; m[0][3] = 0.0f;
		m[1][0] = 0.0f; m[1][1] = y;	m[1][2] = 0.0f; m[1][3] = 0.0f;
		m[2][0] = 0.0f; m[2][1] = 0.0f; m[2][2] = z; 	m[2][3] = 0.0f;
		m[3][0] = 0.0f; m[3][1] = 0.0f; m[3][2] = 0.0f; m[3][3] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initRotation(float x, float y, float z) {
		
		Matrix4f rx = new Matrix4f();
		Matrix4f ry = new Matrix4f();
		Matrix4f rz = new Matrix4f();
		
		x = (float) Math.toRadians(x);
		y = (float) Math.toRadians(y);
		z = (float) Math.toRadians(z);
		
		rx.m[0][0] = 1.0f; 					rx.m[0][1] = 0.0f; 					rx.m[0][2] = 0.0f; 					rx.m[0][3] = 0.0f;
		rx.m[1][0] = 0.0f; 					rx.m[1][1] = (float) Math.cos(x); 	rx.m[1][2] = (float) -Math.sin(x);	rx.m[1][3] = 0.0f;
		rx.m[2][0] = 0.0f; 					rx.m[2][1] = (float) Math.sin(x); 	rx.m[2][2] = (float) Math.cos(x);	rx.m[2][3] = 0.0f;
		rx.m[3][0] = 0.0f; 					rx.m[3][1] = 0.0f; 					rx.m[3][2] = 0.0f; 					rx.m[3][3] = 1.0f;
		

		ry.m[0][0] = (float) Math.cos(y);	ry.m[0][1] = 0.0f; 					ry.m[0][2] = (float) -Math.sin(y);  ry.m[0][3] = 0.0f;
		ry.m[1][0] = 0.0f; 					ry.m[1][1] = 1.0f;				 	ry.m[1][2] = 0.0f;					ry.m[1][3] = 0.0f;
		ry.m[2][0] = (float) Math.sin(y);	ry.m[2][1] = 0.0f; 					ry.m[2][2] = (float) Math.cos(y);	ry.m[2][3] = 0.0f;
		ry.m[3][0] = 0.0f; 					ry.m[3][1] = 0.0f; 					ry.m[3][2] = 0.0f; 					ry.m[3][3] = 1.0f;
		
		rz.m[0][0] = (float) Math.cos(z);	rz.m[0][1] = (float) -Math.sin(z);	rz.m[0][2] = 0.0f; 					rz.m[0][3] = 0.0f;
		rz.m[1][0] = (float) Math.sin(z);	rz.m[1][1] = (float) Math.cos(z); 	rz.m[1][2] = 0.0f;					rz.m[1][3] = 0.0f;
		rz.m[2][0] = 0.0f; 					rz.m[2][1] = 0.0f; 					rz.m[2][2] = 1.0f;					rz.m[2][3] = 0.0f;
		rz.m[3][0] = 0.0f; 					rz.m[3][1] = 0.0f; 					rz.m[3][2] = 0.0f; 					rz.m[3][3] = 1.0f;

		m = rz.mul(ry.mul(rx)).getM();
		
		return this;
	}
	
	public Matrix4f initRotation(Vector3f f, Vector3f u, Vector3f r) {
		m[0][0] = r.getX(); m[0][1] = r.getY(); m[0][2] = r.getZ(); m[0][3] = 0.0f;
		m[1][0] = u.getX(); m[1][1] = u.getY(); m[1][2] = u.getZ(); m[1][3] = 0.0f;
		m[2][0] = f.getX(); m[2][1] = f.getY(); m[2][2] = f.getZ(); m[2][3] = 0.0f;
		m[3][0] = 0.0f; 	m[3][1] = 0.0f; 	m[3][2] = 0.0f; 	m[3][3] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		
		float tanHalfFOV = (float) Math.tan(fov / 2);
		float zRange = zNear - zFar;
		
		m[0][0] = 1.0f / (tanHalfFOV * aspectRatio);	m[0][1] = 0.0f; 				m[0][2] = 0.0f; 					m[0][3] = 0.0f;
		m[1][0] = 0.0f; 								m[1][1] = -1.0f / tanHalfFOV; 	m[1][2] = 0.0f;						m[1][3] = 0.0f;
		m[2][0] = 0.0f; 								m[2][1] = 0.0f; 				m[2][2] = (-zNear -zFar) / zRange;	m[2][3] = 2.0f * zFar * zNear / zRange;
		m[3][0] = 0.0f; 								m[3][1] = 0.0f; 				m[3][2] = 1.0f; 					m[3][3] = 0.0f;
		
		return this;
	}
	
	
	public Matrix4f mul(Matrix4f r) {
		Matrix4f res = new Matrix4f();
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				res.set(i, j, m[i][0] * r.get(0, j) +
								m[i][1] * r.get(1, j) +
								m[i][2] * r.get(2, j) +
								m[i][3] * r.get(3, j));
			}
		}
		
		return res;
	}
	
	public void set(int i, int j, float value) {
		this.m[i][j] = value;
	}
	
	public float get(int i, int j) {
		return m[i][j];
	}
	
	public float[][] getM() {
		float[][] res = new float[4][4];
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				res[i][j] = m[i][j];
			}
		}
		
		return res;
	}
}
