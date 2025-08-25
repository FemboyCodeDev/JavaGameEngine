package game_engine.material.shader;

import game_engine.material.Material;
import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.scene.Scene;

public class FragmentData {
    public Float2 screenUV;
    public Float3 weights;
    public Material mat;
    public Float2[] triUVs;
    public Float2 texUV;
    public Texture tex;
    public Float3 worldNormal;
    public float depth;

    public FragmentData() {
        this.screenUV = new Float2();
        this.weights = new Float3();
        this.mat = new Material();
        this.triUVs = new Float2[3];
        this.texUV = new Float2();
        this.tex = Scene.errorMat.tex;
        this.worldNormal = new Float3();
        this.depth = Float.MAX_VALUE;
    }
    public FragmentData(Float2 screenUV, Float3 weights, Material mat, Float2[] UVs, Float3 worldNormal, float depth) {
        this.screenUV = screenUV;
        this.weights = weights;
        this.mat = mat;
        this.triUVs = UVs;
        this.texUV = mat.convertUV(UVs[0].scale(weights.x).add(UVs[1].scale(weights.y)).add(UVs[2].scale(weights.z)));
        this.tex = (mat.tex == null) ? Scene.errorMat.tex : mat.tex;
        this.worldNormal = worldNormal;
        this.depth = depth;
    }
}
