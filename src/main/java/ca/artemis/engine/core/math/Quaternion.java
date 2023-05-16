package ca.artemis.engine.core.math;

//Quaternion class
public class Quaternion {
    
    //Quaternion components
    public float x, y, z, w;

    //Constructors
    public Quaternion() {
        this(0, 0, 0, 1);
    }

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    //Setters
    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    public Quaternion set(Quaternion q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;

        return this;
    }

    //Addition
    public Quaternion add(Quaternion q) {
        this.x += q.x;
        this.y += q.y;
        this.z += q.z;
        this.w += q.w;

        return this;
    }

    //Subtraction
    public Quaternion sub(Quaternion q) {
        this.x -= q.x;
        this.y -= q.y;
        this.z -= q.z;
        this.w -= q.w;

        return this;
    }

    //Multiplication
    public Quaternion mul(Quaternion q) {
        float x = this.x * q.w + this.y * q.z - this.z * q.y + this.w * q.x;
        float y = -this.x * q.z + this.y * q.w + this.z * q.x + this.w * q.y;
        float z = this.x * q.y - this.y * q.x + this.z * q.w + this.w * q.z;
        float w = -this.x * q.x - this.y * q.y - this.z * q.z + this.w * q.w;

        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    public Quaternion mul(float x, float y, float z, float w) {
        float _x = this.x * w + this.y * z - this.z * y + this.w * x;
        float _y = -this.x * z + this.y * w + this.z * x + this.w * y;
        float _z = this.x * y - this.y * x + this.z * w + this.w * z;
        float _w = -this.x * x - this.y * y - this.z * z + this.w * w;

        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.w = _w;

        return this;
    }

    public Quaternion mul(Vector3f v) {
        float x = this.w * v.x + this.y * v.z - this.z * v.y;
        float y = this.w * v.y + this.z * v.x - this.x * v.z;
        float z = this.w * v.z + this.x * v.y - this.y * v.x;
        float w = -this.x * v.x - this.y * v.y - this.z * v.z;

        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    //Division
    public Quaternion div(Quaternion q) {
        float x = this.x * q.w - this.y * q.z + this.z * q.y + this.w * q.x;
        float y = this.x * q.z + this.y * q.w - this.z * q.x + this.w * q.y;
        float z = -this.x * q.y + this.y * q.x + this.z * q.w + this.w * q.z;
        float w = this.x * q.x + this.y * q.y + this.z * q.z + this.w * q.w;

        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    public Quaternion div(float x, float y, float z, float w) {
        float _x = this.x * w - this.y * z + this.z * y + this.w * x;
        float _y = this.x * z + this.y * w - this.z * x + this.w * y;
        float _z = -this.x * y + this.y * x + this.z * w + this.w * z;
        float _w = this.x * x + this.y * y + this.z * z + this.w * w;

        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.w = _w;

        return this;
    }

    //Normalize
    public Quaternion normalize() {
        float length = length();

        x /= length;
        y /= length;
        z /= length;
        w /= length;

        return this;
    }

    //Conjugate
    public Quaternion conjugate() {
        x = -x;
        y = -y;
        z = -z;

        return this;
    }

    //Inverse
    public Quaternion inverse() {
        return conjugate().normalize();
    }

    //Rotation
    public Quaternion rotate(float angle, float x, float y, float z) {
        float sinHalfAngle = (float) Math.sin(Math.toRadians(angle / 2));
        float cosHalfAngle = (float) Math.cos(Math.toRadians(angle / 2));

        float rx = x * sinHalfAngle;
        float ry = y * sinHalfAngle;
        float rz = z * sinHalfAngle;
        float rw = cosHalfAngle;

        Quaternion rotation = new Quaternion(rx, ry, rz, rw);
        return mul(rotation);
    }

    //Rotation
    public Quaternion rotate(Vector3f axis, float angle) {
        float sinHalfAngle = (float) Math.sin(Math.toRadians(angle / 2));
        float cosHalfAngle = (float) Math.cos(Math.toRadians(angle / 2));

        float rx = axis.x * sinHalfAngle;
        float ry = axis.y * sinHalfAngle;
        float rz = axis.z * sinHalfAngle;
        float rw = cosHalfAngle;

        Quaternion rotation = new Quaternion(rx, ry, rz, rw);
        return mul(rotation);
    }

    //Rotation
    public void rotate(Vector3f forward, Vector3f up) {
        Vector3f f = forward.normalized();
        Vector3f r = up.normalized();
        r = r.cross(f);
        Vector3f u = f.cross(r);

        float m00 = r.x;
        float m01 = r.y;
        float m02 = r.z;
        float m10 = u.x;
        float m11 = u.y;
        float m12 = u.z;
        float m20 = f.x;
        float m21 = f.y;
        float m22 = f.z;

        float trace = m00 + m11 + m22;

        if (trace > 0) {
            float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
            w = 0.25f / s;
            x = (m12 - m21) * s;
            y = (m20 - m02) * s;
            z = (m01 - m10) * s;
        } else {
            if (m00 > m11 && m00 > m22) {
                float s = 2.0f * (float) Math.sqrt(1.0f + m00 - m11 - m22);
                w = (m12 - m21) / s;
                x = 0.25f * s;
                y = (m01 + m10) / s;
                z = (m02 + m20) / s;
            } else if (m11 > m22) {
                float s = 2.0f * (float) Math.sqrt(1.0f + m11 - m00 - m22);
                w = (m20 - m02) / s;
                x = (m01 + m10) / s;
                y = 0.25f * s;
                z = (m12 + m21) / s;
            } else {
                float s = 2.0f * (float) Math.sqrt(1.0f + m22 - m00 - m11);
                w = (m01 - m10) / s;
                x = (m02 + m20) / s;
                y = (m12 + m21) / s;
                z = 0.25f * s;
            }
        }

        float length = length();

        x /= length;
        y /= length;
        z /= length;
        w /= length;
    }
    
    //Length
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    //get forward vector
    public Vector3f getForward() {
        return new Vector3f(0, 0, 1).rotate(this);
    }

    //To formated String
    @Override
    public String toString() {
        return "Quaternion: (" + x + ", " + y + ", " + z + ", " + w + ")";
    }
}
