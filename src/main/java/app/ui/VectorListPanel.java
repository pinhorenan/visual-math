package app.ui;

import app.model.ObservableVector;
import app.model.VectorWorld;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class VectorListPanel extends VBox {
    public VectorListPanel(VectorWorld world) {
        Button add = new Button("Novo vetor");
        add.setOnAction(_ -> world.add(new ObservableVector(1, 0, 0)));

        ListView<ObservableVector> list = new ListView<>(world.getVectors());
        list.setCellFactory(_ -> new VectorCell(world.getVectors()));

        setSpacing(8);
        getChildren().addAll(add, list);
    }
}
