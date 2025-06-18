package game_engine.math;

public class Float3 {
    public float x;
    public float y;
    public float z;

    public Float3() {
        this.x = 0f;
        this.y = 0f;
        this.z = 0f;
    }
    public Float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void offset(float dx, float dy, float dz) {
        x += dx;
        y += dy;
        z += dz;
    }
    public void offset(Float3 f3) {
        offset(f3.x, f3.y, f3.z);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Float3 add(Float3 f3) {
        return new Float3(x + f3.x, y + f3.y, z + f3.z);
    }
    public Float3 sub(Float3 f3) {
        return new Float3(x - f3.x, y - f3.y, z - f3.z);
    }
    public Float3 scale(double s) {
        return new Float3((float) (x * s), (float) (y * s), (float) (z * s));
    }
    public Float3 multiply(Float3 f3) {
        return new Float3(x * f3.x, y * f3.y, z * f3.z);
    }
    public Float3 inverse() {
        return new Float3(1 / x, 1 / y, 1 / z);
    }
    public Float3 modulo(float v) {
        return new Float3(x % v, y % v, z % v);
    }
    public Float3 normalize() {
        double length = length();
        return (length == 0d) ? new Float3() : scale(length);
    }

    public Float2 to2D() {
        return new Float2(x, y);
    }
    public int getColor() {
        int r = (int) (x * 255);
        int g = (int) (y * 255);
        int b = (int) (z * 255);
        return ((r << 16) | (g << 8) | b);
    }

    @Override
    public String toString() {
        return String.format("%f : %f : %f", x, y, z);
    }
}
