package game_engine.script;

import game_engine.scene.GameObject;

public abstract class Script {
    public static double deltaTime = 0d;
    private static long lastFrameNanoTime = -1L;
    public static void updateDeltaTime() {
        long currentTime = System.nanoTime();
        if (lastFrameNanoTime != -1L) {
            deltaTime = (currentTime - lastFrameNanoTime) / 1_000_000_000d;
        }
        lastFrameNanoTime = currentTime;
    }

    public abstract void update(GameObject obj);
}
