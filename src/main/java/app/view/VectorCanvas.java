package app.view;

import app.model.VectorWorld;
import javafx.scene.Node;

public interface VectorCanvas {
    void bind(VectorWorld world);
    Node getView();
}
