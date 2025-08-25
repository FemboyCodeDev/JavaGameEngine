package game_engine.material.shader.debug;

import game_engine.material.shader.FragmentData;
import game_engine.material.shader.Shader;
import game_engine.math.Float3;

public class DebugShader extends Shader {
    @Override
    public Float3 fragment(FragmentData f) {
        double l = f.weights.to2D().length();
        if ((l < .35d) || (l > .65d)) {
            return discard;
        }
        return f.screenUV.to3D(1f).scale(Math.min(1f, 4f / f.depth));
    }
}
