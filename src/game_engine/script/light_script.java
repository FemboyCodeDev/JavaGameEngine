package game_engine.script;

import game_engine.math.Float3;
import game_engine.math.Maths;
import game_engine.physics.raycast;
import game_engine.physics.raycast_result;
import game_engine.scene.GameObject;
import game_engine.scene.Scene;
import game_engine.scene.Transform;

public class light_script extends Script {
    int wait_time = 0;
    @Override
    public void update(GameObject obj) {
        wait_time += 1;
        if (wait_time < 10){
            return;
        }
        wait_time = 0;
    for (int x=235;x<325-45; x += 10){
        for (int y=0;y<360; y += 36){

            Transform temp_transform = new Transform(0,0,0,0,0,0,0,0,0);

            temp_transform.rot = new Float3(0,y,x);
            temp_transform.updateRotation();

            Float3 direction = Maths.rotate(new Float3(1,0,0), temp_transform);

            //direction = new Float3 (0,-1,0);

            raycast_result ray_result = raycast.raycast(obj.transform.pos,direction,20);
            //System.out.println(ray_result.collision);
            String objName = "light_obj" + x + y;
            //System.out.println(objName);
            //System.out.println(Scene.getObject(objName));
            if (Scene.getObject(objName) == null){
                Scene.add(new GameObject(objName,"debug_marker",x, y, 0f, 0f, 0f, 0f, 0.5f,obj.mat,null));

            }
            Scene.getObject(objName).transform.pos = ray_result.position;
            //Scene.getObject(objName).transform.pos = obj.transform.pos.add(direction.scale(10));
            if (ray_result.collision){


            }



        }
        }
    }
}
