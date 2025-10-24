package game_engine.physics;

import game_engine.math.Float3;

public class raycast {
    public Float3 raycast(Float3 origin, Float3 direction, double distance){

        for (int i=0;i < distance;i++){
            Float3 checkpoint = origin.add(direction.scale(i));
            if (collision.collision(checkpoint)){
                return checkpoint;
            }
        }
        return new Float3(0,0,0);
    }
}
