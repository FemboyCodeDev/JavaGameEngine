package game_engine.material;

import game_engine.material.shader.Shader;
import game_engine.material.shader.UnlitShader;
import game_engine.math.Float2;

public class Material {
    public Shader shader;
    public Texture tex;
    public Float2 scale;
    public int renderData;
    public Material() {
        this.shader = new UnlitShader();
        this.tex = new Texture(2, 2);
        this.scale = new Float2(1f, 1f);
    }
    public Material(Shader shader, Texture texture, float scaleX, float scaleY) {
        this.shader = shader;
        this.tex = texture;
        this.scale = new Float2(scaleX, scaleY);
        this.renderData = 0;
    }
    public Material(Shader shader, Texture texture, float scaleX, float scaleY,int renderData) {
        this.shader = shader;
        this.tex = texture;
        this.scale = new Float2(scaleX, scaleY);
        this.renderData = renderData;
    }

    public Float2 convertUV(Float2 uv) {
        return uv.multiply(scale);
    }
}
