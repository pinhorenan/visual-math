package app.util;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Color;

/** Cone sólido (fuste = 0 -> ponta) com base centrada em y = +h/2 e ápice em y = -h/2. */
public class Cone extends MeshView {
    public Cone(float radius, float height, int divisions, Color color) {
        super(buildMesh(radius, height, divisions));
        setMaterial(new PhongMaterial(color));
    }

    private static TriangleMesh buildMesh(float r, float h, int n) {
        TriangleMesh mesh = new TriangleMesh();

        // 1. Ápice do cone (topo, Y negativo)
        mesh.getPoints().addAll(0, -h / 2, 0); // idx 0

        // 2. Borda da base (pontos do círculo no plano Y positivo)
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            float x = (float) (r * Math.cos(a));
            float z = (float) (r * Math.sin(a));
            mesh.getPoints().addAll(x, h / 2, z); // idx 1..n
        }

        // 3. Centro da base (plano Y positivo)
        mesh.getPoints().addAll(0, h /2, 0); // idx n+1
        mesh.getTexCoords().addAll(0, 0);  // necessário pelo JavaFX

        // 4. Faces laterais (liga ápice a borda)
        for (int i = 1; i <= n; i++) {
            int next = i % n + 1;                // 1,2,...,n  →  next == 1 quando i == n
            mesh.getFaces().addAll(0, 0, next, 0, i, 0);
        }

        // 5. Faces da base (liga centro aos pontos da borda)
        int centerIndex = n + 1;
        for (int i = 1; i <= n; i++) {
            int next = i % n + 1;
            // Ordem dos vértices invertida para ficar voltado para baixo
            mesh.getFaces().addAll(centerIndex, 0, i, 0, next, 0);
        }

        return mesh;
    }
}
