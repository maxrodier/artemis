package ca.artemis.engine.maths;

public class Quaternion {
	
	public float x;
	public float y;
	public float z;
	public float w;
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion(Vector3f axis, float angle) {
		float sinHalfAngle = (float) Math.sin(angle / 2);
		float cosHalfAngle = (float) Math.cos(angle / 2);
		
		this.x = axis.x * sinHalfAngle;
		this.y = axis.y * sinHalfAngle;
		this.z = axis.z * sinHalfAngle;
		this.w = cosHalfAngle;
	}
	
	public float length() {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	public void normalize() {
		float length = length();
		
		this.x = this.x / length;
		this.y = this.y / length;
		this.z = this.z / length;
		this.w = this.w / length;
	}

	public Quaternion normalized() {
		float length = length();
		
		return new Quaternion(x / length, y / length, z / length, w / length);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}

	// Quaternion-quaternion multiplication
	public Quaternion mul(Quaternion r) {
		float w = this.w * r.w - this.x * r.x - this.y * r.y - this.z * r.z;
		float x = this.w * r.x + this.x * r.w + this.y * r.z - this.z * r.y;
		float y = this.w * r.y - this.x * r.z + this.y * r.w + this.z * r.x;
		float z = this.w * r.z + this.x * r.y - this.y * r.x + this.z * r.w;
		
		return new Quaternion(x, y, z, w);
	}

	// Quaternion-vector multiplication
	public Quaternion mul(Vector3f r) {
		float w = -this.x * r.x - this.y * r.y - this.z * r.z;
		float x =  this.w * r.x + this.y * r.z - this.z * r.y;
		float y =  this.w * r.y - this.x * r.z + this.z * r.x;
		float z =  this.w * r.z + this.x * r.y - this.y * r.x;
		
		return new Quaternion(x, y, z, w);
	}

	public Vector3f getForward() {
    	// Calculate the components of the forward vector
		float x = 2.0f * (this.x * this.z - this.w * this.y);
        float y = 2.0f * (this.y * this.z + this.w * this.x);
        float z = 1.0f - 2.0f * (this.x * this.x + this.y * this.y);

		Vector3f forward = new Vector3f(x, y, z); // Create a new Vector3f object with the calculated components
		forward.normalize(); // Normalize the forward vector to ensure it has a length of 1
		return forward;
	}

	public Vector3f getUp() {
    	// Calculate the components of the up vector
        float x = 2.0f * (this.x * this.y + this.w * this.z);
        float y = 1.0f - 2.0f * (this.x * this.x + this.z * this.z);
        float z = 2.0f * (this.y * this.z - this.w * this.x);

		Vector3f up = new Vector3f(x, y, z); // Create a new Vector3f object with the calculated components
		up.normalize(); // Normalize the up vector to ensure it has a length of 1
		return up;
	}

	public Vector3f getRight() {
    	// Calculate the components of the right vector
        float x = 1.0f - 2.0f * (this.y * this.y + this.z * this.z);
        float y = 2.0f * (this.x * this.y - this.w * this.z);
        float z = 2.0f * (this.x * this.z + this.w * this.y);

		Vector3f right = new Vector3f(x, y, z); // Create a new Vector3f object with the calculated components
		right.normalize(); // Normalize the right vector to ensure it has a length of 1
		return right;
	}

	public Matrix4f toRotationMatrix() {
		Vector3f forward =  getForward();
		Vector3f up = getUp();
		Vector3f right = getRight();

		return new Matrix4f().initRotation(forward, up, right);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(w);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quaternion other = (Quaternion) obj;
		if (Float.floatToIntBits(w) != Float.floatToIntBits(other.w))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}
}