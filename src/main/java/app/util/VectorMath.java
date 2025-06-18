package app.util;

/** Funções utilitárias de Álgebra Vetorial. */
public final class VectorMath {

    private VectorMath() {}

    // Módulo (magnitude) de um vetor 2D.
    public static double magnitude(double x, double y) {
        return Math.hypot(x, y);
    }

    // Produto escalar (dot product) de dois vetores 2D.
    public static double dot(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }

    // todo: decompor em funcao angleRad e dps uma angleDeg q so chama Math.toDegrees(angleRad).
    // todo: talvez também montar funcao sin cos
    public static double angleDeg(double x1, double y1, double x2, double y2) {
        double m1 = magnitude(x1, y1);
        double m2 = magnitude(x2, y2);
        if (m1 == 0 || m2 == 0) return 0.0;
        double cos = dot(x1, y1, x2, y2) / (m1 * m2);
        cos = Math.max(-1, Math.min(1, cos)); // Clamping to avoid NaN
        return Math.toDegrees(Math.acos(cos));
    }

}
