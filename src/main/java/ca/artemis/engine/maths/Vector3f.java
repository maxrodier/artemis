package ca.artemis.engine.maths;

public class Vector3f {
    
    public static final int BYTES = Float.BYTES + Float.BYTES + Float.BYTES;

    public float x;
    public float y;
    public float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

	public float length() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}
	
	public void normalize() {
		float length = length();
		
        this.x = this.x / length;
        this.y = this.y / length;
        this.z = this.z / length;
	}

    public Vector3f normalized() {
		float length = length();
		
		return new Vector3f(x / length, y / length, z / length);
	}

    public Vector3f add(Vector3f r) {
        float _x = this.x + r.x;
        float _y = this.y + r.y;
        float _z = this.z + r.z;

        return new Vector3f(_x, _y, _z);
    }

    public Vector3f sub(Vector3f r) {
        float _x = this.x - r.x;
        float _y = this.y - r.y;
        float _z = this.z - r.z;

        return new Vector3f(_x, _y, _z);
    }

    public Vector3f mul(float r) {
        float _x = this.x * r;
        float _y = this.y * r;
        float _z = this.z * r;

        return new Vector3f(_x, _y, _z);
    }

    @Override
	public String toString() {
        return "[" + x  + " " + y + " " + z +"]";
	}
}
