package game_engine.script;

import game_engine.material.shader.*;
import game_engine.material.shader.debug.*;
import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.render.Camera;
import game_engine.scene.GameObject;
import game_engine.scene.Scene;
import game_engine.scene.Transform;

import java.awt.event.KeyEvent;

public class CameraScript extends Script {
    private static final float MOVE_SPEED = 4f;
    private static final float ROTATION_SPEED = 900f;

    @Override
    public void update(GameObject obj) {
        Transform transform = obj.transform;

        Float2 mouse = Input.getMouseDelta();
        transform.rot.x -= (float) (mouse.y * ROTATION_SPEED * deltaTime);
        transform.rot.y += (float) (mouse.x * ROTATION_SPEED * deltaTime);
        transform.rot.x = Maths.clamp(-90f, 90f, transform.rot.x);
        transform.updateRotation();

        Float2 movement = Input.movementInput();
        Float3[] vectors = Maths.getBasisVectors(new Float3(0f, transform.rot.y, 0f));
        Float3 f3 = vectors[0].scale(movement.x).add(vectors[2].scale(movement.y));
        transform.move(f3.scale(MOVE_SPEED * deltaTime));

        if (Input.keyDown(KeyEvent.VK_R)) {
            Camera.fov = 30f;
        } else if (Input.keyUp(KeyEvent.VK_R)) {
            Camera.fov = 90f;
        }

        if (Input.keyDown(KeyEvent.VK_E)) {
            Camera.shaderOverride = !Camera.shaderOverride;
        }
        if (Input.keyDown(KeyEvent.VK_Q)) {
            Camera.showOverdraw = !Camera.showOverdraw;
        }
        if (Input.keyDown(KeyEvent.VK_F)) {
            Camera.shaderStatus = !Camera.shaderStatus;
        }

        if (Input.keyDown(KeyEvent.VK_1)) {
            Scene.camera.mat.shader = new UnlitShader();
        } else if (Input.keyDown(KeyEvent.VK_2)) {
            Scene.camera.mat.shader = new WireframeShader();
        } else if (Input.keyDown(KeyEvent.VK_3)) {
            Scene.camera.mat.shader = new DepthShader();
        } else if (Input.keyDown(KeyEvent.VK_4)) {
            Scene.camera.mat.shader = new WeightsShader();
        } else if (Input.keyDown(KeyEvent.VK_5)) {
            Scene.camera.mat.shader = new UVShader();
        } else if (Input.keyDown(KeyEvent.VK_6)) {
            Scene.camera.mat.shader = new NormalsShader();
        } else if (Input.keyDown(KeyEvent.VK_7)) {
            Scene.camera.mat.shader = new DebugShader();
        }
    }
}
