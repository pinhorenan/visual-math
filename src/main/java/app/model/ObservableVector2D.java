package app.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Vetor 2D com propriedades observáveis para bind bidirecional.
 */
public class ObservableVector2D {

    private final DoubleProperty x = new SimpleDoubleProperty();
    private final DoubleProperty y = new SimpleDoubleProperty();

    public ObservableVector2D(double x, double y) {
        this.x.set(x);
        this.y.set(y);
    }

    public DoubleProperty xProperty() { return x; }
    public DoubleProperty yProperty() { return y; }

    public double getX() { return x.get(); }
    public void setX(double v) { x.set(v); }
    public double getY() { return y.get(); }
    public void setY(double v) { y.set(v); }
}
