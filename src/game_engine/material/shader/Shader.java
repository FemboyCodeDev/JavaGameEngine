package game_engine.material.shader;

import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.scene.Scene;

public abstract class Shader {
    protected static final Float3 discard = new Float3(-1f, 0f, 0f);

    public abstract Float3 fragment(FragmentData f);

    protected static float calculateLightIntensity(Float3 normal) {
        float intensity = (Maths.dotProduct(normal, Scene.dirToSun) + 1f) * .5f;
        return Maths.lerp(intensity, 1f, Scene.envLight);
    }
}
