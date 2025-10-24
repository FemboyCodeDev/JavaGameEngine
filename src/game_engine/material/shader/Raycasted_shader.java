package game_engine.material.shader;

import game_engine.math.Float3;

public class Raycasted_shader extends Shader {
    @Override
    public Float3 fragment(FragmentData f)
    {
        return new Float3((float)f.mat.renderData*255,0,0);
        //return f.tex.sample(f.texUV);
    }

}
