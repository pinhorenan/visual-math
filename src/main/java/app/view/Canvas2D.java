package app.view;

import app.model.ObservableVector;
import app.model.VectorWorld;
import app.util.VectorMath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Renderizador 2D para N vetores.
 * <p>Escala dinâmica, grade/ticks opcionais, suporte a rotulagem e exibição
 * de ângulo + ortogonalidade entre v₁ e v₂ (quando existirem).</p>
 */
public class Canvas2D extends Canvas implements VectorCanvas {
    private VectorWorld world;

    /* flags de exibição */
    private final BooleanProperty showResult = new SimpleBooleanProperty(true);
    private final BooleanProperty showCoord  = new SimpleBooleanProperty(true);
    private final BooleanProperty showOrtho  = new SimpleBooleanProperty(true);
    private final BooleanProperty showAngle  = new SimpleBooleanProperty(true);
    private final BooleanProperty showTicks  = new SimpleBooleanProperty(true);
    private final BooleanProperty showGrid   = new SimpleBooleanProperty(true);

    private double currentScale = 30;

    public Canvas2D() {
        super(600, 600);
        setStyle("-fx-border-color:#bbb; -fx-border-width:1;");
        widthProperty().addListener(_ -> draw());
        heightProperty().addListener(_ -> draw());
    }

    /* =========== Propriedades de exibição =========== */

    public BooleanProperty showResultProperty(){ return showResult; }
    public BooleanProperty showCoordProperty (){ return showCoord;  }
    public BooleanProperty showOrthoProperty (){ return showOrtho;  }
    public BooleanProperty showAngleProperty (){ return showAngle;  }
    public BooleanProperty showTicksProperty (){ return showTicks;  }
    public BooleanProperty showGridProperty  (){ return showGrid;   }


    /* ============== VectorCanvas ============== */

