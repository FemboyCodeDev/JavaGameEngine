package game_engine.material.shader;

import game_engine.math.Float3;

public class UnlitShader extends Shader {
    @Override
    public Float3 fragment(FragmentData f) {
        return f.tex.sample(f.texUV);
    }
}
