package game_engine.physics;

import game_engine.math.Float3;

public class player_phys {
    public static Float3 camera_pos = new Float3(0,0,0);
    public static Float3 PlayerVel = new Float3(0,0,0);
    public static Float3 accel_dir = new Float3(0,0,0); // This comes from keyboard input
    public void updatePhysics(float DeltaTime) {
        // Constants (you should define these as class fields or final variables)
        float gravity_strength = -9.0f;
        float jump_strength = 400.0f + 9.0f; // Note: Jump logic is usually in input handling
        //jump_strength = 0;
        float friction = 10.0f;
        friction = 50;
        float max_velocity = 1000.0f;
        float accelerate = 300.0f;
        float player_height = 2.0f;
        float ground_level = -2.0f;

        // --- State Variables (Assumed to be class fields) ---


        boolean EnablePhysics = true;


        if (EnablePhysics) {
            // 1. Ground Check
            boolean onGround = camera_pos.y - (player_height / 2.0f) <= ground_level;

            // 2. Friction
            if (onGround) {
                // Calculate 2D speed (ignoring vertical component for ground movement)
                float speed = (float) Math.sqrt(PlayerVel.x * PlayerVel.x + PlayerVel.z * PlayerVel.z);

                if (speed != 0.0f) {
                    float drop = speed * friction * DeltaTime;

                    // Calculate new 2D speed, ensuring it's not negative
                    float newSpeed = Math.max(speed - drop, 0.0f);

                    // Scale the horizontal velocity components
                    float ratio = newSpeed / speed;
                    PlayerVel.x *= ratio;
                    PlayerVel.z *= ratio;
                }
            }

            // 3. Movement Acceleration
            // accel_dir is set by keyboard input logic elsewhere
            accel_dir = accel_dir.normalize(); // Ensure the direction vector is unit length

            float projVel = PlayerVel.dotProduct(accel_dir); // Dot product of current velocity and desired direction
            float accelVel = accelerate * DeltaTime;

            if (projVel + accelVel > max_velocity) {
                accelVel = Math.max(max_velocity - projVel, 0.0f);
            }

            // Add the acceleration to the current velocity
            PlayerVel = PlayerVel.add(accel_dir.scale(accelVel));


            // 4. Gravity
            PlayerVel.y += gravity_strength * DeltaTime;

            // 5. Apply Movement
            camera_pos = camera_pos.add(PlayerVel.scale(DeltaTime));

            // 6. Ground Clamping
            if (camera_pos.y - (player_height / 2.0f) < ground_level) {
                // Snap position to ground level
                camera_pos.y = ground_level + (player_height / 2.0f);
                // Stop downward velocity
                if (PlayerVel.y < 0) {
                    PlayerVel.y = 0;
                }
            }
        }
    }

}
