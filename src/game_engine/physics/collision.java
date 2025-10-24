package game_engine.physics;

import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.scene.GameObject;
import game_engine.scene.Model;

public class collision {

    // Assuming this utility class exists and works correctly
    public static boolean collision(Float3 pos){
        return basic_collision.collision(pos);
    }

    // Assuming this utility class exists and works correctly
    public static GameObject collision_object(Float3 pos, double size){
        return basic_collision.collision_object(pos, size);
    }

    /**
     * Calculates the UV coordinates of a 3D position (pos) on the surface of a collided object.
     * Uses the closest point projection and Barycentric Coordinates for accurate UV interpolation.
     * @param pos The 3D world position to check.
     * @return The Float2 UV coordinates, or null if no collision occurs.
     */
    public static Float2 uv_collision(Float3 pos){
        GameObject obj = collision_object(pos, 2);

        if (obj.name.equals("none")) {
            return null; // No object found
        }

        Model model = obj.getModel();
        int triangles = model.triangleCount();

        // Find the triangle whose closest point to 'pos' is the nearest one.
        double min_dist_sq = Double.MAX_VALUE;
        Float2 final_uv = null;

        // Iterate through all triangles
        for (int i = 0; i < triangles; i++) {
            Float3[] triangle_verts = model.getTriVertexes(i); // V0, V1, V2
            Float2[] triangle_uvs = model.getTriUVs(i);       // UV0, UV1, UV2

            // Line below is for testing the actual algorithm
            triangle_uvs = new Float2[]{new Float2(0f, 0f), new Float2(1,0f), new Float2(0,1f)};

            // Get the closest point on the triangle's surface (barycentric coords and distance)
            double[] bary_and_dist = closest_point_on_triangle(pos, triangle_verts);

            if (bary_and_dist == null) continue; // Skip degenerate triangles

            double u = bary_and_dist[0]; // weight for V1
            double v = bary_and_dist[1]; // weight for V2
            double w = 1.0 - u - v;      // weight for V0
            double dist_sq = bary_and_dist[2];

            // We only care about the closest point overall
            if (dist_sq < min_dist_sq) {
                min_dist_sq = dist_sq;

                // Interpolate UV coordinates using the guaranteed-valid Barycentric weights (u, v, w)
                // UV = w*UV0 + u*UV1 + v*UV2
                float uv_x = (float) (w * triangle_uvs[0].x + u * triangle_uvs[1].x + v * triangle_uvs[2].x);
                float uv_y = (float) (w * triangle_uvs[0].y + u * triangle_uvs[1].y + v * triangle_uvs[2].y);
                final_uv = new Float2(uv_x, uv_y);
            }
        }

        if (final_uv != null) {
            System.out.println("uv value: " + final_uv);
        } else {
            System.out.println("uv value: none");
        }

        return final_uv;
    }

    /**
     * Finds the closest point on the triangle's SURFACE to the point P and returns
     * the Barycentric coordinates and the squared distance to that closest point.
     * This ensures the returned u and v are always in the valid range [0, 1]
     * and u + v <= 1, even if P is outside the triangle's projection.
     * @param p The 3D point.
     * @param tri Array of 3 vertices [V0, V1, V2].
     * @return double[] {u (weight for V1), v (weight for V2), squared_distance}, or null.
     */
    private static double[] closest_point_on_triangle(Float3 p, Float3[] tri) {
        Float3 v0 = tri[0];
        Float3 v1 = tri[1];
        Float3 v2 = tri[2];

        Float3 edge1 = v1.sub(v0);
        Float3 edge2 = v2.sub(v0);
        Float3 p_v0 = p.sub(v0);

        // Pre-calculate dot products
        double dot00 = edge1.dotProduct(edge1);
        double dot01 = edge1.dotProduct(edge2);
        double dot02 = edge1.dotProduct(p_v0);
        double dot11 = edge2.dotProduct(edge2);
        double dot12 = edge2.dotProduct(p_v0);

        // Check for degenerate triangle (zero area)
        double D = dot00 * dot11 - dot01 * dot01;
        if (Math.abs(D) < 1e-6) {
            return null;
        }

        // 1. Calculate Barycentric coords (u, v) for the projection on the plane
        // u = weight for V1, v = weight for V2 (V0 is the origin)
        double u_proj = (dot11 * dot02 - dot01 * dot12) / D;
        double v_proj = (dot00 * dot12 - dot01 * dot02) / D;

        double u, v;

        // 2. Check if P's projection is inside the triangle
        if (u_proj >= 0 && v_proj >= 0 && (u_proj + v_proj) <= 1) {
            // Case 1: Closest point is inside the triangle face
            u = u_proj;
            v = v_proj;
        } else {
            // Case 2: Closest point is on an edge or vertex. Clamp to the nearest feature.

            // The following logic is an efficient way to find the closest feature
            // and determine the new clamped (u,v) coordinates.

            // Check region outside V0 (u < 0 and v < 0)
            if (u_proj < 0 && v_proj < 0) {
                // Closest point is V0
                u = 0; v = 0;
            }
            // Check region outside V1 (u > 1 and v < 0)
            else if (u_proj > 1 && v_proj < 0) {
                // Closest point is V1
                u = 1; v = 0;
            }
            // Check region outside V2 (v > 1 and u < 0)
            else if (u_proj < 0 && v_proj > 1) {
                // Closest point is V2
                u = 0; v = 1;
            }
            // Check region along Edge V0V1 (u in [0,1], v < 0)
            else if (v_proj < 0 && u_proj >= 0 && u_proj <= 1) {
                // Closest point is on Edge V0V1. Clamp v=0, u is the clamped value.
                u = u_proj; v = 0;
            }
            // Check region along Edge V0V2 (v in [0,1], u < 0)
            else if (u_proj < 0 && v_proj >= 0 && v_proj <= 1) {
                // Closest point is on Edge V0V2. Clamp u=0, v is the clamped value.
                u = 0; v = v_proj;
            }
            // Check region along Edge V1V2 (u+v > 1)
            else if (u_proj + v_proj > 1) {
                // Clamp the projection to the V1-V2 edge
                double edge_v1v2_dot = edge2.sub(edge1).dotProduct(p.sub(v1));
                double edge_v1v2_len_sq = edge2.sub(edge1).dotProduct(edge2.sub(edge1));
                double t = Math.max(0, Math.min(1, edge_v1v2_dot / edge_v1v2_len_sq));

                // Closest point is on Edge V1V2, where V1 is the origin for this edge (t=0)
                // The new clamped barycentric coords:
                u = 1.0 - t; // Weight for V1
                v = t;       // Weight for V2
            }
            // Check region outside V0V1V2 (where u, v, w are all negative/invalid)
            else {
                // If all clamping fails, default to V0 as a safe fallback (or the origin of the edge logic)
                u = 0; v = 0;
            }
        }

        // 3. Calculate the closest point P_closest using the clamped/valid (u, v)
        // P_closest = V0 + u * (V1 - V0) + v * (V2 - V0)
        Float3 p_closest = v0.add(edge1.scale((float)u)).add(edge2.scale((float)v));

        // 4. Calculate the squared distance to this closest point
        // dist_sq = |P - P_closest|^2
        double dist_sq = p.sub(p_closest).dotProduct(p.sub(p_closest));

        // Result: {u (weight for V1), v (weight for V2), squared_distance to closest point}
        return new double[]{u, v, dist_sq};
    }
}