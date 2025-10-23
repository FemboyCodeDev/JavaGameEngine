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
    Float3 velocity = new Float3(0,0,0);
    public void PM_Accelerate(Float3 wishdir, float wishspeed, float accel,float frametime){

        float currentspeed = this.velocity.dotProduct(wishdir);
        float addspeed = wishspeed-currentspeed;
        if (addspeed <= 0){
            return;
        }
        float accelspeed = accel*frametime*wishspeed;
        if (accelspeed>addspeed){
            accelspeed = addspeed;
        }

        this.velocity.x += accelspeed*wishdir.x;
        this.velocity.y += accelspeed*wishdir.y;
        this.velocity.z += accelspeed*wishdir.z;



    }

    int numtouch=0;
    boolean spectator = false;
    Float3 angles = new Float3(0,0,0);
    float waterjumptime = 0;
    float waterlevel=0;

    Float3 origin = new Float3(0,0,0);

    void NudgePosition (void)
    {
        Float3	base;
        int		x, y, z;
        int		i;
        //static int		sign[3] = {0, -1, 1};
        Float3 sign = new Float3(0,-1,1); // Replace with Int3 when implemented

        base = this.origin;
        double tempvalue = 0;
        for (i=0 ; i<3 ; i++)
            tempvalue = ((int)(this.origin.getIndex(i)*8)) * 0.125;
            this.origin.setIndex(i,(float)tempvalue);
//	pmove.origin[2] += 0.124;

//	if (pmove.dead)
//		return;		// might be a squished point, so don'y bother
//	if (PM_TestPlayerPosition (pmove.origin) )
//		return;

        for (z=0 ; z<=2 ; z++)
        {
            for (x=0 ; x<=2 ; x++)
            {
                for (y=0 ; y<=2 ; y++)
                {
                    this.origin.x = (float)(base.x + (sign.x * 1.0/8));
                    this.origin.y = (float)(base.y + (sign.y * 1.0/8));
                    this.origin.z = (float)(base.z + (sign.z * 1.0/8));
                    if (PM_TestPlayerPosition (this.origin))
                        return;
                }
            }
        }
        origin = base;
//	Con_DPrintf ("NudgePosition: stuck\n");
    }


        /*
    =============
    PlayerMove

    Returns with origin, angles, and velocity modified in place.

    Numtouch and touchindex[] will be set if any of the physents
    were contacted during the move.
    =============
    */
    public void PlayerMove (float frametime)
    {
        //frametime = pmove.cmd.msec * 0.001;
        this.numtouch = 0;

        AngleVectors (pmove.angles, forward, right, up);

        if (this.spectator)
        {
            SpectatorMove ();
            return;
        }

        NudgePosition ();

        // take angles directly from command
        VectorCopy (this.cmd.angles, this.angles);

        // set onground, watertype, and waterlevel
        PM_CatagorizePosition ();

        if (waterlevel == 2)
            CheckWaterJump ();

        if (this.velocity.z < 0)
            this.waterjumptime = 0;

        if (pmove.cmd.buttons & BUTTON_JUMP)
            JumpButton ();
        else
            pmove.oldbuttons &= ~BUTTON_JUMP;

        PM_Friction ();

        if (waterlevel >= 2)
            PM_WaterMove ();
        else
            PM_AirMove ();

        // set onground, watertype, and waterlevel for final spot
        PM_CatagorizePosition ();
    }
}