    @Override
    public void bind(VectorWorld world) {
        this.world = world;

        /* 1) Ouvinte para adição/remoção de vetores na lista */
        world.getVectors().addListener((ListChangeListener<ObservableVector>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (ObservableVector v : c.getAddedSubList()) {
                        v.xProperty().addListener(_ -> draw());
                        v.yProperty().addListener(_ -> draw());
                    }
                }
            }
            draw();
        });
        world.getVectors().forEach(v -> {
            v.xProperty().addListener(_ -> draw());
            v.yProperty().addListener(_ -> draw());
        });

        showResult.addListener(_ -> draw());
        showCoord .addListener(_ -> draw());
        showOrtho .addListener(_ -> draw());
        showAngle .addListener(_ -> draw());
        showTicks .addListener(_ -> draw());
        showGrid  .addListener(_ -> draw());

        draw();
    }

    @Override
    public Node getView() {
        return this;
    }

    /* ================= Desenho principal ================= */

    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        double W = getWidth(), H = getHeight();
        double cx = W / 2, cy = H / 2;

        g.setFill(Color.WHITE); g.fillRect(0, 0, W, H);
        if (world == null) return;

        /* === escala dinâmica === */
        double max = 1;
        for (ObservableVector v : world.getVectors()) {
            max = Math.max(max, Math.max(Math.abs(v.getX()), Math.abs(v.getY())));
        }
        double margin = 80;
        currentScale = max == 0 ? 40 : Math.min((W/2 - margin) / max, (H/2 - margin) / max);

        /* grade */
        if (showGrid.get()) drawGrid(g, cx, cy);
        g.setStroke(Color.LIGHTGRAY); g.setLineWidth(1);
        g.strokeLine(cx, 0, cx, H); g.strokeLine(0, cy, W, cy);
        if (showTicks.get()) drawTicks(g, cx, cy);

        /* desenha vetores */
        int i = 0;
        for (ObservableVector v : world.getVectors()) {
            drawVector(g, cx, cy, v.getX(), v.getY(), pickColor(i), "v" + (i + 1));
            i++;
        }

        /* operações envolvendo dois vetores */
        if (world.getVectors().size() >= 2) {
            var a = world.getVectors().get(0);
            var b = world.getVectors().get(1);

            if (showResult.get()) {
                drawVector(g, cx, cy, a.getX()+b.getX(), a.getY()+b.getY(), Color.GREEN, "R");
            }

            double ang = VectorMath.angleDeg(a, b);
            boolean ortho = Math.abs(VectorMath.dot(a, b)) < 1e-6;

            if (showAngle.get()) drawAngleArc(g, cx, cy, a.getX(),a.getY(), b.getX(),b.getY(), ang);
            if (showOrtho.get() && ortho) drawOrthoMarker(g, cx, cy);
        }
    }

    /* ================== Métodos auxiliares de desenho ================== */

    private Color pickColor(int i) {
        return switch (i) {
            case 0 -> Color.RED;
            case 1 -> Color.BLUE;
            case 2 -> Color.ORANGE;
            default -> Color.hsb((i * 45) % 360, 0.8, 0.8);
        };
    }

    private void drawVector(GraphicsContext g, double ox, double oy,
                            double x, double y, Color color,String label) {
        double ex = ox + x * currentScale, ey = oy - y * currentScale;
        g.setStroke(color);
        g.setLineWidth(2);
        g.strokeLine(ox, oy, ex, ey);

        /* ponta da seta */
        double ang = Math.atan2(oy - ey, ex - ox), len = 10, off = Math.toRadians(20);
        g.strokeLine(ex, ey, ex - len * Math.cos(ang - off), ey + len * Math.sin(ang - off));
        g.strokeLine(ex, ey, ex - len * Math.cos(ang + off), ey + len * Math.sin(ang + off));

        g.setFill(color.darker());
        if (showCoord.get()) {
            g.fillText(label + String.format(" (%.1f, %.1f)", x, y), ex + 4, ey - 4);
        } else {
            g.fillText(label, ex + 4, ey - 4);
        }
    }

    private void drawGrid(GraphicsContext g, double cx, double cy) {
        g.setStroke(Color.web("#f0f0f0")); g.setLineWidth(1);
        int maxY = (int) Math.ceil(getWidth() / (2 * currentScale));
        int maxX = (int) Math.ceil(getHeight() / (2 * currentScale));
        for (int i = -maxX; i <= maxX; i++) {
            g.strokeLine(cx + i * currentScale, 0, cx + i * currentScale, getHeight());
        }
        for (int j = -maxY; j <= maxY; j++) {
            g.strokeLine(0, cy - j * currentScale, getWidth(), cy - j * currentScale);
        }
    }

    private void drawTicks(GraphicsContext g, double cx, double cy) {
        g.setStroke(Color.GRAY); g.setLineWidth(1);
        int maxX = (int) (getWidth() / (2 * currentScale));
        int maxY = (int) (getHeight() / (2 * currentScale));
        for (int i = -maxX; i <= maxX; i++) {
            double x = cx + i * currentScale;
            g.strokeLine(x, cy -4, x, cy + 4);
            if (i != 0) g.fillText(Integer.toString(i), x - 4, cy + 16);
        }
        for (int j = -maxY; j <= maxY; j++) {
            double y = cy - j * currentScale;
            g.strokeLine(cx - 4, y, cx + 4, y);
            if (j != 0) g.fillText(Integer.toString(j), cx + 6, y + 4);
        }
    }

    private void drawAngleArc(GraphicsContext g, double cx, double cy,
                              double x1, double y1, double x2, double y2, double angle) {
        double r = 40;
        double a1 = Math.atan2(y1, x1), a2 = Math.atan2(y2, x2);
        double sweep = a2 - a1;
        if (sweep <= -Math.PI) sweep += 2 * Math.PI;
        if (sweep > Math.PI) sweep -= 2 * Math.PI;

        g.setStroke(Color.DARKORANGE);
        g.setLineWidth(1.5);
        g.beginPath();
        for (double t = 0; t <= 1; t += 0.02) {
            double a = a1 + t * sweep;
            double px = cx + r * Math.cos(a);
            double py = cy - r * Math.sin(a);
            if (t == 0) g.moveTo(px, py);
            else g.lineTo(px, py);
        }
        g.stroke();
        g.setFill(Color.DARKORANGE.darker());
        double mid = a1 + sweep / 2;
        g.fillText(String.format("%.1fº", angle), cx + (4 + 10) * Math.cos(mid), cy - (r + 10) * Math.sin(mid));
    }

    private void drawOrthoMarker(GraphicsContext g, double cx, double cy) {
        g.setStroke(Color.MEDIUMPURPLE); g.setLineWidth(2);
        g.strokeOval(cx - 10, cy - 10, 20, 20);
        g.setFill(Color.MEDIUMPURPLE);
        g.fillText("⊥", cx + 12, cy - 12);
    }

}