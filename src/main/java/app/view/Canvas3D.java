package app.view;

import app.util.Cone;
import app.model.ObservableVector;
import app.model.VectorWorld;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import static app.util.VectorMath.magnitude;

/**
 * Canvas 3D – eixos centrados, grids nos 3 planos, ticks e câmera orbit em qualquer área.
 */
public class Canvas3D extends StackPane implements VectorCanvas {

    private VectorWorld world;

    /* grupos desenháveis */
    private final Group axesGroup    = new Group();
    private final Group gridGroup    = new Group();
    private final Group ticksGroup   = new Group();
    private final Group vectorsGroup = new Group();

    /* flags compartilháveis com Canvas2D */
    private final BooleanProperty showGrid  = new SimpleBooleanProperty(true);
    private final BooleanProperty showTicks = new SimpleBooleanProperty(true);
    public BooleanProperty showGridProperty()  { return showGrid;  }
    public BooleanProperty showTicksProperty() { return showTicks; }

    /* câmera orbit */
    private final Rotate rotateX = new Rotate(-30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
    private final SubScene subScene;
    private double anchorX, anchorY, anchorAngleX, anchorAngleY;

    /* escala atual em px por unidade */
    private double currentScale = 40;

    public Canvas3D() {
        /* câmera */
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setNearClip(0.1);
        cam.setFarClip(10_000);
        cam.setTranslateZ(-600);

        Group root3D = new Group(gridGroup, axesGroup, ticksGroup, vectorsGroup);
        root3D.getTransforms().addAll(rotateX, rotateY);

        subScene = new SubScene(root3D, 600, 600, true, null);
        subScene.setCamera(cam);
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());
        getChildren().add(subScene);

        /* redimensiona */
        widthProperty().addListener((_,_,w) -> subScene.setWidth(w.doubleValue()));
        heightProperty().addListener((_,_,h)-> subScene.setHeight(h.doubleValue()));

        initMouseOrbit();
    }

