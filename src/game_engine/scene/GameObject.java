package game_engine.scene;

import game_engine.material.Material;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.script.Script;

public class GameObject {
    public boolean active;
    public String name;
    public Transform transform;
    public Material mat;
    public Script script;
    public String modelKey;
    public GameObject(String name, String model, float xp, float yp, float zp, float pr, float yr, float rr, float xs, float ys, float zs, Material mat, Script script) {
        this.active = true;
        this.name = name;
        this.modelKey = model;
        this.transform = new Transform(xp, yp, zp, pr, yr, rr, xs, ys, zs);
        this.mat = mat;
        this.script = script;
    }
    public GameObject(String name, String model, float xp, float yp, float zp, float pr, float yr, float rr, float s, Material mat, Script script) {
        this(name, model, xp, yp, zp, pr, yr, rr, s, s, s, mat, script);
    }
    public GameObject(String name, String model, float xp, float yp, float zp, float pr, float yr, float rr, Material mat, Script script) {
        this(name, model, xp, yp, zp, pr, yr, rr, 1f, mat, script);
    }
    public GameObject(String name, String model, float xp, float yp, float zp, Material mat, Script script) {
        this(name, model, xp, yp, zp, 0f, 0f, 0f, mat, script);
    }
    public GameObject(String name, String model, Material mat, Script script) {
        this(name, model, 0f, 0f, 0f, mat, script);
    }
    public GameObject(String name) {
        this(name, "", null, null);
    }


    public Model getModel() {
        return Scene.getModel(modelKey);
    }

    public int triangleCount() {
        return getModel().triangleCount();
    }
    public Float3[] getTriVertexes(int i) {
        return Maths.transformTri(getModel().getTriVertexes(i), transform);
    }
    public Float2[] getTriUVs(int i) {
        return getModel().getTriUVs(i);
    }
    public Float3 getTriNormal(int i) {
        return Maths.rotate(getModel().getTriNormal(i), transform);
    }

    public void updateScript() {
        if (script != null) {
            script.update(this);
        }
    }
}
