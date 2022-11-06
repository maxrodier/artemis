package ca.artemis.engine.core.math;

import java.nio.FloatBuffer;

public class Matrix4f {

	private static boolean DEPTH_ZERO_TO_ONE = true;

	public static int SIZE = 16;
	public static int BYTES = SIZE * Float.BYTES;
	
	private float[] m;

	public Matrix4f() {
		this.m = new float[16];
	}
	
	public Matrix4f initIdentity() {
		m[0] = 1.0f; m[4]  = 0.0f; m[8]  = 0.0f; m[12] = 0.0f;
		m[1] = 0.0f; m[5]  = 1.0f; m[9]  = 0.0f; m[13] = 0.0f;
		m[2] = 0.0f; m[6]  = 0.0f; m[10] = 1.0f; m[14] = 0.0f;
		m[3] = 0.0f; m[7]  = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initTranslation(float x, float y, float z) {
		m[0] = 1.0f; m[4]  = 0.0f; m[8]  = 0.0f; m[12] = x;
		m[1] = 0.0f; m[5]  = 1.0f; m[9]  = 0.0f; m[13] = y;
		m[2] = 0.0f; m[6]  = 0.0f; m[10] = 1.0f; m[14] = z;
		m[3] = 0.0f; m[7]  = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
	public Matrix4f initScaling(float x, float y, float z) {
		m[0] = x;    m[4]  = 0.0f; m[8]  = 0.0f; m[12] = 0.0f;
		m[1] = 0.0f; m[5]  = y;    m[9]  = 0.0f; m[13] = 0.0f;
		m[2] = 0.0f; m[6]  = 0.0f; m[10] = z;    m[14] = 0.0f;
		m[3] = 0.0f; m[7]  = 0.0f; m[11] = 0.0f; m[15] = 1.0f;
		
		return this;
	}
	
    /* 
	public Matrix4f initRotation(float x, float y, float z) {
		
		Matrix4f rx = new Matrix4f();
		Matrix4f ry = new Matrix4f();
		Matrix4f rz = new Matrix4f();
		
		x = (float) Math.toRadians(x);
		y = (float) Math.toRadians(y);
		z = (float) Math.toRadians(z);
		
		rx.m[0] = 1.0f; 				rx.m[1]  = 0.0f; 					rx.m[2]  = 0.0f; 					rx.m[3]  = 0.0f;
		rx.m[4] = 0.0f; 				rx.m[5]  = (float) Math.cos(x); 	rx.m[6]  = (float) -Math.sin(x);	rx.m[7]  = 0.0f;
		rx.m[8] = 0.0f; 				rx.m[9]  = (float) Math.sin(x); 	rx.m[10] = (float) Math.cos(x);		rx.m[11] = 0.0f;
		rx.m[12] = 0.0f; 				rx.m[13] = 0.0f; 					rx.m[14] = 0.0f; 					rx.m[15] = 1.0f;
		
		ry.m[0] = (float) Math.cos(y);	ry.m[1]  = 0.0f; 					ry.m[2]  = (float) -Math.sin(y);  	ry.m[3]  = 0.0f;
		ry.m[4] = 0.0f; 				ry.m[5]  = 1.0f;					ry.m[6]  = 0.0f;					ry.m[7]  = 0.0f;
		ry.m[8] = (float) Math.sin(y);	ry.m[9]  = 0.0f; 					ry.m[10] = (float) Math.cos(y);		ry.m[11] = 0.0f;
		ry.m[12] = 0.0f; 				ry.m[13] = 0.0f; 					ry.m[14] = 0.0f; 					ry.m[15] = 1.0f;
		
		rz.m[0] = (float) Math.cos(z);	rz.m[1]  = (float) -Math.sin(z);	rz.m[2]  = 0.0f; 					rz.m[3]  = 0.0f;
		rz.m[4] = (float) Math.sin(z);	rz.m[5]  = (float) Math.cos(z); 	rz.m[6]  = 0.0f;					rz.m[7]  = 0.0f;
		rz.m[8] = 0.0f; 				rz.m[9]  = 0.0f; 					rz.m[10] = 1.0f;					rz.m[11] = 0.0f;
		rz.m[12] = 0.0f; 				rz.m[13] = 0.0f; 					rz.m[14] = 0.0f; 					rz.m[15] = 1.0f;

		m = rz.mul(ry.mul(rx)).getM();
		
		return this;
	}
    */
	
    /* 
	public Matrix4f initRotation(Vector3f f, Vector3f u, Vector3f r) {
		m[0]  = r.getX(); 	m[1]  = r.getY(); 	m[2]  = r.getZ(); 	m[3]  = 0.0f;
		m[4]  = u.getX(); 	m[5]  = u.getY(); 	m[6]  = u.getZ(); 	m[7]  = 0.0f;
		m[8]  = f.getX(); 	m[9]  = f.getY(); 	m[10] = f.getZ(); 	m[11] = 0.0f;
		m[12] = 0.0f;		m[13] = 0.0f; 		m[14] = 0.0f; 		m[15] = 1.0f;
		
		return this;
	}
	*/

	public Matrix4f initPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		
		float tanHalfFOV = (float) Math.tan(fov / 2);
		float zRange = zFar - zNear;
		
		m[0] = 1.0f / (aspectRatio * tanHalfFOV);	m[4] = 0.0f; 				m[8]  = 0.0f; 						m[12] = 0.0f;
		m[1] = 0.0f; 								m[5] = 1.0f / tanHalfFOV; 	m[9]  = 0.0f;						m[13] = 0.0f;
		m[2] = 0.0f; 								m[6] = 0.0f; 				m[10] = -(zNear +zFar) / zRange;	m[14] = -2.0f * zFar * zNear / zRange;
		m[3] = 0.0f; 								m[7] = 0.0f; 				m[11] = -1.0f;						m[15] = 0.0f;

		return this;
	}

	public Matrix4f initOrthographic(float left, float right, float bottom, float top, float near, float far) {
		float width = right - left;
		float height = top - bottom;
		float depth = far - near;

		m[0] = 2/width;	m[4]  = 0;		    m[8]  = 0;											m[12] = -(right + left)/width;
		m[1] = 0;		m[5]  = 2/height;	m[9]  = 0;											m[13] = -(top + bottom)/height; //TODO: m31 -(top + bottom)/height might be a bug ....
		m[2] = 0;		m[6]  = 0;			m[10] = -(DEPTH_ZERO_TO_ONE ? 1f : 2.0f)/depth;		m[14] = -(DEPTH_ZERO_TO_ONE ? near : (far + near))/depth;
		m[3] = 0;		m[7]  = 0;			m[11] = 0;											m[15] = 1;

		return this;
	}

    public Matrix4f setLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f f = new Vector3f(center).sub(eye).normalize();
        Vector3f s = new Vector3f(f).cross(up).normalize();
        Vector3f u = new Vector3f(s).cross(f);

		m[0] = s.x;		m[4] = s.y;		m[8]  = s.z;	m[12] = -(s.dot(eye));
        m[1] = u.x;		m[5] = u.y;		m[9]  = u.z;	m[13] = -(u.dot(eye));
        m[2] = -f.x;	m[6] = -f.y;	m[10] = -f.z;	m[14] = f.dot(eye);
		m[3] = 0;		m[7] = 0;		m[11] = 0;	    m[15] = 1;

        return this;
    }
    
