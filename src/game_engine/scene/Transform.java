package game_engine.scene;

import game_engine.math.Float3;
import game_engine.math.Maths;

public class Transform {
    public Float3 pos;
    public Float3 rot;
    public Float3[] basisVectors;
    public Float3 scale;

    public Transform(float xp, float yp, float zp, float pr, float yr, float rr, float xs, float ys, float zs) {
        this.pos = new Float3(xp, yp, zp);
        this.rot = new Float3(pr, yr, rr);
        this.scale = new Float3(xs, ys, zs);
        updateRotation();
    }

    public void move(float dx, float dy, float dz) {
        pos.offset(dx, dy, dz);
    }
    public void move(Float3 f3) {
        pos = pos.add(f3);
    }
    public void rotate(float dp, float dy, float dr) {
        rot.offset(dp, dy, dr);
        updateRotation();
    }
    public void rotate(Float3 f3) {
        rot = rot.add(f3);
        updateRotation();
    }

    public void updateRotation() {
        rot.x %= 360f;
        rot.y %= 360f;
        rot.z %= 360f;
        basisVectors = Maths.getBasisVectors(rot);
    }
}
