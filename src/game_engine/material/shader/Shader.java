package game_engine.material.shader;

import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.scene.Scene;

public abstract class Shader {
    protected static final Float3 discard = new Float3(-1f, 0f, 0f);

    public abstract Float3 fragment(Texture tex, Float2 texUV, Float3 worldNormal, float depth, Float3 weights, Float2 screenUV);

    protected static float getLightingIntensity(Float3 normal) {
        float intensity = (Maths.dotProduct(normal, Scene.dirToSun) + 1f) * .5f;
        return (intensity * (1f - Scene.envLight)) + Scene.envLight;
    }
}
