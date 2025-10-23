package game_engine.physics;
import game_engine.math.Float3;
import game_engine.scene.Scene;
import game_engine.scene.GameObject;

public class basic_collision {

    public static boolean collision(Float3 pos) {
        GameObject[] objs = Scene.getObjects();

        for (GameObject obj:objs){

            if (obj.modelKey == "cube"){
                Float3 deltapos = pos.sub(obj.transform.pos);
                System.out.println(deltapos);
                return true;
            }
        }

        return true;





}
}
