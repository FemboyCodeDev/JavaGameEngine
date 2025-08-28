package game_engine.material.shader.debug;

import game_engine.material.shader.FragmentData;
import game_engine.material.shader.Shader;
import game_engine.math.Float3;

public class WireframeShader extends Shader {
    @Override
    public Float3 fragment(FragmentData f) {
        Float3 bgCol = new Float3(.15f, .15f, .15f);
        Float3 wireCol = new Float3(.8f, .8f, .8f);
        float min = Math.min(Math.min(f.weights.x, f.weights.y), f.weights.z);
        if (min < .025f) {
            return wireCol;
        }
        return bgCol;
    }
}
