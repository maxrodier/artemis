package ca.artemis;

public class Vec3 {
    
    public static final int BYTES = Float.BYTES + Float.BYTES + Float.BYTES;

    public float x;
    public float y;
    public float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
