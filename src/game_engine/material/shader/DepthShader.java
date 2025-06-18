package game_engine.material.shader;

import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;

public class DepthShader extends Shader {
    @Override
    public Float3 fragment(Texture tex, Float2 texUV, Float3 worldNormal, float depth, Float3 weights, Float2 screenUV) {
        float d = depth / 50f;
        if (d > 1f) {
            return new Float3(.75f, 1f, 1f);
        }
        return new Float3(d, d, d);
    }
}