	/* 
	public Matrix4f mul(Matrix4f r) {
		Matrix4f res = new Matrix4f();
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				res.set(i, j, m[i * 4 + 0] * r.get(0, j) +
								m[i * 4 + 1] * r.get(1, j) +
								m[i * 4 + 2] * r.get(2, j) +
								m[i * 4 + 3] * r.get(3, j));
			}
		}
		
		return res;
	}
	*/

	public void set(int column, int row, float value) {
		this.m[column * 4 + row] = value;
	}
	
	public float get(int column, int row) {
		return m[column * 4 + row];
	}

	public float[] get(float[] array) {
		for(int column = 0; column < 4; column++) {
			for(int row = 0; row < 4; row++) {
				array[column * 4 + row ] = m[column * 4 + row];
			}
		}
		
		return array;	
	}

    public FloatBuffer get(FloatBuffer floatBuffer, int offset) {
		for(int column = 0; column < 4; column++) {
        	for(int row = 0; row < 4; row++) {
                floatBuffer.put(column * 4 + row + offset, m[column * 4 + row]);
			}
		}
        return floatBuffer;
    }

	public void print() {
		for(int row = 0; row < 4; row++) {
			System.out.print("|");
        	for(int column = 0; column < 4; column++) {
                System.out.print(" " + m[column * 4 + row] + " ");
			}
			System.out.print("|");
			System.out.println();
		}
	}
}
