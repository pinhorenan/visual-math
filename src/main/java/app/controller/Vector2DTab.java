package app.controller;

import app.model.ObservableVector2D;
import app.view.VectorCanvas;
import app.view.VectorControlPanel;
import javafx.scene.layout.BorderPane;

/**
 * Controller de alto nível: agrega o Canvas e o Sidebar de controles.
 */
public class Vector2DTab extends BorderPane {

    public Vector2DTab() {
        ObservableVector2D v1 = new ObservableVector2D(0, 0);
        ObservableVector2D v2 = new ObservableVector2D(0, 0);
        VectorCanvas canvas = new VectorCanvas(v1, v2);
        VectorControlPanel sidebar = new VectorControlPanel(v1, v2, canvas);

        setLeft(sidebar);
        setCenter(canvas);
    }
}
