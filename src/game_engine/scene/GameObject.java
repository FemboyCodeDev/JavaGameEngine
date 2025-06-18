package game_engine.scene;

import game_engine.material.Material;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.script.Script;
import game_engine.material.shader.Shader;

public class GameObject {
    public Transform transform;
    public Material mat;
    public Script script;
    private final String modelKey;
    public GameObject(String modelKey, float x, float y, float z, float pitch, float yaw, float roll, float scale, Material material, Script script) {
        this.transform = new Transform(x, y, z, pitch, yaw, roll, scale);
        this.mat = material;
        this.script = script;
        this.modelKey = modelKey;
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
