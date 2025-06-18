package app.ui;

import app.model.ObservableVector;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**  Uma célula com _spinners_ que atualizam o vetor em tempo real. */
public class VectorCell extends ListCell<ObservableVector> {

    private final HBox box = new HBox(6);
    private final Spinner<Double> sx = buildSpinner();
    private final Spinner<Double> sy = buildSpinner();
    private final Spinner<Double> sz = buildSpinner();

    public VectorCell(javafx.collections.ObservableList<ObservableVector> list) {
        box.setPadding(new Insets(4));
        Button remove = new Button("×");
        box.getChildren().addAll(
                new Label("x"), sx,
                new Label("y"), sy,
                new Label("z"), sz,
                remove
        );
        HBox.setHgrow(sx, Priority.ALWAYS);
        HBox.setHgrow(sy, Priority.ALWAYS);
        HBox.setHgrow(sz, Priority.ALWAYS);

        remove.setOnAction(_ -> {
            if (getItem() != null) list.remove(getItem());
        });
    }

    @Override protected void updateItem(ObservableVector v, boolean empty) {
        super.updateItem(v, empty);
        if (empty || v == null) {
            setGraphic(null);
            return;
        }
        bindSpinner(sx, v.xProperty());
        bindSpinner(sy, v.yProperty());
        bindSpinner(sz, v.zProperty());
        setGraphic(box);
    }

    /* ------------ helpers ------------ */

    private Spinner<Double> buildSpinner() {
        Spinner<Double> sp = new Spinner<>(-20, 20, 0, 0.1);
        sp.setPrefWidth(60);
        sp.setEditable(true);
        return sp;
    }
    /** Mantém o spinner e a propriedade sincronizados a cada digitação. */
    private void bindSpinner(Spinner<Double> sp, javafx.beans.property.DoubleProperty prop) {
        // inicial
        sp.getValueFactory().setValue(prop.get());
        // 1) spinner → vetor
        sp.valueProperty().addListener((_, _, newV) -> prop.set(newV));
        // 2) editor de texto enquanto digita
        sp.getEditor().textProperty().addListener((_, _, txt) -> {
            try { prop.set(Double.parseDouble(txt.replace(',','.'))); }
            catch (NumberFormatException ignored) {}
        });
        // 3) vetor → spinner (caso outra parte altere)
        prop.addListener((_, _, newV) -> {
            if (!sp.isFocused()) sp.getValueFactory().setValue(newV.doubleValue());
        });
    }
}
