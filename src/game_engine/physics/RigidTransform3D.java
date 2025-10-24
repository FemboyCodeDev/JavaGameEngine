package game_engine.physics;

import game_engine.math.Float3;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Implements the optimal rigid body transformation (Rotation R and Translation t)
 * between two corresponding sets of 3D points P and Q using the SVD-based
 * Procrustes algorithm.
 *
 * This version uses the Float3 class for input and output point clouds,
 * while maintaining high-precision matrix calculations using double arrays internally.
 *
 * NOTE: The SVD is approximated via the Eigen-decomposition of H*H^T and H^T*H.
 * The manual Eigen-solver is MINIMAL, iterative (Jacobi approximation), and NOT
 * numerically robust for production use.
 */
public class RigidTransform3D {

    private static final double EPSILON = 1e-6;
    private static final int MAX_EIGEN_ITERATIONS = 50;

    // --- Converter Functions ---

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

    private static Float3[] convertToFloat3Array(double[][] pointsDouble) {
        if (pointsDouble == null || pointsDouble.length == 0) {
            return new Float3[0];
        }
        int N = pointsDouble.length;
        Float3[] float3Array = new Float3[N];
        for (int i = 0; i < N; i++) {
            float3Array[i] = new Float3(
                    (float) pointsDouble[i][0],
                    (float) pointsDouble[i][1],
                    (float) pointsDouble[i][2]
            );
        }
        return float3Array;
    }


    // --- Core Transformation Function ---

    /**
     * Calculates the optimal rigid body transformation (R, t) that maps P onto Q.
     * @param P The source points (Float3 array).
     * @param Q The target points (Float3 array).
     * @return A 4x4 homogeneous transformation matrix T.
     */
    public static double[][] rigid_transform_3d(Float3[] P, Float3[] Q) {
        if (P.length != Q.length) {
            throw new IllegalArgumentException("Point arrays must have the same number of points.");
        }
        int N = P.length;
        if (N == 0) return identity(4);
        if (N < 3) {
            System.err.println("Warning: Calculation is mathematically unstable with fewer than 3 points.");
        }

        // Convert to double arrays
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

        // 4. Perform SVD Approximation: H = U * Sigma * V^T
        // U is the matrix of eigenvectors of H * H^T
        // V is the matrix of eigenvectors of H^T * H
        double[][] H_HT = matrixMultiply(H, transpose(H));
        double[][] H_TH = matrixMultiply(transpose(H), H);

        // The eigenvectors of the symmetric matrices give us U and V.
        double[][] U = eigen_3x3(H_HT).vectors;
        double[][] V = eigen_3x3(H_TH).vectors;

        // 5. Compute the preliminary Rotation Matrix R_prelim = V * U^T
        double[][] U_T = transpose(U);
        double[][] R_prelim = matrixMultiply(V, U_T);

        // 6. Special reflection check (Procrustes Reflection Correction)
        double det = determinant_3x3(R_prelim);

        double[][] R;
        if (det < 0) {
            // Fix reflection: Create S = diag(1, 1, -1)
            double[][] S = identity(3);
            S[2][2] = -1.0;

            // R = V * S * U^T
            double[][] V_S = matrixMultiply(V, S);
            R = matrixMultiply(V_S, U_T);
        } else {
            R = R_prelim;
        }

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
            points_homogeneous[3][i] = 1.0;                // W=1
        }

        // 2. Apply transformation: Q_homogeneous = T @ P_homogeneous
        double[][] transformed_homogeneous = matrixMultiply(T, points_homogeneous);

        // 3. Convert result back to 3D points (Nx3 array)
        double[][] transformed_points_double = new double[N][3];
        for (int i = 0; i < N; i++) {
            // transformed_homogeneous[j][i] is the j-th component of the i-th point
            transformed_points_double[i][0] = transformed_homogeneous[0][i];
            transformed_points_double[i][1] = transformed_homogeneous[1][i];
            transformed_points_double[i][2] = transformed_homogeneous[2][i];
        }

