package game_engine.material.shader;

import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;

public class WireframeShader extends Shader {
    @Override
    public Float3 fragment(Texture tex, Float2 texUV, Float3 worldNormal, float depth, Float3 weights, Float2 screenUV) {
        Float3 bgCol = new Float3(.15f, .15f, .15f);
        Float3 wireCol = new Float3(.8f, .8f, .8f);
        float min = Math.min(Math.min(weights.x, weights.y), weights.z);
        if (min < .04f) {
            return wireCol;
        }
        return bgCol;
    }
}
