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
    double	STOP_EPSILON =0.1;
    int numtouch=0;
    boolean spectator = false;
    Float3 angles = new Float3(0,0,0);
    float waterjumptime = 0;
    float waterlevel=0;

    Float3 origin = new Float3(0,0,0);

    int PM_ClipVelocity (Float3 in, Float3 normal, Float3 out, float overbounce)
    {
        float	backoff;
        float	change;
        int		i, blocked;

        blocked = 0;
        if (normal.z > 0){
            blocked |= 1;		// floor
            }
        //if (!normal.z){
          //  blocked |= 2;	}	// step

        //backoff = DotProduct (in, normal) * overbounce;
        backoff = normal.dotProduct(in)*overbounce;
        for (i=0 ; i<3 ; i++)
        {
            change = normal.getIndex(i)*backoff;
            out.setIndex(i,in.getIndex(i) - change);
            if (out.getIndex(i) > -STOP_EPSILON && out.getIndex(i) < STOP_EPSILON)
                out.setIndex(i,0);
        }

        return blocked;
    }


    void NudgePosition ()
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
                    //if (PM_TestPlayerPosition (this.origin))
                     //   return;
                }
            }
        }
        origin = base;
//	Con_DPrintf ("NudgePosition: stuck\n");
    }

    Float3 accelerate = new Float3(0,0,0);

    void PM_AirMove ()
    {
        int			i;
        Float3		wishvel;
        float		fmove, smove;
        Float3		wishdir;
        float		wishspeed;

        fmove = pmove.cmd.forwardmove;
        smove = pmove.cmd.sidemove;

        forward[2] = 0;
        right[2] = 0;
        VectorNormalize (forward);
        VectorNormalize (right);

        for (i=0 ; i<2 ; i++)
            wishvel[i] = forward[i]*fmove + right[i]*smove;
        wishvel[2] = 0;

        VectorCopy (wishvel, wishdir);
        wishspeed = VectorNormalize(wishdir);

//
// clamp to server defined max speed
//
        if (wishspeed > movevars.maxspeed)
        {
            VectorScale (wishvel, movevars.maxspeed/wishspeed, wishvel);
            wishspeed = movevars.maxspeed;
        }

//	if (pmove.waterjumptime)
//		Con_Printf ("am->%f, %f, %f\n", pmove.velocity[0], pmove.velocity[1], pmove.velocity[2]);

        if ( onground != -1)
        {
            this.velocity.z = 0;
            PM_Accelerate (wishdir, wishspeed, this.accelerate);
            pmove.velocity.z -= movevars.entgravity * movevars.gravity * frametime;
            PM_GroundMove ();
        }
        else
        {	// not on ground, so little effect on velocity
            PM_AirAccelerate (wishdir, wishspeed, movevars.accelerate);

            // add gravity
            pmove.velocity[2] -= movevars.entgravity * movevars.gravity * frametime;

            PM_FlyMove ();

        }


        /*
    =============
    PlayerMove

    Returns with origin, angles, and velocity modified in place.

    Numtouch and touchindex[] will be set if any of the physents
    were contacted during the move.
    =============
    */
    public void PlayerMove (float frametime,Float3 cmd_angles)
    {
        //frametime = pmove.cmd.msec * 0.001;
        this.numtouch = 0;

        //AngleVectors (this.angles, forward, right, up);


        if (this.spectator)
        {
            //SpectatorMove ();
            return;
        }

        NudgePosition ();

        // take angles directly from command

        //VectorCopy (this.cmd.angles, this.angles);

        this.angles = cmd_angles;



        // set onground, watertype, and waterlevel
        //PM_CatagorizePosition ();

        if (waterlevel == 2)
            //CheckWaterJump ();

        if (this.velocity.z < 0)
            this.waterjumptime = 0;

        if (pmove.cmd.buttons & BUTTON_JUMP)
            JumpButton ();
        //else
          //  pmove.oldbuttons &= ~BUTTON_JUMP;

        PM_Friction ();

        if (waterlevel >= 2)
            PM_WaterMove ();
        else
            PM_AirMove ();

        // set onground, watertype, and waterlevel for final spot
        PM_CatagorizePosition ();
    }
}