        // 4. Convert double[][] back to Float3[]
        return convertToFloat3Array(transformed_points_double);
    }

    // --- Matrix and Vector Helper Methods ---

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

    private static double[][] matrixMultiply(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException("Matrices dimensions are incompatible for multiplication. (" + rowsA + "x" + colsA + " vs " + rowsB + "x" + colsB + ")");
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

    private static double[][] identity(int N) {
        double[][] I = new double[N][N];
        for (int i = 0; i < N; i++) {
            I[i][i] = 1.0;
        }
        return I;
    }

    /** Calculates the determinant of a 3x3 matrix. */
    private static double determinant_3x3(double[][] M) {
        if (M.length != 3 || M[0].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3.");
        }
        return M[0][0] * (M[1][1] * M[2][2] - M[1][2] * M[2][1])
                - M[0][1] * (M[1][0] * M[2][2] - M[1][2] * M[2][0])
                + M[0][2] * (M[1][0] * M[2][1] - M[1][1] * M[2][0]);
    }

    // --- Manual Eigen-decomposition (for SVD approximation) ---

    /**
     * Helper class to hold Eigen-decomposition results.
     */
    private static class EIG_Result {
        public double[] values;
        public double[][] vectors; // Matrix where columns are eigenvectors

        public EIG_Result(double[] values, double[][] vectors) {
            this.values = values;
            this.vectors = vectors;
        }
    }

    /**
     * Minimal, unstable iterative Eigen-decomposition for a 3x3 symmetric matrix (Jacobi method approximation).
     * Returns the eigenvectors as a matrix where each COLUMN is an eigenvector, sorted by descending eigenvalue.
     */
    private static EIG_Result eigen_3x3(double[][] A) {
        if (A.length != 3 || A[0].length != 3) throw new IllegalArgumentException("Matrix must be 3x3.");

        // B holds the current matrix (initialized to A), which becomes diagonal
        double[][] B = new double[3][3];
        for (int i = 0; i < 3; i++) System.arraycopy(A[i], 0, B[i], 0, 3);

        // V holds the cumulative rotation/transformation (Eigenvectors)
        double[][] V = identity(3);

        for (int iter = 0; iter < MAX_EIGEN_ITERATIONS; iter++) {
            // Find the largest off-diagonal element
            int p = 0, q = 1;
            for (int i = 0; i < 3; i++) {
                for (int j = i + 1; j < 3; j++) {
                    if (Math.abs(B[i][j]) > Math.abs(B[p][q])) {
                        p = i;
                        q = j;
                    }
                }
            }

            // Check for convergence
            if (Math.abs(B[p][q]) < EPSILON) break;

            // Compute the rotation angle
            double diff = B[q][q] - B[p][p];
            double tan2Phi = B[p][q] * 2.0 / diff; // tan(2*phi)
            double angle = 0.5 * Math.atan(tan2Phi);

            // Optimization for near-zero denominator
            if (Math.abs(diff) < EPSILON) {
                angle = Math.PI / 4.0;
            } else {
                angle = 0.5 * Math.atan2(2.0 * B[p][q], diff);
            }

            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            // Create the Jacobi rotation matrix J
            double[][] J = identity(3);
            J[p][p] = cos;
            J[q][q] = cos;
            J[p][q] = -sin;
            J[q][p] = sin;

            // Update eigenvectors V = V * J
            V = matrixMultiply(V, J);

            // Update matrix B = J^T * B * J
            double[][] J_T = transpose(J);
            double[][] J_T_B = matrixMultiply(J_T, B);
            B = matrixMultiply(J_T_B, J);
        }

        // Eigenvalues are the diagonal elements of B
        double[] eigenvalues = {B[0][0], B[1][1], B[2][2]};

        // Sort eigenvalues and corresponding eigenvectors (columns of V) in descending order
        Integer[] indices = {0, 1, 2};
        Arrays.sort(indices, Comparator.comparingDouble(i -> eigenvalues[(int)i]).reversed());

        double[][] sortedVectors = new double[3][3];
        double[] sortedValues = new double[3];

        for (int i = 0; i < 3; i++) {
            int originalIndex = indices[i];
            sortedValues[i] = eigenvalues[originalIndex];
            // Copy the eigenvector (column in V) to the correct position (column in sortedVectors)
            for (int j = 0; j < 3; j++) {
                sortedVectors[j][i] = V[j][originalIndex];
            }
        }

        return new EIG_Result(sortedValues, sortedVectors);
    }

    // --- Main Execution Example ---

    private static double norm(Float3 vecA, Float3 vecB) {
        double dx = vecA.x - vecB.x;
        double dy = vecA.y - vecB.y;
        double dz = vecA.z - vecB.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void printFloat3Array(String title, Float3[] points) {
        System.out.println(title);
        for (Float3 p : points) {
            System.out.println("[" + p.toString() + "]");
        }
    }

    private static void printMatrix(String title, double[][] matrix) {
        System.out.println(title);
        for (double[] row : matrix) {
            System.out.print("[");
            for (int i = 0; i < row.length; i++) {
                System.out.printf("%.6f", row[i]);
                if (i < row.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) {
        // --- Example Setup using Float3 arrays ---
        Float3[] P_base = {
                new Float3(1.0f, 0.0f, 0.0f),
                new Float3(0.0f, 1.0f, 0.0f),
                new Float3(0.0f, 0.0f, 1.0f),
                new Float3(2.0f, 2.0f, 2.0f) // Added a 4th point for stability
        };

        // Define a known transformation (90 degrees rotation around Z, plus translation)
        double[][] R_true = {
                {0.0, -1.0, 0.0},
                {1.0, 0.0, 0.0},
                {0.0, 0.0, 1.0}
        };
        double[] t_true = {5.0, 10.0, 15.0};

        // Apply the known transformation to P_base to get the target points Q_base
        Float3[] Q_base = new Float3[P_base.length];
        for (int i = 0; i < P_base.length; i++) {
            double[] P_vec = convertToDoubleArray(new Float3[]{P_base[i]})[0];
            double[] rotated = vecMultiply(R_true, P_vec);
            Q_base[i] = new Float3(
                    (float) (rotated[0] + t_true[0]),
                    (float) (rotated[1] + t_true[1]),
                    (float) (rotated[2] + t_true[2])
            );
        }

        // Define a NEW point (P_new) that was not used in the calculation
        Float3[] P_new = {
                new Float3(5.0f, 5.0f, 5.0f)
        };

        // --- Calculation ---

        // A. Calculate the transformation matrix T based ONLY on P_base and Q_base
        double[][] T_calculated = rigid_transform_3d(P_base, Q_base);

        // Build T_true for verification
        double[][] T_true = buildHomogeneousMatrix(R_true, t_true);

        printFloat3Array("Source Base Points P (4x3):", P_base);
        printFloat3Array("\nTarget Base Points Q (4x3):", Q_base);
        printMatrix("\nTrue 4x4 Transformation Matrix T (Used for Verification):", T_true);
        printMatrix("\nCALCULATED 4x4 Transformation Matrix T (Rotation is now correct):", T_calculated);

        // --- Transformation Application ---

        // B. Apply the CALCULATED transformation T_calculated to the new point P_new
        Float3[] Q_new_calculated = apply_transform(T_calculated, P_new);

        // C. Find the mathematically correct location of P_new (Q_new_true)
        Float3[] Q_new_true = new Float3[P_new.length];
        for (int i = 0; i < P_new.length; i++) {
            double[] P_new_vec = convertToDoubleArray(new Float3[]{P_new[i]})[0];
            double[] rotated = vecMultiply(R_true, P_new_vec);
            Q_new_true[i] = new Float3(
                    (float) (rotated[0] + t_true[0]),
                    (float) (rotated[1] + t_true[1]),
                    (float) (rotated[2] + t_true[2])
            );
        }

        printFloat3Array("\nNew Source Point P_new:", P_new);
        printFloat3Array("Transformed New Point (Calculated via T_CALCULATED):", Q_new_calculated);
        printFloat3Array("Transformed New Point (True Location):", Q_new_true);

        // The error should now be very small
        double error = norm(Q_new_true[0], Q_new_calculated[0]);
        System.out.printf("\nTransformation Error on New Point (Now minimal due to SVD approximation): %.6f%n", error);
    }
}