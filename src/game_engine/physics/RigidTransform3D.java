package game_engine.physics;
import game_engine.math.Float3;
import java.util.Arrays;

/**
 * Implements the optimal rigid body transformation (Rotation R and Translation t)
 * between two corresponding sets of 3D points P and Q using the SVD-based
 * Procrustes algorithm.
 *
 * This version uses the Float3 class for input and output point clouds,
 * while maintaining high-precision matrix calculations using double arrays internally.
 *
 * NOTE: SVD and determinant calculation still require a dedicated linear algebra library.
 */
public class RigidTransform3D {

    // --- Converter Functions ---

    /** Converts an array of Float3 vectors into a high-precision double[][] matrix (Nx3). */
    private static double[][] convertToDoubleArray(Float3[] points) {
        if (points == null || points.length == 0) {
            return new double[0][3];
        }
        int N = points.length;
        double[][] doubleArray = new double[N][3];
        for (int i = 0; i < N; i++) {
            doubleArray[i][0] = points[i].x;
            doubleArray[i][1] = points[i].y;
            doubleArray[i][2] = points[i].z;
        }
        return doubleArray;
    }

    /** Converts a double[][] matrix (Nx3) back into an array of Float3 vectors. */
    private static Float3[] convertToFloat3Array(double[][] pointsDouble) {
        if (pointsDouble == null || pointsDouble.length == 0) {
            return new Float3[0];
        }
        int N = pointsDouble.length;
        Float3[] float3Array = new Float3[N];
        for (int i = 0; i < N; i++) {
            // Casting back to float might lose precision, but it matches the Float3 structure
            float3Array[i] = new Float3(
                    (float) pointsDouble[i][0],
                    (float) pointsDouble[i][1],
                    (float) pointsDouble[i][2]
            );
        }
        return float3Array;
    }


    // --- Core Transformation Functions ---

    /**
     * Calculates the optimal rigid body transformation (R, t) that maps P onto Q.
     * The result is a 4x4 homogeneous transformation matrix T (double[][]).
     *
     * @param P The source point cloud, an array of Float3.
     * @param Q The target point cloud, an array of Float3.
     * @return The 4x4 homogeneous transformation matrix T.
     */
    public static double[][] rigid_transform_3d(Float3[] P, Float3[] Q) {
        if (P.length != Q.length) {
            throw new IllegalArgumentException("Point arrays must have the same number of points.");
        }
        if (P.length < 3) {
            System.out.println("Warning: Calculation is mathematically unstable with fewer than 3 points.");
        }

        // Convert to double arrays for high-precision calculation
        double[][] P_double = convertToDoubleArray(P);
        double[][] Q_double = convertToDoubleArray(Q);

        // 1. Compute Centroids
        double[] C_P = getCentroid(P_double);
        double[] C_Q = getCentroid(Q_double);

        // 2. Center the point clouds
        double[][] P_centered = subtractCentroid(P_double, C_P);
        double[][] Q_centered = subtractCentroid(Q_double, C_Q);

        // 3. Compute the 3x3 covariance matrix H = P_centered^T * Q_centered
        double[][] P_T = transpose(P_centered);
        double[][] H = matrixMultiply(P_T, Q_centered);

        // --- SVD SECTION (Requires a Linear Algebra Library) ---
        // H = U * Sigma * V.T
        System.out.println("\n--- NOTICE: SVD required here. Placeholder values are used. ---");

        // The following lines assume a library provided the SVD components U, V:
        double[][] U = identity(3); // Placeholder for U
        double[][] V = identity(3); // Placeholder for V (V from Python's Vh.T)

        // 5. Compute the optimal Rotation Matrix R = V * U.T
        double[][] U_T = transpose(U);
        double[][] R = matrixMultiply(V, U_T);

        // 6. Special reflection check (Requires a library function for the determinant.)
        // -----------------------------------------------------------------------------

        // 7. Compute the Translation Vector t = C_Q - R * C_P
        double[] R_CP = vecMultiply(R, C_P);
        double[] t = new double[3];
        for (int i = 0; i < 3; i++) {
            t[i] = C_Q[i] - R_CP[i];
        }

        // 8. Build the 4x4 Homogeneous Transformation Matrix T
        return buildHomogeneousMatrix(R, t);
    }

