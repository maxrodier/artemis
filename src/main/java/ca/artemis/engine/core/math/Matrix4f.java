package ca.artemis.engine.core.math;

//Matric4f class
public class Matrix4f {
    
    //Matrix4f variables
    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    //Constructors
    public Matrix4f() {
        this(1, 0, 0, 0,
             0, 1, 0, 0,
             0, 0, 1, 0,
             0, 0, 0, 1);
    }

    public Matrix4f(float m00, float m01, float m02, float m03,
                    float m10, float m11, float m12, float m13,
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        set(m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33);
    }

    //Setters
    public Matrix4f set(float m00, float m01, float m02, float m03,
                    float m10, float m11, float m12, float m13,
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;

        return this;
    }

    public Matrix4f set(Matrix4f m) {
        return set(m.m00, m.m01, m.m02, m.m03,
                    m.m10, m.m11, m.m12, m.m13,
                    m.m20, m.m21, m.m22, m.m23,
                    m.m30, m.m31, m.m32, m.m33);
    }

    //Identity
    public Matrix4f identity() {
        return set(1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1);
    }

    //Translation
    public Matrix4f translation(float x, float y, float z) {
        return set(1, 0, 0, x,
                    0, 1, 0, y,
                    0, 0, 1, z,
                    0, 0, 0, 1);
    }

    public Matrix4f translation(Vector3f v) {
        return translation(v.x, v.y, v.z);
    }

    //Scaling
    public Matrix4f scaling(float x, float y, float z) {
        return set(x, 0, 0, 0,
                    0, y, 0, 0,
                    0, 0, z, 0,
                    0, 0, 0, 1);
    }

    public Matrix4f scaling(Vector3f v) {
        return scaling(v.x, v.y, v.z);
    }

    //Rotation
    public Matrix4f rotation(float x, float y, float z) {
        float sinX = (float) Math.sin(x);
        float cosX = (float) Math.cos(x);
        float sinY = (float) Math.sin(y);
        float cosY = (float) Math.cos(y);
        float sinZ = (float) Math.sin(z);
        float cosZ = (float) Math.cos(z);

        return set(cosY * cosZ, -cosX * sinZ + sinX * sinY * cosZ, sinX * sinZ + cosX * sinY * cosZ, 0,
                    cosY * sinZ, cosX * cosZ + sinX * sinY * sinZ, -sinX * cosZ + cosX * sinY * sinZ, 0,
                    -sinY, sinX * cosY, cosX * cosY, 0,
                    0, 0, 0, 1);
    }

    public Matrix4f rotation(Quaternion q) {
        float x = q.x;
        float y = q.y;
        float z = q.z;
        float w = q.w;

        float xx = x * x;
        float xy = x * y;
        float xz = x * z;
        float xw = x * w;

        float yy = y * y;
        float yz = y * z;
        float yw = y * w;

        float zz = z * z;
        float zw = z * w;

        return set(1 - 2 * (yy + zz), 2 * (xy - zw), 2 * (xz + yw), 0,
                    2 * (xy + zw), 1 - 2 * (xx + zz), 2 * (yz - xw), 0,
                    2 * (xz - yw), 2 * (yz + xw), 1 - 2 * (xx + yy), 0,
                    0, 0, 0, 1);
    }

    //Perspective
    public Matrix4f perspective(float fov, float aspect, float zNear, float zFar) {
        float tanHalfFOV = (float) Math.tan(fov / 2);
        float zRange = zNear - zFar;

        return set(1.0f / (tanHalfFOV * aspect), 0, 0, 0,
                    0, 1.0f / tanHalfFOV, 0, 0,
                    0, 0, (-zNear - zFar) / zRange, 2 * zFar * zNear / zRange,
                    0, 0, 1, 0);
    }

    //Orthographic
    public Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {
        float width = right - left;
        float height = top - bottom;
        float depth = far - near;

        return set(2 / width, 0, 0, -(right + left) / width,
                    0, 2 / height, 0, -(top + bottom) / height,
                    0, 0, -2 / depth, -(far + near) / depth,
                    0, 0, 0, 1);
    }

    //Addition
    public Matrix4f add(Matrix4f m) {
        return set(this.m00 + m.m00, this.m01 + m.m01, this.m02 + m.m02, this.m03 + m.m03,
                    this.m10 + m.m10, this.m11 + m.m11, this.m12 + m.m12, this.m13 + m.m13,
                    this.m20 + m.m20, this.m21 + m.m21, this.m22 + m.m22, this.m23 + m.m23,
                    this.m30 + m.m30, this.m31 + m.m31, this.m32 + m.m32, this.m33 + m.m33);
    }

    //Subtraction
    public Matrix4f sub(Matrix4f m) {
        return set(this.m00 - m.m00, this.m01 - m.m01, this.m02 - m.m02, this.m03 - m.m03,
                    this.m10 - m.m10, this.m11 - m.m11, this.m12 - m.m12, this.m13 - m.m13,
                    this.m20 - m.m20, this.m21 - m.m21, this.m22 - m.m22, this.m23 - m.m23,
                    this.m30 - m.m30, this.m31 - m.m31, this.m32 - m.m32, this.m33 - m.m33);
    }

    //Multiplication
    public Matrix4f mul(Matrix4f m) {
        float m00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20 + this.m03 * m.m30;
        float m01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21 + this.m03 * m.m31;
        float m02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22 + this.m03 * m.m32;
        float m03 = this.m00 * m.m03 + this.m01 * m.m13 + this.m02 * m.m23 + this.m03 * m.m33;
        float m10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20 + this.m13 * m.m30;
        float m11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31;
        float m12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32;
        float m13 = this.m10 * m.m03 + this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33;
        float m20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20 + this.m23 * m.m30;
        float m21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31;
        float m22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32;
        float m23 = this.m20 * m.m03 + this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33;
        float m30 = this.m30 * m.m00 + this.m31 * m.m10 + this.m32 * m.m20 + this.m33 * m.m30;
        float m31 = this.m30 * m.m01 + this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31;
        float m32 = this.m30 * m.m02 + this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32;
        float m33 = this.m30 * m.m03 + this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33;

        return set(m00, m01, m02, m03,
                    m10, m11, m12, m13,
                    m20, m21, m22, m23,
                    m30, m31, m32, m33);
    }

    //To Array
    public float[] toArray() {
        return new float[] {
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        };
    }

    //To formated String
    @Override
    public String toString() {
        return "Matrix4f: \n" +
                m00 + " " + m01 + " " + m02 + " " + m03 + "\n" +
                m10 + " " + m11 + " " + m12 + " " + m13 + "\n" +
                m20 + " " + m21 + " " + m22 + " " + m23 + "\n" +
                m30 + " " + m31 + " " + m32 + " " + m33 + "\n";
    }
}
