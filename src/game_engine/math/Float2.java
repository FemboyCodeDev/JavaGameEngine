package game_engine.math;

public class Float2 {
    public float x;
    public float y;

    public Float2() {
        this.x = 0f;
        this.y = 0f;
    }
    public Float2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void offset(float dx, float dy) {
        x += dx;
        y += dy;
    }
    public void offset(Float2 f2) {
        offset(f2.x, f2.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Float2 add(Float2 f2) {
        return new Float2(x + f2.x, y + f2.y);
    }
    public Float2 sub(Float2 f2) {
        return new Float2(x - f2.x, y - f2.y);
    }
    public Float2 scale(double s) {
        return new Float2((float) (x * s), (float) (y * s));
    }
    public Float2 multiply(Float2 f2) {
        return new Float2(x * f2.x, y * f2.y);
    }
    public Float2 rotate90() {
        return new Float2(y, -x);
    }
    public Float2 inverse() {
        return new Float2(1 / x, 1 / y);
    }
    public Float2 modulo(float v) {
        return new Float2(x % v, y % v);
    }
    public Float2 normalize() {
        double length = length();
        return (length == 0d) ? new Float2() : scale(1 / length);
    }

    public Float3 to3D() {
        return new Float3(x, y, 0f);
    }
    public Float3 to3D(float z) {
        return new Float3(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("%f : %f", x, y);
    }
}