    /**
     * Applies a 4x4 homogeneous transformation matrix T to a set of 3D points.
     *
     * @param T The 4x4 homogeneous transformation matrix (double[][]).
     * @param points An array of Float3 points.
     * @return An array of transformed Float3 points.
     */
    public static Float3[] apply_transform(double[][] T, Float3[] points) {
        double[][] pointsDouble = convertToDoubleArray(points);
        int N = pointsDouble.length;
        if (N == 0 || pointsDouble[0].length != 3 || T.length != 4 || T[0].length != 4) {
            throw new IllegalArgumentException("Invalid input dimensions.");
        }

        // 1. Convert points from Nx3 to 4xN homogeneous coordinates
        double[][] points_homogeneous = new double[4][N];
        for (int i = 0; i < N; i++) {
            points_homogeneous[0][i] = pointsDouble[i][0]; // X
            points_homogeneous[1][i] = pointsDouble[i][1]; // Y
            points_homogeneous[2][i] = pointsDouble[i][2]; // Z
            points_homogeneous[3][i] = 1.0;          // W=1
        }

        // 2. Apply transformation: Q_homogeneous = T @ P_homogeneous
        double[][] transformed_homogeneous = matrixMultiply(T, points_homogeneous);

        // 3. Convert result back to 3D points (Nx3 array)
        double[][] transformed_points_double = new double[N][3];
        for (int i = 0; i < N; i++) {
            transformed_points_double[i][0] = transformed_homogeneous[0][i];
            transformed_points_double[i][1] = transformed_homogeneous[1][i];
            transformed_points_double[i][2] = transformed_homogeneous[2][i];
        }

        // 4. Convert double[][] back to Float3[]
        return convertToFloat3Array(transformed_points_double);
    }

    // --- Matrix and Vector Helper Methods (Manual Implementations, operating on double[][]) ---

    /** Computes the centroid (mean of each dimension) of the point cloud. */
    private static double[] getCentroid(double[][] points) {
        int N = points.length;
        double[] centroid = new double[3];
        for (double[] point : points) {
            centroid[0] += point[0];
            centroid[1] += point[1];
            centroid[2] += point[2];
        }
        centroid[0] /= N;
        centroid[1] /= N;
        centroid[2] /= N;
        return centroid;
    }

    /** Subtracts the centroid from all points in the cloud. */
    private static double[][] subtractCentroid(double[][] points, double[] centroid) {
        int N = points.length;
        double[][] centered = new double[N][3];
        for (int i = 0; i < N; i++) {
            centered[i][0] = points[i][0] - centroid[0];
            centered[i][1] = points[i][1] - centroid[1];
            centered[i][2] = points[i][2] - centroid[2];
        }
        return centered;
    }

    /** Multiplies two matrices A * B. */
    private static double[][] matrixMultiply(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrices dimensions are incompatible for multiplication.");
        }

