package ca.artemis.engine.core.math;

//Vector2f class
public class Vector2f {
    
    //Vector2f variables
    public float x, y;

    //Constructors
    public Vector2f() {
        this(0, 0);
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    //Setters
    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public Vector2f set(Vector2f v) {
        this.x = v.x;
        this.y = v.y;

        return this;
    }

    //Addition
    public Vector2f add(Vector2f v) {
        this.x += v.x;
        this.y += v.y;

        return this;
    }

    public Vector2f add(float f) {
        this.x += f;
        this.y += f;

        return this;
    }

    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;

        return this;
    }

    //Subtraction
    public Vector2f sub(Vector2f v) {
        this.x -= v.x;
        this.y -= v.y;

        return this;
    }

    public Vector2f sub(float f) {
        this.x -= f;
        this.y -= f;

        return this;
    }

    public Vector2f sub(float x, float y) {
        this.x -= x;
        this.y -= y;

        return this;
    }

    //Multiplication
    public Vector2f mul(Vector2f v) {
        this.x *= v.x;
        this.y *= v.y;

        return this;
    }

    public Vector2f mul(float f) {
        this.x *= f;
        this.y *= f;

        return this;
    }

    public Vector2f mul(float x, float y) {
        this.x *= x;
        this.y *= y;

        return this;
    }

    //Division
    public Vector2f div(Vector2f v) {
        this.x /= v.x;
        this.y /= v.y;

        return this;
    }

    public Vector2f div(float f) {
        this.x /= f;
        this.y /= f;

        return this;
    }

    public Vector2f div(float x, float y) {
        this.x /= x;
        this.y /= y;

        return this;
    }

    //Dot product
    public float dot(Vector2f v) {
        return this.x * v.x + this.y * v.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    //Length
    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    //Normalize
    public Vector2f normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;

        return this;
    }

    //Rotate
    public void rotate(float angle) {
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        this.set(this.x * cos - this.y * sin, this.x * sin + this.y * cos);
    }

    //Normalized
    public Vector2f normalized() {
        float length = this.length();
        return new Vector2f(this.x / length, this.y / length);
    }

    //To formated String
    @Override
    public String toString() {
        return "Vector2f: (" + this.x + ", " + this.y + ")";
    }
}
