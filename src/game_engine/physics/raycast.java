package game_engine.physics;

import game_engine.math.Float3;

public class raycast {
    public static raycast_result raycast(Float3 origin, Float3 direction, double distance){
        Float3 checkpoint = origin;
        for (double i=0;i < distance;i+=0.1){
             checkpoint = origin.add(direction.scale(i));
            if (collision.collision(checkpoint)){
                return new raycast_result(true, checkpoint, origin);
            }
        }
        return new raycast_result(false, checkpoint, origin);
        //return new raycast_result(false);
    }
}
