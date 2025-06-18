package app.view;

import app.model.ObservableVector2D;
import app.util.VectorMath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import static app.util.VectorMath.magnitude;

/**
 * Canvas que desenha v₁, v₂ e v₁+v₂ com:
 * • escala dinâmica (autofit)
 * • borda externa
 * • ticks e grade opcionais
 */
public class VectorCanvas extends Canvas {

    private final ObservableVector2D v1;
    private final ObservableVector2D v2;

    /* flags de exibição */
    private final BooleanProperty showCoords = new SimpleBooleanProperty(true);
    private final BooleanProperty showAngle  = new SimpleBooleanProperty(true);
    private final BooleanProperty showOrtho  = new SimpleBooleanProperty(true);
    private final BooleanProperty showResult = new SimpleBooleanProperty(true);
    private final BooleanProperty showTicks  = new SimpleBooleanProperty(true);
    private final BooleanProperty showGrid   = new SimpleBooleanProperty(false);

    /* escala usada no último draw (para conversão de mouse) */
    private double currentScale = 30;
    private String dragging = null;

    public VectorCanvas(ObservableVector2D v1, ObservableVector2D v2) {
        super(600, 600);                 // você pode redimensionar via layout
        this.v1 = v1;
        this.v2 = v2;

        setStyle("-fx-border-color:#ccc; -fx-border-width:1;");

        Runnable repaint = this::draw;
        v1.xProperty().addListener(_ -> repaint.run());
        v1.yProperty().addListener(_ -> repaint.run());
        v2.xProperty().addListener(_ -> repaint.run());
        v2.yProperty().addListener(_ -> repaint.run());

        showCoords.addListener(_ -> repaint.run());
        showAngle .addListener(_ -> repaint.run());
        showOrtho .addListener(_ -> repaint.run());
        showResult.addListener(_ -> repaint.run());
        showTicks .addListener(_ -> repaint.run());
        showGrid  .addListener(_ -> repaint.run());

        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPress);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDrag);
        addEventHandler(MouseEvent.MOUSE_RELEASED, _ -> dragging = null);

        draw();
    }

    /* ======= propriedades públicas para o painel ======= */
    public BooleanProperty showCoordsProperty() { return showCoords; }
    public BooleanProperty showAngleProperty()  { return showAngle;  }
    public BooleanProperty showOrthoProperty()  { return showOrtho;  }
    public BooleanProperty showResultProperty() { return showResult; }
    public BooleanProperty showTicksProperty()  { return showTicks;  }
    public BooleanProperty showGridProperty()   { return showGrid;   }

    /* ================= desenho principal ================= */
    private void draw() {
        double w = getWidth(), h = getHeight();
        double cx = w / 2, cy = h / 2;

        // --- coordenadas dos vetores ---
        double x1 = v1.getX(), y1 = v1.getY();
        double x2 = v2.getX(), y2 = v2.getY();
        double x3 = (x1 + x2), y3 = (y1 + y2);

        /* arco e ortogonalidade */
        double m1 = magnitude(x1, y1); // módulo de v₁
        double m2 = magnitude(x2, y2); // módulo de v₂
        double m3 = magnitude(x3, y3); // módulo de v₁+v₂
        double dot = VectorMath.dot(x1, y1, x2, y2); // produto escalar
        double ang = VectorMath.angleDeg(x1, y1, x2, y2); // ângulo entre v₁ e v₂
        boolean ortho = Math.abs(dot) < 1e-6; // v₁ e v₂ são ortogonais?

        /* === escala dinâmica === */
        double max = Math.max(
                Math.max(Math.abs(x1), Math.abs(y1)),
                Math.max(Math.abs(x2), Math.abs(y2)));
        max = Math.max(max, Math.max(Math.abs(x3), Math.abs(y3)));
        double margin = 100;
        double scale = (max == 0) ? 40
                : Math.min((w / 2 - margin) / max, (h / 2 - margin) / max);
        currentScale = scale;

        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        /* grid opcional */
        if (showGrid.get()) drawGrid(g, cx, cy, scale);

        /* eixos */
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(1);
        g.strokeLine(cx, 0, cx, h);
        g.strokeLine(0, cy, w, cy);

        /* ticks opcionais */
        if (showTicks.get()) drawTicks(g, cx, cy, scale);

        /* vetores */
        if (m1 > 0.1) drawVector(g, cx, cy, x1, y1, scale, Color.RED,   "v₁");
        if (m2 > 0.1) drawVector(g, cx, cy, x2, y2, scale, Color.BLUE,  "v₂");

        /* informativos */
        if (showResult.get() && m1 > 0.1 && m2 > 0.1) {
            drawVector(g, cx, cy, x3, y3, scale, Color.GREEN, "v₁+v₂");
        }
        if (showAngle.get() && m1 > 0.1 && m2 > 0.1) {
            drawAngleArc(g, cx, cy, x1, y1, x2, y2, ang);
        }
        if (showOrtho.get() && ortho && m1 > 0.1 && m2 > 0.1) {
            drawOrthoMarker(g, cx, cy);
        }
    }

    /* ---------------- grid e ticks ---------------- */
    private void drawGrid(GraphicsContext g, double cx, double cy, double scale) {
        g.setStroke(Color.web("#f0f0f0"));
        g.setLineWidth(1);
        int maxX = (int) Math.ceil(getWidth() / (2 * scale));
        int maxY = (int) Math.ceil(getHeight() / (2 * scale));
        for (int i = -maxX; i <= maxX; i++) {
            double x = cx + i * scale;
            g.strokeLine(x, 0, x, getHeight());
        }
        for (int j = -maxY; j <= maxY; j++) {
            double y = cy + j * scale * -1;
            g.strokeLine(0, y, getWidth(), y);
        }
    }

    private void drawTicks(GraphicsContext g, double cx, double cy, double scale) {
        g.setStroke(Color.GRAY);
        g.setLineWidth(1);
        int maxX = (int) Math.floor(getWidth() / (2 * scale));
        int maxY = (int) Math.floor(getHeight() / (2 * scale));

        for (int i = -maxX; i <= maxX; i++) {
            double x = cx + i * scale;
            g.strokeLine(x, cy - 4, x, cy + 4);
            if (i != 0) g.fillText(String.valueOf(i), x - 4, cy + 16);
        }
        for (int j = -maxY; j <= maxY; j++) {
            double y = cy - j * scale;
            g.strokeLine(cx - 4, y, cx + 4, y);
            if (j != 0) g.fillText(String.valueOf(j), cx + 6, y + 4);
        }
    }

    /* ---------------- vetores, arco, ortho ---------------- */
    private void drawVector(GraphicsContext g,
                            double ox, double oy,
                            double x, double y,
                            double scale,
                            Color color, String label) {
        double ex = ox + x * scale, ey = oy - y * scale;
        g.setStroke(color); g.setLineWidth(2);
        g.strokeLine(ox, oy, ex, ey);

        // flecha
        double ang = Math.atan2(oy - ey, ex - ox), len = 10, off = Math.toRadians(20);
        g.strokeLine(ex, ey, ex - len * Math.cos(ang - off), ey + len * Math.sin(ang - off));
        g.strokeLine(ex, ey, ex - len * Math.cos(ang + off), ey + len * Math.sin(ang + off));

        // rótulo
        g.setFill(color.darker());
        if (showCoords.get())
            g.fillText(label + String.format(" (%.1f, %.1f)", x, y), ex + 5, ey - 5);
        else
            g.fillText(label, ex + 5, ey - 5);
    }

    private void drawAngleArc(GraphicsContext g,
                              double cx, double cy,
                              double x1, double y1,
                              double x2, double y2,
                              double angle) {
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
            if (t == 0) g.moveTo(px, py); else g.lineTo(px, py);
        }
        g.stroke();

        double mid = a1 + sweep / 2;
        g.setFill(Color.DARKORANGE.darker());
        g.fillText(String.format("%.1f°", angle),
                cx + (r + 10) * Math.cos(mid),
                cy - (r + 10) * Math.sin(mid));
    }

    private void drawOrthoMarker(GraphicsContext g, double cx, double cy) {
        g.setStroke(Color.MEDIUMPURPLE); g.setLineWidth(2);
        g.strokeOval(cx - 10, cy - 10, 20, 20);
        g.setFill(Color.MEDIUMPURPLE);
        g.fillText("⊥", cx + 12, cy - 12);
    }

    /* ---------------- drag ---------------- */
    private double[] screenToCartesian(double sx, double sy) {
        double cx = getWidth() / 2, cy = getHeight() / 2;
        return new double[] { (sx - cx) / currentScale, (cy - sy) / currentScale };
    }
    private boolean near(double[] p, double vx, double vy) {
        return Math.hypot(p[0] - vx, p[1] - vy) < 0.5;
    }
    private void onPress(MouseEvent e) {
        double[] p = screenToCartesian(e.getX(), e.getY());
        dragging = null;
        if (near(p, v1.getX(), v1.getY())) dragging = "v1";
        else if (near(p, v2.getX(), v2.getY())) dragging = "v2";
    }
    private void onDrag(MouseEvent e) {
        if (dragging == null) return;
        double[] p = screenToCartesian(e.getX(), e.getY());
        if ("v1".equals(dragging)) { v1.setX(p[0]); v1.setY(p[1]); }
        else                       { v2.setX(p[0]); v2.setY(p[1]); }
    }
}
