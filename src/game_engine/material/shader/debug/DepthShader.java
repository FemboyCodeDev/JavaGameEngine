package game_engine.material.shader.debug;

import game_engine.material.shader.FragmentData;
import game_engine.material.shader.Shader;
import game_engine.math.Float3;

public class DepthShader extends Shader {
    @Override
    public Float3 fragment(FragmentData f) {
        float d = f.depth / 50f;
        if (d > 1f) {
            return new Float3(.75f, 1f, 1f);
        }
        return new Float3(d, d, d);
    }
}
