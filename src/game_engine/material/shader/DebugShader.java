package game_engine.material.shader;

import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;

public class DebugShader extends Shader {
    @Override
    public Float3 fragment(Texture tex, Float2 texUV, Float3 worldNormal, float depth, Float3 weights, Float2 screenUV) {
        double l = weights.to2D().length();
        if ((l < .35d) || (l > .65d)) {
            return discard;
        }
        return screenUV.to3D(1f).scale(Math.min(1f, 4f / depth));
    }
}
