package ca.artemis.engine.core.math;

public class Vector3f {

	public float x;
	public float y;
	public float z;
	
	public Vector3f() {
		this(0.0f, 0.0f, 0.0f);
	}
	
	public Vector3f(Vector3f vector) {
		this(vector.x, vector.y, vector.z);
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float dot(Vector3f r) {
		return x * r.getX() + y * r.getY() + z * r.getZ();
	}
	
	public Vector3f cross(Vector3f vector) {
		float x = this.y * vector.z - this.z * vector.y;
		float y = this.z * vector.x - this.x * vector.z;
		float z = this.x * vector.y - this.y * vector.x;
		
		this.x = x;
		this.y = y;
		this.z = z;

		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public Vector3f normalize() {
		float length = length();
		
		this.x = this.x / length;
		this.y = this.y / length;
		this.z = this.z / length;

		return this;
	}
	
	public Vector3f rotate(Vector3f axis, float angle) {
		float sinAngle = (float)Math.sin(-angle);
		float cosAngle = (float)Math.cos(-angle);
		
		return this.cross(axis.mul(sinAngle)).add(this.mul(cosAngle)).add(axis.mul(this.dot(axis.mul(1 - cosAngle))));
	}
	
	public Vector3f rotate(Quaternion rotation) {
		Quaternion conjugate = rotation.conjugate();
		Quaternion w = rotation.mul(this).mul(conjugate);
		
		return new Vector3f(w.getX(), w.getY(), w.getZ());
	}
	
	public Vector3f add(Vector3f r) {
		return new Vector3f(x + r.getX(), y + r.getY(), z + r.getZ());
	}

	public Vector3f add(float x, float y, float z) {
		this.x = this.x + x;
		this.y = this.y + y;
		this.z = this.z + z;

		return this;
	}

	public Vector3f sub(Vector3f vector) {
		this.x = this.x - vector.x;
		this.y = this.y - vector.y;
		this.z = this.z - vector.z;

		return this;
	}

	public Vector3f sub(float r) {
		return new Vector3f(x - r, y - r, z - r);
	}
	
	public Vector3f mul(Vector3f r) {
		return new Vector3f(x * r.getX(), y * r.getY(), z * r.getZ());
	}

	public Vector3f mul(float r) {
		return new Vector3f(x * r, y * r, z * r);
	}
	
	public Vector3f div(Vector3f r) {
		return new Vector3f(x / r.getX(), y / r.getY(), z / r.getZ());
	}

	public Vector3f div(float r) {
		return new Vector3f(x / r, y / r, z / r);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Vector3f other = (Vector3f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}
}
