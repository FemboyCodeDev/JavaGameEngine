package game_engine.physics;
import game_engine.math.Float3;
import game_engine.scene.Scene;
import game_engine.scene.GameObject;
import game_engine.math.Maths;
import game_engine.scene.Transform;

public class basic_collision {


    public static boolean collision(Float3 pos) {
        GameObject[] objs = Scene.getObjects();

        for (GameObject obj:objs){

            if (obj.modelKey == "cube"){
                Float3 deltapos = pos.sub(obj.transform.pos);
                deltapos = deltapos.multiply(obj.transform.scale.inverse());

                Transform temp_transform = new Transform(0,0,0,0,0,0,0,0,0);

                temp_transform.rot = obj.transform.rot;
                temp_transform.updateRotation();



                deltapos = Maths.rotate(deltapos, temp_transform);
                //System.out.println(deltapos);
                if (Math.abs(deltapos.x)<1){
                    if (Math.abs(deltapos.y)<1){
                        if (Math.abs(deltapos.z)<1){
                            return true;
                        }
                    }
                }

            }
            //return false;
        }

        return false;





}
    public static GameObject collision_object(Float3 pos){
        return collision_object(pos,1);
    }

    public static GameObject collision_object(Float3 pos,double size) {
        GameObject[] objs = Scene.getObjects();

        for (GameObject obj:objs){

            if (obj.modelKey == "cube"){
                Float3 deltapos = pos.sub(obj.transform.pos);
                deltapos = deltapos.multiply(obj.transform.scale.inverse());

                Transform temp_transform = new Transform(0,0,0,0,0,0,0,0,0);

                temp_transform.rot = obj.transform.rot;
                temp_transform.updateRotation();



                deltapos = Maths.rotate(deltapos, temp_transform);
                //System.out.println(deltapos);
                if (Math.abs(deltapos.x)<size){
                    if (Math.abs(deltapos.y)<size){
                        if (Math.abs(deltapos.z)<size){
                            return obj;
                        }
                    }
                }

            }
            //return false;
        }

        //return false;
        return new GameObject("none");





    }
}
