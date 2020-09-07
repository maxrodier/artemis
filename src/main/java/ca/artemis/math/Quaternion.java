package ca.artemis.math;

public class Quaternion {
	
	private float x;
	private float y;
	private float z;
	private float w;
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion(Vector3f axis, float angle) {
		float sinHalfAngle = (float) Math.sin(angle / 2);
		float cosHalfAngle = (float) Math.cos(angle / 2);
		
		this.x = axis.getX() * sinHalfAngle;
		this.y = axis.getY() * sinHalfAngle;
		this.z = axis.getZ() * sinHalfAngle;
		this.w = cosHalfAngle;
	}
	
	public float length() {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	public Quaternion normalized() {
		float length = length();
		
		return new Quaternion(x / length, y / length, z / length, w / length);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}
	
	public Quaternion mul(Quaternion r) {
		float x = this.x * r.getW() + this.w * r.getX() + this.y * r.getZ() - this.z * r.getY();
		float y = this.y * r.getW() + this.w * r.getY() + this.z * r.getX() - this.x * r.getZ();
		float z = this.z * r.getW() + this.w * r.getZ() + this.x * r.getY() - this.y * r.getX();
		float w = this.w * r.getW() - this.x * r.getX() - this.y * r.getY() - this.z * r.getZ();
		
		return new Quaternion(x, y, z, w);
	}
	
	public Quaternion mul(Vector3f r) {
		float w = -this.x * r.getX() - this.y * r.getY() - this.z * r.getZ();
		float x =  this.w * r.getX() + this.y * r.getZ() - this.z * r.getY();
		float y =  this.w * r.getY() + this.z * r.getX() - this.x * r.getZ();
		float z =  this.w * r.getZ() + this.x * r.getY() - this.y * r.getX();
		
		return new Quaternion(x, y, z, w);
	}

	public Matrix4f toRotationMatrix() {
		Vector3f forward =  new Vector3f(2.0f * (x * z - w * y), 2.0f * (y * z + w * x), 1.0f - 2.0f * (x * x + y * y));
		Vector3f up = new Vector3f(2.0f * (x * y + w * z), 1.0f - 2.0f * (x * x + z * z), 2.0f * (y * z - w * x));
		Vector3f right = new Vector3f(1.0f - 2.0f * (y * y + z * z), 2.0f * (x * y - w * z), 2.0f * (x * z + w * y));

		return new Matrix4f().initRotation(forward, up, right);
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
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