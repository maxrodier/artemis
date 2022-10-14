package ca.artemis;

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
	
	public Quaternion(Vec3 axis, float angle) {
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
	
	public Quaternion normalized() {
		float length = length();
		
		return new Quaternion(x / length, y / length, z / length, w / length);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(-x, -y, -z, w);
	}
	
	public Quaternion mul(Quaternion r) {
		float x = this.x * r.getW() + this.w * r.x + this.y * r.z - this.z * r.y;
		float y = this.y * r.getW() + this.w * r.y + this.z * r.x - this.x * r.z;
		float z = this.z * r.getW() + this.w * r.z + this.x * r.y - this.y * r.x;
		float w = this.w * r.getW() - this.x * r.x - this.y * r.y - this.z * r.z;
		
		return new Quaternion(x, y, z, w);
	}
	
	public Quaternion mul(Vec3 r) {
		float w = -this.x * r.x - this.y * r.y - this.z * r.z;
		float x =  this.w * r.x + this.y * r.z - this.z * r.y;
		float y =  this.w * r.y + this.z * r.x - this.x * r.z;
		float z =  this.w * r.z + this.x * r.y - this.y * r.x;
		
		return new Quaternion(x, y, z, w);
	}

	public mat4 toRotationMatrix() {
		Vec3 forward =  new Vec3(2.0f * (x * z - w * y), 2.0f * (y * z + w * x), 1.0f - 2.0f * (x * x + y * y));
		Vec3 up = new Vec3(2.0f * (x * y + w * z), 1.0f - 2.0f * (x * x + z * z), 2.0f * (y * z - w * x));
		Vec3 right = new Vec3(1.0f - 2.0f * (y * y + z * z), 2.0f * (x * y - w * z), 2.0f * (x * z + w * y));

		return new mat4().initRotation(forward, up, right);
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