    /* ------------ interface VectorCanvas ------------ */
    @Override public void bind(VectorWorld world) {
        this.world = world;

        /* ouvintes para rebuild */
        world.getVectors().addListener((ListChangeListener<ObservableVector>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(v -> {
                        v.xProperty().addListener(_ -> rebuild());
                        v.yProperty().addListener(_ -> rebuild());
                        v.zProperty().addListener(_ -> rebuild());
                    });
                }
            }
            rebuild();
        });
        world.getVectors().forEach(v -> {
            v.xProperty().addListener(_ -> rebuild());
            v.yProperty().addListener(_ -> rebuild());
            v.zProperty().addListener(_ -> rebuild());
        });

        showGrid .addListener(_ -> rebuild());
        showTicks.addListener(_ -> rebuild());

        rebuild();
    }
    @Override public Node getView() { return this; }

    /* ----------------- desenho principal ----------------- */
    private void rebuild() {
        axesGroup.getChildren().clear();
        gridGroup.getChildren().clear();
        ticksGroup.getChildren().clear();
        vectorsGroup.getChildren().clear();
        if (world == null) return;

        /* escala em função do maior vetor */
        double maxLen = world.getVectors().stream()
                .mapToDouble(v -> magnitude(v.toArray()))
                .max().orElse(1);
        double spanUnits = Math.max(2, maxLen * 1.2);   // margem
        currentScale = 200 / spanUnits;

        if (showGrid.get())  buildGrids(spanUnits);
        if (showTicks.get()) buildTicks(spanUnits);
        buildAxes(spanUnits);

        int idx = 0;
        for (ObservableVector v : world.getVectors()) {
            if (magnitude(v.toArray()) < 1e-6) continue;
            vectorsGroup.getChildren().add(buildArrow(v, pickColor(idx++), "v"+idx));
        }
    }

    /* ---------- grids em XY, XZ, YZ ---------- */
    private void buildGrids(double spanUnits) {
        int lines = (int)Math.ceil(spanUnits);
        double lenPx = lines * 2 * currentScale;
        PhongMaterial mat = new PhongMaterial(Color.grayRgb(180, 1));

        // GRID NO PLANO XZ (Y = 0)
        for (int k = -lines; k <= lines; k++) {
            // linhas paralelas ao X (varia Z)
            Cylinder c1 = new Cylinder(0.3, lenPx);
            c1.setMaterial(mat);
            c1.getTransforms().addAll(
                    new Rotate(90, Rotate.Z_AXIS), // alinhar no eixo X
                    new Translate(0, 0, k * currentScale)
            );
            gridGroup.getChildren().add(c1);

            // linhas paralelas ao Z (varia X)
            Cylinder c2 = new Cylinder(0.3, lenPx);
            c2.setMaterial(mat);
            c2.getTransforms().addAll(
                    new Rotate(90, Rotate.X_AXIS), // alinhar no eixo Z
                    new Translate(k * currentScale, 0, 0)
            );
            gridGroup.getChildren().add(c2);
        }

        // GRID NO PLANO XY (Z = 0)
        for (int k = -lines; k <= lines; k++) {
            // linhas paralelas ao X (varia Y)
            Cylinder c3 = new Cylinder(0.3, lenPx);
            c3.setMaterial(mat);
            c3.getTransforms().addAll(
                    new Rotate(90, Rotate.Y_AXIS),
                    new Translate(0, k * currentScale, 0)
            );
            gridGroup.getChildren().add(c3);

            // linhas paralelas ao Y (varia X)
            Cylinder c4 = new Cylinder(0.3, lenPx);
            c4.setMaterial(mat);
            c4.getTransforms().addAll(
                    new Rotate(0, Rotate.X_AXIS),
                    new Translate(k * currentScale, 0, 0)
            );
            gridGroup.getChildren().add(c4);
        }

        // GRID NO PLANO YZ (X = 0)
        for (int k = -lines; k <= lines; k++) {
            // linhas paralelas ao Y (varia Z)
            Cylinder c5 = new Cylinder(0.3, lenPx);
            c5.setMaterial(mat);
            c5.getTransforms().addAll(
                    new Rotate(0, Rotate.Z_AXIS),
                    new Translate(0, 0,k * currentScale )
            );
            gridGroup.getChildren().add(c5);

            // linhas paralelas ao Z (varia Y)
            Cylinder c6 = new Cylinder(0.3, lenPx);
            c6.setMaterial(mat);
            c6.getTransforms().addAll(
                    new Rotate(90, Rotate.Y_AXIS),
                    new Translate(0, k * currentScale, 0)
            );
            gridGroup.getChildren().add(c6);
        }
    }

    /* ---------- ticks nos 3 eixos ---------- */
    private void buildTicks(double spanUnits) {
        int ticks = (int)Math.ceil(spanUnits);
        PhongMaterial mat = new PhongMaterial(Color.DARKGRAY);

        double size = 6;                       // tamanho do tick
        for (int k = -ticks; k <= ticks; k++) {
            if (k == 0) continue;
            double offset = k * currentScale;

            ticksGroup.getChildren().add(newBox(size, mat,
                    new Translate(offset, 0, 0)));          // X-axis
            ticksGroup.getChildren().add(newBox(size, mat,
                    new Translate(0, offset, 0)));          // Y-axis
            ticksGroup.getChildren().add(newBox(size, mat,
                    new Translate(0, 0, offset)));          // Z-axis
        }
    }

    /* ---------- eixos bidirecionais ---------- */
    private void buildAxes(double spanUnits) {
        double len = spanUnits * currentScale;
        double shaftRadius = 1.0;
        double headLen = 12;
        double headRad = 4;

        // EIXO X (horizontal)
        Cylinder x = new Cylinder(shaftRadius, len * 2);
        x.setMaterial(new PhongMaterial(Color.RED));
        x.getTransforms().addAll(
                new Rotate(90, Rotate.Z_AXIS),
                new Translate(0, 0, 0)  // centrado na origem
        );

        Cone xHead = new Cone((float) headRad, (float) headLen, 24, Color.RED);
        xHead.getTransforms().addAll(
                new Rotate(90, Rotate.Z_AXIS),
                new Translate(len, 0, 0)
        );

        Text xLabel = new Text("X");
        xLabel.setFill(Color.RED.darker());
        xLabel.getTransforms().add(new Translate(len + headLen + 6, 0, 0));

        // EIXO Y (vertical)
        Cylinder y = new Cylinder(shaftRadius, len * 2);
        y.setMaterial(new PhongMaterial(Color.GREEN));
        y.getTransforms().add(new Translate(0, 0, 0));

        Cone yHead = new Cone((float) headRad, (float) headLen, 24, Color.GREEN);
        yHead.getTransforms().addAll(
                new Translate(0, len, 0)
        );

        Text yLabel = new Text("Y");
        yLabel.setFill(Color.GREEN.darker());
        yLabel.getTransforms().add(new Translate(0, len + headLen + 6, 0));

        // EIXO Z (profundidade)
        Cylinder z = new Cylinder(shaftRadius, len * 2);
        z.setMaterial(new PhongMaterial(Color.BLUE));
        z.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS),
                new Translate(0, 0, 0)
        );

        Cone zHead = new Cone((float) headRad, (float) headLen, 24, Color.BLUE);
        zHead.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS),
                new Translate(0, 0, len)
        );

        Text zLabel = new Text("Z");
        zLabel.setFill(Color.BLUE.darker());
        zLabel.getTransforms().add(new Translate(0, 0, len + headLen + 6));

        // Adiciona todos ao grupo
        axesGroup.getChildren().addAll(
                x, xHead, xLabel,
                y, yHead, yLabel,
                z, zHead, zLabel
        );
    }

    /* ---------- seta de vetor ---------- */
    private Group buildArrow(ObservableVector v, Color color, String label) {
        double lenPx   = magnitude(v.toArray()) * currentScale;
        double headLen = lenPx * 0.12, headRad = headLen * 0.35, shaftRad = 2, shaftLen = lenPx - headLen;

        Cylinder shaft = new Cylinder(shaftRad, Math.max(shaftLen, 1));
        shaft.setMaterial(new PhongMaterial(color));
        shaft.getTransforms().add(new Translate(0, -shaftLen / 2, 0));

        Cone head = new Cone((float) headRad, (float) headLen, 24, color);
        head.getTransforms().addAll(new Translate(0, -lenPx + headLen/2, 0));
        Group arrow = new Group(shaft, head);
        arrow.getTransforms().add(orientFromYAxis(v.getX(), v.getY(), v.getZ()));

        Text t = new Text(label + String.format(" (%.1f, %.1f, %.1f)", v.getX(), v.getY(), v.getZ()));
        t.setFill(color.darker());
        t.setTranslateY(-lenPx - 12);
        // arrow.getChildren().add(t);
        return arrow;
    }

    /* ---------- helpers / util ---------- */
    private Color pickColor(int i) {
        return switch (i) {
            case 0 -> Color.RED;
            case 1 -> Color.DODGERBLUE;
            default -> Color.hsb(i * 40 % 360, 0.8, 0.9);
        };
    }

    private Transform orientFromYAxis(double x, double y, double z) {
        Point3D dir = new Point3D(x, y, z);
        if (dir.magnitude() < 1e-6) return new Rotate();
        dir = dir.normalize();
        Point3D axis = new Point3D(-dir.getZ(), 0, dir.getX());
        double angle = Math.toDegrees(Math.acos(dir.getY()));
        return axis.magnitude() < 1e-6 ? new Rotate(dir.getY() > 0 ? 0 : 180, Rotate.X_AXIS)
                : new Rotate(angle, axis);
    }

    private Box newBox(double s, PhongMaterial m, Transform... t) {
        Box b = new Box(s, s, s);
        b.setMaterial(m);
        b.getTransforms().addAll(t);
        return b;
    }

    /* ---------- mouse orbit ---------- */
    private void initMouseOrbit() {
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            anchorX = e.getSceneX();  anchorY = e.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            rotateX.setAngle(anchorAngleX - (e.getSceneY() - anchorY));
            rotateY.setAngle(anchorAngleY + (e.getSceneX() - anchorX));
        });
    }
}
