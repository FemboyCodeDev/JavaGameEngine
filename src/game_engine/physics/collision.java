package game_engine.physics;

import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.scene.GameObject;
import game_engine.scene.Scene;
import game_engine.scene.Model;
import game_engine.physics.RigidTransform3D;

import static game_engine.physics.RigidTransform3D.apply_transform;
import static game_engine.physics.RigidTransform3D.rigid_transform_3d;

public class collision {
    public static boolean collision(Float3 pos){
        return basic_collision.collision(pos);

    }

    public static GameObject collision_object(Float3 pos,double size){
        return basic_collision.collision_object(pos,size);

    }

    public static void uv_collision(Float3 pos){
        GameObject obj = collision_object(pos,2);
        //System.out.println(obj.name);
        if (obj.name != "none"){
            //System.out.println("COLLISION IN UV COLLISION");
            Model model = obj.getModel();

            int triangles = model.triangleCount();
            Float3[] points = new Float3[triangles];
            for (int i=0;i<triangles;i++){
                Float3[] triangle = model.getTriVertexes(i);
                Float2[] uv = model.getTriUVs(i);
                Float3[] target = {new Float3(0,0,0),new Float3(10,0,0),new Float3(0,10,0)};
                double[][] T_calculated = rigid_transform_3d(triangle, target);
                Float3 newpoint = apply_transform(T_calculated, new Float3[]{pos})[0];
                points[i] = newpoint;
            }
            //Get point with min Z value
            Float3 uv_value = points[0];
            for (Float3 p : points){
                if (p.z < Math.abs(uv_value.z)){
                    uv_value = p;
                }
            }
            System.out.println("uv value: " + uv_value);
        }

    }
}
