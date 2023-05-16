package ca.artemis.engine.core.math;

//Vector3f class
public class Vector3f {
    
    //Vector3f variables
    public float x, y, z;

    //Constructors
    public Vector3f() {
        this(0, 0, 0);
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //Setters
    public Vector3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    public Vector3f set(Vector3f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;

        return this;
    }

    //Addition
    public Vector3f add(Vector3f v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;

        return this;
    }

    public Vector3f add(float f) {
        this.x += f;
        this.y += f;
        this.z += f;

        return this;
    }

    public Vector3f add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    //Subtraction
    public Vector3f sub(Vector3f v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;

        return this;
    }

    public Vector3f sub(float f) {
        this.x -= f;
        this.y -= f;
        this.z -= f;

        return this;
    }

    public Vector3f sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    //Multiplication
    public Vector3f mul(Vector3f v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;

        return this;
    }

    public Vector3f mul(float f) {
        this.x *= f;
        this.y *= f;
        this.z *= f;

        return this;
    }

    public Vector3f mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;

        return this;
    }

    //Division
    public Vector3f div(Vector3f v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;

        return this;
    }

    public Vector3f div(float f) {
        this.x /= f;
        this.y /= f;
        this.z /= f;

        return this;
    }

    public Vector3f div(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;

        return this;
    }

    //Dot product
    public float dot(Vector3f v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    //Cross product
    public Vector3f cross(Vector3f v) {
        return new Vector3f(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    //Length
    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    //Normalize
    public Vector3f normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;

        return this;
    }

    //Rotate around axis
    public Vector3f rotate(float angle, Vector3f axis) {
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        this.x = (cos + (1 - cos) * axis.x * axis.x) * this.x + ((1 - cos) * axis.x * axis.y - axis.z * sin) * this.y + ((1 - cos) * axis.x * axis.z + axis.y * sin) * this.z;
        this.y = ((1 - cos) * axis.x * axis.y + axis.z * sin) * this.x + (cos + (1 - cos) * axis.y * axis.y) * this.y + ((1 - cos) * axis.y * axis.z - axis.x * sin) * this.z;
        this.z = ((1 - cos) * axis.x * axis.z - axis.y * sin) * this.x + ((1 - cos) * axis.y * axis.z + axis.x * sin) * this.y + (cos + (1 - cos) * axis.z * axis.z) * this.z;
        
        return this;
    }

    //Rotate using quaternion
    public Vector3f rotate(Quaternion rotation) {
        Quaternion conjugate = rotation.conjugate();

        Quaternion w = rotation.mul(this).mul(conjugate);

        this.x = w.x;
        this.y = w.y;
        this.z = w.z;

        return this;
    }

    //Getters
    public Vector3f normalized() {
        float length = this.length();
        return new Vector3f(this.x / length, this.y / length, this.z / length);
    }

    //To formated String
    @Override
    public String toString() {
        return "Vector3f: (" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
