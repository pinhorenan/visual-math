package app.util;

import app.model.ObservableVector;
import java.util.Arrays;

/**
 * Utilidades matemáticas para vetores 2D e 3D (ou superior).
 * Aceita tanto arrays de {@code double[]} quanto {@code ObservableVector}.
 */
public final class VectorMath {

    private VectorMath() {}

    /* ====================== */
    /* Operações com double[] */
    /* ====================== */

    public static double[] add(double[] v, double[] w) {
        checkSameDimension(v, w);
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) result[i] = v[i] + w[i];
        return result;
    }

    public static double[] subtract(double[] v, double[] w) {
        checkSameDimension(v, w);
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) result[i] = v[i] - w[i];
        return result;
    }

    public static double dot(double[] v, double[] w) {
        checkSameDimension(v, w);
        double sum = 0;
        for (int i = 0; i < v.length; i++) sum += v[i] * w[i];
        return sum;
    }

    public static double magnitude(double[] v) {
        return Math.sqrt(magnitudeSquared(v));
    }

    public static double magnitudeSquared(double[] v) {
        double sum = 0;
        for (double x : v) sum += x * x;
        return sum;
    }

    public static double[] normalize(double[] v) {
        double mag = magnitude(v);
        if (mag == 0) throw new ArithmeticException("Não é possível normalizar o vetor nulo.");
        return scalarMultiply(1.0 / mag, v);
    }

    public static double[] scalarMultiply(double alpha, double[] v) {
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) result[i] = alpha * v[i];
        return result;
    }

    public static double[] cross(double[] v, double[] w) {
        if (v.length != 3 || w.length != 3) {
            throw new IllegalArgumentException("Produto vetorial requer vetores 3D.");
        }
        return new double[] {
                v[1]*w[2] - v[2]*w[1], // Y1 * Z2 - Y2 * Z1
                v[2]*w[0] - v[0]*w[2], // Y2 * Z0 - Y0 * Z2
                v[0]*w[1] - v[1]*w[0]  // Y0 * Z1 - Y1 * Z0
        };
    }

    public static double angleRad(double[] v, double[] w) {
        double magV = magnitude(v);
        double magW = magnitude(w);
        if (magV == 0 || magW == 0) return 0;
        double cos = dot(v, w) / (magV * magW);
        cos = Math.max(-1, Math.min(1, cos)); // evitar erros de arredondamento
        return Math.acos(cos);
    }

    public static double angleDeg(double[] v, double[] w) {
        return Math.toDegrees(angleRad(v, w));
    }

    /* ============================== */
    /* Operações com ObservableVector */
    /* ============================== */

    public static double[] add(ObservableVector v, ObservableVector w) {
        return add(v.toArray(), w.toArray());
    }

    public static double[] subtract(ObservableVector v, ObservableVector w) {
        return subtract(v.toArray(), w.toArray());
    }

    public static double dot(ObservableVector v, ObservableVector w) {
        return dot(v.toArray(), w.toArray());
    }

    public static double magnitude(ObservableVector v) {
        return magnitude(v.toArray());
    }

    public static double magnitudeSquared(ObservableVector v) {
        return magnitudeSquared(v.toArray());
    }

    public static double[] normalize(ObservableVector v) {
        return normalize(v.toArray());
    }

    public static double[] scalarMultiply(double alpha, ObservableVector v) {
        return scalarMultiply(alpha, v.toArray());
    }

    public static double[] cross(ObservableVector v, ObservableVector w) {
        return cross(v.toArray(), w.toArray());
    }

    public static double angleRad(ObservableVector v, ObservableVector w) {
        return angleRad(v.toArray(), w.toArray());
    }

    public static double angleDeg(ObservableVector v, ObservableVector w) {
        return angleDeg(v.toArray(), w.toArray());
    }

    /* ========================================================== */
    private static void checkSameDimension(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vetores devem ter a mesma dimensão.");
    }

    public static String toString(double[] v) {
        return Arrays.toString(v);
    }
}
