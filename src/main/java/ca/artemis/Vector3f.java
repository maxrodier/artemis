package ca.artemis;

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
}
