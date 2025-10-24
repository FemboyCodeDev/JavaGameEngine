package game_engine.physics;

import game_engine.math.Float3;
import game_engine.scene.GameObject;
import game_engine.scene.Scene;
import game_engine.scene.Model;
import game_engine.physics.RigidTransform3D;

public class collision {
    public static boolean collision(Float3 pos){
        return basic_collision.collision(pos);

    }

    public static GameObject collision_object(Float3 pos,double size){
        return basic_collision.collision_object(pos,size);

    }

    public static void uv_collision(Float3 pos){
        GameObject obj = collision_object(pos,2);
        System.out.println(obj.name);
        if (obj.name != "none"){
            System.out.println("COLLISION IN UV COLLISION");
            Model model = obj.getModel();
        }

    }
}
