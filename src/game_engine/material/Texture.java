package game_engine.material;

import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Texture {
    public final int width;
    public final int height;
    private final BufferedImage texture;
    private final Graphics2D g;
    public Texture(int width, int height) {
        this.width = width;
        this.height = height;
        this.texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.g = this.texture.createGraphics();

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, halfWidth, halfHeight);
        g.fillRect(halfWidth, halfHeight, halfWidth, halfHeight);
    }

    public Float3 sample(Float2 uv) {
        int x = Math.min((int) (uv.x * width), width - 1);
        int y = Math.min((int) (uv.y * height), height - 1);
        return Maths.colorVector(texture.getRGB(x, y));
    }

    public void clear() {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    }
    public void fill(int x, int y, int width, int height, int color) {
        g.setColor(new Color(color));
        g.fillRect(x, y, width, height);
    }
}
