package game_engine.render;

import game_engine.material.Material;
import game_engine.material.Texture;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.math.Pair;
import game_engine.scene.GameObject;
import game_engine.scene.Scene;
import game_engine.script.Input;
import game_engine.script.Script;
import game_engine.material.shader.Shader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Camera extends JPanel {
    private static final float NEAR_CLIP_DST = .01f;
    private static final Float3[] STANDARD_WEIGHTS = new Float3[]{
            new Float3(1f, 0f, 0f), new Float3(0f, 1f, 0f), new Float3(0f, 0f, 1f)};

    public static boolean shaderOverride = false;
    public static boolean showOverdraw = false;
    private static final float OVERDRAW_LIMIT = 8f;
    private static final Float3 OVER_OVERDRAW_COLOR = new Float3(1f, .6f, .6f);
    public static boolean shaderStatus = false;
    private static final Float3 SHADER_RETURN = new Float3(1f, 1f, 1f);
    private static final Float3 SHADER_DISCARD = new Float3(1f, 0f, 0f);

    public static float fov;
    private static int resX;
    private static int resY;
    private static int width;
    private static int height;
    private static float[][] depthBuffer;
    private static BufferedImage renderBuffer;

    public Camera(float camFov, int resolutionX, int resolutionY, int screenWidth, int screenHeight) {
        fov = camFov;
        resX = resolutionX;
        resY = resolutionY;
        width = screenWidth;
        height = screenHeight;
        depthBuffer = new float[resX][resY];
        renderBuffer = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_RGB);


    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderBuffer.setRGB(0, 0, resX, resY, new int[resX * resY], 0, resX);
        depthBuffer = new float[resX][resY];
        for (int i = 0; i < depthBuffer.length; i++) {
            float[] filler = new float[depthBuffer[i].length];
            Arrays.fill(filler, Float.MAX_VALUE);
            depthBuffer[i] = filler;
        }
        g.setColor(Color.WHITE);

        Script.updateDeltaTime();
        Scene.updateCamera();

        for (GameObject object : Scene.getObjects()) {
            object.updateScript();
            renderObject(object);
        }

        Input.updateInput();

        g.drawImage(renderBuffer.getScaledInstance(width, height, BufferedImage.SCALE_FAST), 0, 0, null);
        g.drawString(String.format("fps: %.2f", 1 / Script.deltaTime), 5, 15);
        g.drawString(String.format("%s%s%s",
                shaderOverride ? "shaderOverride " : "", showOverdraw ? "showOverdraw " : "", shaderStatus ? "shaderStatus " : ""), 5, 30);
        if (shaderOverride) {
            g.drawString(Scene.camera.mat.shader.getClass().getSimpleName(), 5, 50);
        } else if (shaderStatus) {
            g.drawString("white - regular return", 5, 50);
            g.drawString("red   - discarded fragment", 5, 65);
        }
    }

    private static void renderObject(GameObject object) {
        for (int t = 0; t < object.triangleCount(); t++) {
            Float2[] UVs = object.getTriUVs(t);
            Float3 worldNormal = object.getTriNormal(t);
            Float3[] tri = Maths.triToView(object.getTriVertexes(t), Scene.camera.transform);
            Material mat = object.mat;
            renderTriangle(tri, worldNormal, UVs, (mat == null) ? Scene.errorMat : mat);
        }
    }

    private static void renderTriangle(Float3[] tri, Float3 worldNormal, Float2[] UVs, Material mat) {
        Float3[] vectors = Maths.getInverseBasisVectors(Scene.camera.transform);
        Float3 viewNormal = Maths.rotate(worldNormal, vectors[0], vectors[1], vectors[2]);
        boolean facingCam = (Maths.dotProduct(viewNormal, tri[0]) < 0f);

        if (facingCam) {
            boolean clipA = (tri[0].z <= NEAR_CLIP_DST);
            boolean clipB = (tri[1].z <= NEAR_CLIP_DST);
            boolean clipC = (tri[2].z <= NEAR_CLIP_DST);
            int clipCount = Maths.boolToInt(clipA) + Maths.boolToInt(clipB) + Maths.boolToInt(clipC);

            switch (clipCount) {
                case 0:
                    drawTriangle(triToScreen(tri), STANDARD_WEIGHTS, worldNormal, UVs, mat);
                    break;
                case 1:
                    int clipIndex = (clipA ? 0 : (clipB ? 1 : 2));
                    int nextI = (clipIndex + 1) % 3;
                    int prevI = (clipIndex + 2) % 3;
                    Float3 clippedP = tri[clipIndex];
                    Float3 A = tri[nextI];
                    Float3 B = tri[prevI];

                    float fracA = (NEAR_CLIP_DST - clippedP.z) / (A.z - clippedP.z);
                    float fracB = (NEAR_CLIP_DST - clippedP.z) / (B.z - clippedP.z);
                    Float3 clipPointA = clippedP.lerp(A, fracA);
                    Float3 clipPointB = clippedP.lerp(B, fracB);

                    Float3 weightA = STANDARD_WEIGHTS[clipIndex].lerp(STANDARD_WEIGHTS[nextI], fracA);
                    Float3 weightB = STANDARD_WEIGHTS[clipIndex].lerp(STANDARD_WEIGHTS[prevI], fracB);
                    Float3[] weights1 = new Float3[]{STANDARD_WEIGHTS[nextI], STANDARD_WEIGHTS[prevI], weightB};
                    Float3[] weights2 = new Float3[]{STANDARD_WEIGHTS[nextI], weightA, weightB};

                    drawTriangle(triToScreen(A, B, clipPointB), weights1, worldNormal, UVs, mat);
                    drawTriangle(triToScreen(A, clipPointA, clipPointB), weights2, worldNormal, UVs, mat);
                    break;
                case 2:
                    int nonClipI = ((!clipA) ? 0 : ((!clipB) ? 1 : 2));
                    int clipIA = (nonClipI + 1) % 3;
                    int clipIB = (nonClipI + 2) % 3;
                    Float3 P = tri[nonClipI];
                    Float3 clippedA = tri[clipIA];
                    Float3 clippedB = tri[clipIB];

                    float fracX = (NEAR_CLIP_DST - P.z) / (clippedA.z - P.z);
                    float fracY = (NEAR_CLIP_DST - P.z) / (clippedB.z - P.z);
                    Float3 clipPointX = P.lerp(clippedA, fracX);
                    Float3 clipPointY = P.lerp(clippedB, fracY);

                    Float3 weightX = STANDARD_WEIGHTS[nonClipI].lerp(STANDARD_WEIGHTS[clipIA], fracX);
                    Float3 weightY = STANDARD_WEIGHTS[nonClipI].lerp(STANDARD_WEIGHTS[clipIB], fracY);
                    Float3[] weights = new Float3[]{STANDARD_WEIGHTS[nonClipI], weightX, weightY};

                    drawTriangle(triToScreen(P, clipPointX, clipPointY), weights, worldNormal, UVs, mat);
                    break;
                default:
                    break;
            }
        }
    }
    private static void drawTriangle(Float3[] tri, Float3[] triWeights, Float3 worldNormal, Float2[] UVs, Material mat) {
        Float2 a = tri[0].to2D();
        Float2 b = tri[1].to2D();
        Float2 c = tri[2].to2D();
        Float3 depths = new Float3(tri[0].z, tri[1].z, tri[2].z);

        float minX = Math.min(Math.min(a.x, b.x), c.x);
        float minY = Math.min(Math.min(a.y, b.y), c.y);
        float maxX = Math.max(Math.max(a.x, b.x), c.x);
        float maxY = Math.max(Math.max(a.y, b.y), c.y);
        int startX = Maths.clamp(0, resX - 1, (int) minX);
        int startY = Maths.clamp(0, resY - 1, (int) minY);
        int endX = Maths.clamp(0, resX - 1, (int) maxX);
        int endY = Maths.clamp(0, resY - 1, (int) maxY);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Float2 p = new Float2(x, y);
                Pair<Boolean, Float3> triTest = Maths.pointTriangleTest(a, b, c, p);
                if (triTest.a) {
                    float depth = 1f / Maths.dotProduct(triTest.b, depths.inverse());
                    if (showOverdraw || (depth < depthBuffer[x][y])) {
                        Float2 screenUV = new Float2(x / ((float) resX), y / ((float) resY));
                        Float3 weights = (triWeights[0].scale(triTest.b.x / depths.x)
                                .add(triWeights[1].scale(triTest.b.y / depths.y))
                                .add(triWeights[2].scale(triTest.b.z / depths.z))).scale(depth);
                        Float2 texUV = mat.convertUV(UVs[0].scale(weights.x).add(UVs[1].scale(weights.y)).add(UVs[2].scale(weights.z)));
                        Float3 shaderCol = new Float3(1f, 0f, 1f);

                        Shader shader = shaderOverride ? Scene.camera.mat.shader : mat.shader;
                        if (shader != null) {
                            Texture tex = (mat.tex == null) ? Scene.errorMat.tex : mat.tex;
                            shaderCol = shader.fragment(tex, texUV, worldNormal, depth, weights, screenUV);

                            if (shaderStatus) {
                                if (shaderCol.x < 0f) shaderCol = SHADER_DISCARD;
                                else shaderCol = SHADER_RETURN;
                            } else {
                                if (shaderCol.x < 0f) continue;
                            }
                        }

                        if (showOverdraw) {
                            depth = depthBuffer[x][y];
                            if (depth == Float.MAX_VALUE) {
                                depth = 1f;
                            } else {
                                depth += 1f;
                            }
                            shaderCol = (depth > OVERDRAW_LIMIT) ? OVER_OVERDRAW_COLOR : (new Float3(depth, depth, depth).scale(1 / OVERDRAW_LIMIT));
                            }
                        renderBuffer.setRGB(x, y, shaderCol.getColor());
                        depthBuffer[x][y] = depth;
                    }
                }
            }
        }
    }
    private static Float3[] triToScreen(Float3 A, Float3 B, Float3 C) {
        return Maths.triToScreen(A, B, C, resX, resY, fov);
    }
    private static Float3[] triToScreen(Float3[] tri) {
        return Maths.triToScreen(tri[0], tri[1], tri[2], resX, resY, fov);
    }

    public void startRendering() {
        new Thread(() -> {
            while (true) {
                try {
                    repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