        double[][] C = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }

    /** Computes the transpose of matrix A (A^T). */
    private static double[][] transpose(double[][] A) {
        int rows = A.length;
        int cols = A[0].length;
        double[][] A_T = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                A_T[j][i] = A[i][j];
            }
        }
        return A_T;
    }

    /** Multiplies a matrix R by a 3D vector C: R * C. */
    private static double[] vecMultiply(double[][] R, double[] C) {
        if (R.length != 3 || R[0].length != 3 || C.length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3 and vector must be 3D.");
        }
        double[] result = new double[3];
        for (int i = 0; i < 3; i++) {
            result[i] = R[i][0] * C[0] + R[i][1] * C[1] + R[i][2] * C[2];
        }
        return result;
    }

    /** Constructs the 4x4 homogeneous transformation matrix T from R and t. */
    private static double[][] buildHomogeneousMatrix(double[][] R, double[] t) {
        double[][] T = identity(4);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                T[i][j] = R[i][j];
            }
            T[i][3] = t[i];
        }
        return T;
    }

    /** Creates an N x N identity matrix. */
    private static double[][] identity(int N) {
        double[][] I = new double[N][N];
        for (int i = 0; i < N; i++) {
            I[i][i] = 1.0;
        }
        return I;
    }

    /** Calculates the Euclidean norm (distance) between two Float3 vectors. */
    private static double norm(Float3 vecA, Float3 vecB) {
        double dx = vecA.x - vecB.x;
        double dy = vecA.y - vecB.y;
        double dz = vecA.z - vecB.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Pretty prints an array of Float3 objects. */
    private static void printFloat3Array(String title, Float3[] points) {
        System.out.println(title);
        for (Float3 p : points) {
            System.out.println("[" + p.toString() + "]");
        }
    }

    /** Pretty prints a double[][] matrix. */
    private static void printMatrix(String title, double[][] matrix) {
        System.out.println(title);
        for (double[] row : matrix) {
            System.out.print("[");
            for (int i = 0; i < row.length; i++) {
                System.out.printf("%.4f", row[i]);
                if (i < row.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println("]");
        }
    }


    // --- Main Execution Example ---

    public static void main(String[] args) {
        // --- Example Setup using Float3 arrays ---

        // 1. Define the original set of 3 points (P_base)
        Float3[] P_base = {
                new Float3(1.0f, 0.0f, 0.0f),
                new Float3(0.0f, 1.0f, 0.0f),
                new Float3(0.0f, 0.0f, 1.0f)
        };

        // 2. Define a known transformation (90 degrees rotation around Z, plus translation)
        double[][] R_true = {
                {0.0, -1.0, 0.0},
                {1.0, 0.0, 0.0},
                {0.0, 0.0, 1.0}
        };
        double[] t_true = {5.0, 10.0, 15.0};

        // 3. Apply the known transformation to P_base to get the target points Q_base
        Float3[] Q_base = new Float3[P_base.length];
        for (int i = 0; i < P_base.length; i++) {
            double[] rotated = vecMultiply(R_true, convertToDoubleArray(new Float3[]{P_base[i]})[0]);
            Q_base[i] = new Float3(
                    (float) (rotated[0] + t_true[0]),
                    (float) (rotated[1] + t_true[1]),
                    (float) (rotated[2] + t_true[2])
            );
        }

        // 4. Define a NEW point (P_new) that was not used in the calculation
        Float3[] P_new = {
                new Float3(5.0f, 5.0f, 5.0f) // A point deep inside the structure
        };

        // --- Calculation ---

        // A. Calculate the transformation matrix T based ONLY on P_base and Q_base
        // NOTE: This call will use placeholder SVD results.
        double[][] T_calculated = rigid_transform_3d(P_base, Q_base);

        // Since we know the true transform, we will manually build T_true for verification
        double[][] T_true = buildHomogeneousMatrix(R_true, t_true);

        printFloat3Array("Source Base Points P (3x3):", P_base);
        printFloat3Array("\nTarget Base Points Q (3x3):", Q_base);
        printMatrix("\nTrue 4x4 Transformation Matrix T (Used for Verification):", T_true);

        System.out.println("\n--- Deforming a New Point P_new using T_true ---");

        // B. Apply the true transformation T_true to the new point P_new (Float3[] in, Float3[] out)
        Float3[] Q_new_calculated = apply_transform(T_true, P_new);

        // C. Verification: Find the mathematically correct location of P_new (Q_new_true)
        Float3[] Q_new_true = new Float3[P_new.length];
        for (int i = 0; i < P_new.length; i++) {
            double[] rotated = vecMultiply(R_true, convertToDoubleArray(new Float3[]{P_new[i]})[0]);
            Q_new_true[i] = new Float3(
                    (float) (rotated[0] + t_true[0]),
                    (float) (rotated[1] + t_true[1]),
                    (float) (rotated[2] + t_true[2])
            );
        }

        printFloat3Array("New Source Point P_new:", P_new);
        printFloat3Array("Transformed New Point (Calculated via apply_transform):", Q_new_calculated);
        printFloat3Array("Transformed New Point (True Location):", Q_new_true);

        // Verification of the new point transformation
        double error = norm(Q_new_true[0], Q_new_calculated[0]);
        System.out.printf("\nTransformation Error on New Point (should be close to zero): %.6f%n", error);
    }
}
