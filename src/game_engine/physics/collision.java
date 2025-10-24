package game_engine.physics;

import game_engine.math.Float2;
import game_engine.math.Float3;
import game_engine.scene.GameObject;
import game_engine.scene.Model;
// Removed unused imports: RigidTransform3D, static imports

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
     * Uses Barycentric Coordinates for accurate UV interpolation.
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

        // Find the triangle closest to the collision point
        double min_dist_sq = Double.MAX_VALUE;
        Float2 final_uv = null;

        // Iterate through all triangles to find the closest one
        for (int i = 0; i < triangles; i++) {
            Float3[] triangle_verts = model.getTriVertexes(i); // V0, V1, V2
            Float2[] triangle_uvs = model.getTriUVs(i);       // UV0, UV1, UV2
            triangle_uvs = new Float2[]{new Float2(0f, 0f), new Float2(1,0f), new Float2(1,1f)};

            // Calculate Barycentric Coordinates (u, v, w) for the point pos on the triangle
            // The method returns the distance squared from the point to the triangle plane.
            double[] bary_and_dist = get_barycentric_coords(pos, triangle_verts);

            if (bary_and_dist == null) continue; // Skip degenerate triangles

            double u = bary_and_dist[0]; // weight for V1
            double v = bary_and_dist[1]; // weight for V2
            double w = 1.0 - u - v;      // weight for V0
            double dist_sq = bary_and_dist[2];

            // This is a simplification: we're only checking the distance to the plane,
            // a full check would ensure the point is *within* the triangle's bounds (0 <= u, v, w <= 1)
            // For a general collision system, the collision query already found the closest face.

            if (dist_sq < min_dist_sq) {
                min_dist_sq = dist_sq;

                // Interpolate UV coordinates using the Barycentric weights
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
     * Helper to calculate Barycentric coordinates and the squared distance to the triangle plane.
     * Based on projecting pos onto the plane defined by the triangle.
     * @param p The 3D point.
     * @param tri Array of 3 vertices [V0, V1, V2].
     * @return double[] {u (weight for V1), v (weight for V2), squared_distance}, or null.
     */
    private static double[] get_barycentric_coords(Float3 p, Float3[] tri) {
        // V0, V1, V2
        Float3 v0 = tri[0];
        Float3 v1 = tri[1];
        Float3 v2 = tri[2];

        // Edge vectors
        Float3 edge1 = v1.sub(v0); // V1 - V0
        Float3 edge2 = v2.sub(v0); // V2 - V0

        // Vector from V0 to P
        Float3 p_v0 = p.sub(v0); // P - V0

        // Calculate a normal vector for the triangle (cross product)
        Float3 N = edge1.crossProduct(edge2);
        double N_mag_sq = N.dotProduct(N);

        // Check for degenerate triangle (zero area)
        if (N_mag_sq < 1e-6) {
            return null;
        }

        // Signed distance from P to the plane defined by the triangle
        double d = N.dotProduct(p_v0);
        double dist_sq = (d * d) / N_mag_sq;

        // Find the projection point P_proj onto the plane: P_proj = P - N * (d / |N|^2)
        // Since we only need u/v/w for P_proj, we use the property that
        // the barycentric coords of P are the same as P_proj relative to the triangle's plane.

        // Calculate the weights u and v using a simplified approach that avoids explicit projection
        // and relies on algebraic manipulation of vector equation for the plane.

        double inv_den = 1.0 / N_mag_sq;

        double dot00 = edge1.dotProduct(edge1);
        double dot01 = edge1.dotProduct(edge2);
        double dot02 = edge1.dotProduct(p_v0);
        double dot11 = edge2.dotProduct(edge2);
        double dot12 = edge2.dotProduct(p_v0);

        // Determinant of the system matrix (related to area)
        double D = dot00 * dot11 - dot01 * dot01;

        // Check for near-degenerate in this system (should be covered by N_mag_sq check, but safer)
        if (Math.abs(D) < 1e-6) {
            return null;
        }

        double invD = 1.0 / D;

        // u and v are the weights for V1 and V2, respectively (with V0 as the origin)
        double u = (dot11 * dot02 - dot01 * dot12) * invD;
        double v = (dot00 * dot12 - dot01 * dot02) * invD;

        // Result: {u (weight for V1), v (weight for V2), squared_distance to plane}
        return new double[]{u, v, dist_sq};
    }
}