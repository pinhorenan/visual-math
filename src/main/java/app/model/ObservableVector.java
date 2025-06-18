package app.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import java.util.Arrays;

/**
 * Vetor observável de 2 ou 3 dimensões.
 * Permite uso com propriedades reativas em JavaFX.
 */
public class ObservableVector {
    private final DoubleProperty[] c;

    public ObservableVector(double x, double y) {
        this.c = new DoubleProperty[] {
                new SimpleDoubleProperty(x),
                new SimpleDoubleProperty(y)
        };
    }

    public ObservableVector(double x, double y, double z) {
        this.c = new DoubleProperty[] {
                new SimpleDoubleProperty(x),
                new SimpleDoubleProperty(y),
                new SimpleDoubleProperty(z)
        };
    }

    public int dimension() {
        return c.length;
    }

    public double getX() { return c[0].get(); }
    public void setX(double x) { c[0].set(x); }
    public double getY() { return c[1].get(); }
    public void setY(double y) { c[1].set(y); }
    public double getZ() { return c.length >= 3 ? c[2].get() : 0; }
    public void setZ(double z) { c[2].set(z); }

    public DoubleProperty xProperty() { return c[0]; }
    public DoubleProperty yProperty() { return c[1]; }
    public DoubleProperty zProperty() { return c.length >= 3 ? c[2] : new SimpleDoubleProperty(0); }

    public double[] toArray() {
        return Arrays.stream(c).mapToDouble(DoubleProperty::get).toArray();
    }
}
