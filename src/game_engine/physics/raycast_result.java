package game_engine.physics;

import game_engine.math.Float3;

public class raycast_result {
    public boolean collision;
    public Float3 position;
    public Float3 direction;
    public Float3 origin;
    public double distance;

    public raycast_result(boolean collision, Float3 position, Float3 direction, Float3 origin) {
        this.collision = collision;
        this.position = position;
        this.direction = direction;
        this.origin = origin;
        this.distance = distance;
    }
    public raycast_result(boolean collision, Float3 position, Float3 origin) {
        this(collision, position, new Float3(), origin);
    }
    public raycast_result(boolean collision, Float3 position) {
        this(collision, new Float3(), position);
    }
    public raycast_result(boolean collision) {
        this(collision, new Float3(), new Float3(), new Float3());

    }

}
