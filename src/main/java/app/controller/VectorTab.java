// File: app/controller/VectorTab.java
package app.controller;

import app.model.VectorWorld;
import app.ui.VectorListPanel;
import app.view.Canvas2D;
import app.view.Canvas3D;
import app.view.VectorCanvas;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VectorTab extends BorderPane {

    private final StackPane canvasPane = new StackPane();
    private final Canvas2D canvas2D = new Canvas2D();
    private final Canvas3D canvas3D = new Canvas3D();
    private final VectorWorld world = new VectorWorld();

    public VectorTab() {
        // bind dos dois canvases ao mesmo mundo
        canvas2D.bind(world);
        canvas3D.bind(world);

        canvas3D.showGridProperty().bind(canvas2D.showGridProperty());
        canvas3D.showTicksProperty().bind(canvas2D.showTicksProperty());

        canvasPane.getChildren().add(canvas2D.getView());

        // painel de vetores e de exibição (igual antes)
        TitledPane vectorsPane = new TitledPane("Vetores", new VectorListPanel(world));
        vectorsPane.setCollapsible(false);

        VBox checks = new VBox(6,
                makeCheck("Resultado (v₁+v₂)", canvas2D.showResultProperty()),
                makeCheck("Coordenadas",          canvas2D.showCoordProperty()),
                makeCheck("Ângulo",               canvas2D.showAngleProperty()),
                makeCheck("Ortogonalidade",       canvas2D.showOrthoProperty()),
                makeCheck("Ticks",                canvas2D.showTicksProperty()),
                makeCheck("Grade",                canvas2D.showGridProperty())
        );
        checks.setPadding(new Insets(8));
        TitledPane displayPane = new TitledPane("Exibição", checks);
        displayPane.setCollapsible(false);

        VBox side = new VBox(10, vectorsPane, displayPane);
        side.setPrefWidth(300);
        side.setPadding(new Insets(10));

        setCenter(canvasPane);
        setLeft(side);

        canvasPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        BorderPane.setAlignment(canvasPane, Pos.CENTER);

        // dispara troca de canvas quando vetores mudam ou qualquer zProperty muda
        world.getVectors().addListener((ListChangeListener<? super app.model.ObservableVector>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(v ->
                            v.zProperty().addListener(o -> switchCanvas())
                    );
                }
            }
            switchCanvas();
        });
        // para vetores já existentes
        world.getVectors().forEach(v ->
                v.zProperty().addListener(o -> switchCanvas())
        );
    }

    private void switchCanvas() {
        boolean any3D = world.getVectors().stream()
                .anyMatch(v -> Math.abs(v.getZ()) > 1e-6);
        Node view = any3D ? canvas3D.getView() : canvas2D.getView();
        canvasPane.getChildren().setAll(view);
    }

    private javafx.scene.control.CheckBox makeCheck(String label, javafx.beans.property.BooleanProperty prop){
        javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox(label);
        cb.selectedProperty().bindBidirectional(prop);
        return cb;
    }
}
