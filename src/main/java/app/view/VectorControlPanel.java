package app.view;

import app.model.ObservableVector2D;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.util.Locale;

/**
 * Dois blocos (v₁ & v₂) sempre abertos.
 * Spinner + Slider para X e Y, lado a lado.
 * Opções refinadas (ticks & grade).
 */
public class VectorControlPanel extends VBox {

    public VectorControlPanel(ObservableVector2D v1,
                              ObservableVector2D v2,
                              VectorCanvas canvas) {
        super(16);
        setPadding(new Insets(16));
        setPrefWidth(310);
        setStyle("""
                -fx-background-color: #f8f8f8;
                -fx-border-color: #ccc;
                -fx-border-width: 0 1 0 0;
                """);

        getChildren().setAll(
                vectorBlock("v₁", v1),
                vectorBlock("v₂", v2),
                new Separator(),
                optionsBox(canvas)
        );
    }

    /* -------- bloco de um vetor -------- */
    private VBox vectorBlock(String title, ObservableVector2D vec) {
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em;");

        /* linha dos spinners (X,Y) */
        Spinner<Double> spinX = spinnerFor(vec.xProperty());
        Spinner<Double> spinY = spinnerFor(vec.yProperty());

        HBox spinnerRow = new HBox(10,
                new Label("X:"), spinX,
                new Label("Y:"), spinY);
        spinnerRow.setSpacing(8);

        /* sliders */
        Slider sldX = sliderFor(vec.xProperty());
        Slider sldY = sliderFor(vec.yProperty());

        VBox box = new VBox(6,
                header,
                spinnerRow,
                new HBox(new Label("X:"), sldX),
                new HBox(new Label("Y:"), sldY)
        );
        box.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #ddd;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-padding: 10;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4,0,0,1);
                """);
        return box;
    }

    /* -------- spinner & slider helpers -------- */
    private Spinner<Double> spinnerFor(DoubleProperty prop) {
        // cria o ValueFactory normalmente
        var vf = new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 10, prop.get(), 0.1);
        vf.setConverter(new StringConverter<>() {
            @Override public String toString(Double v) {
                if (v == null) return "";
                return v % 1 == 0
                        ? String.format("%.0f", v)
                        : String.format(Locale.US, "%.2f", v);
            }
            @Override public Double fromString(String s) {
                try {
                    return Double.parseDouble(s.replace(',', '.'));
                } catch (Exception e) {
                    return prop.get();
                }
            }
        });

        Spinner<Double> sp = new Spinner<>(vf);
        sp.setEditable(true);

        // aqui é o pulo: bindBidirectional com prop.asObject()
        vf.valueProperty().bindBidirectional(prop.asObject());

        sp.setPrefWidth(70);
        return sp;
    }


    private Slider sliderFor(DoubleProperty prop) {
        Slider s = new Slider(-10, 10, prop.get());
        s.setShowTickMarks(true);
        s.setMajorTickUnit(1);
        s.setBlockIncrement(0.1);
        s.valueProperty().bindBidirectional(prop);
        s.setPrefWidth(180);
        return s;
    }

    /* -------- opções globais -------- */
    private VBox optionsBox(VectorCanvas canvas) {
        VBox box = new VBox(6,
                check("Mostrar coordenadas",   canvas.showCoordsProperty()),
                check("Mostrar ângulo",        canvas.showAngleProperty()),
                check("Mostrar ortogonalidade",canvas.showOrthoProperty()),
                check("Mostrar v₁+v₂",canvas.showResultProperty()),
                check("Mostrar ticks",         canvas.showTicksProperty()),
                check("Mostrar grade",         canvas.showGridProperty())
        );
        box.setStyle("""
                -fx-padding: 8;
                -fx-border-color: #ccc;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                """);
        return box;
    }

    private CheckBox check(String text, javafx.beans.property.BooleanProperty prop) {
        CheckBox cb = new CheckBox(text);
        cb.selectedProperty().bindBidirectional(prop);
        return cb;
    }
}
