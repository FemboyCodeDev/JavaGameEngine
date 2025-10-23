package game_engine.physics;
import game_engine.math.Float3;


/*

void PM_Accelerate (vec3_t wishdir, float wishspeed, float accel)
{
	int			i;
	float		addspeed, accelspeed, currentspeed;

	if (pmove.dead)
		return;
	if (pmove.waterjumptime)
		return;

	currentspeed = DotProduct (pmove.velocity, wishdir);
	addspeed = wishspeed - currentspeed;
	if (addspeed <= 0)
		return;
	accelspeed = accel*frametime*wishspeed;
	if (accelspeed > addspeed)
		accelspeed = addspeed;

	for (i=0 ; i<3 ; i++)
		pmove.velocity[i] += accelspeed*wishdir[i];
}
 */
public class pmove {
    public static void PM_Accelerate(Float3 wishdir,Float3 veclocity, float wishspeed, float accel,float frametime){

        float currentspeed = veclocity.dotProduct(wishdir);
        float addspeed = wishspeed-currentspeed;
        if (addspeed <= 0){
            return;
        }
        float accelspeed = accel*frametime*wishspeed;
        if (accelspeed>addspeed){
            accelspeed = addspeed;
        }

        veclocity.x += accelspeed*wishdir.x;
        veclocity.y += accelspeed*wishdir.y;
        veclocity.z += accelspeed*wishdir.z;



    }
}
