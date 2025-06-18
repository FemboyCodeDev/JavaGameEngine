package game_engine.material.shader;

import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;

public class UnlitShader extends Shader {
    @Override
    public Float3 fragment(Texture tex, Float2 texUV, Float3 worldNormal, float depth, Float3 weights, Float2 screenUV) {
        return tex.sample(texUV);
    }
}
