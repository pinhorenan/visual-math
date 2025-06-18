package app.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class VectorWorld {
    private final ObservableList<ObservableVector> vectors = FXCollections.observableArrayList();

    public ObservableList<ObservableVector> getVectors() {
        return vectors;
    }

    public void add(ObservableVector v) {
        vectors.add(v);
    }

    public void clear() {
        vectors.clear();
    }

    public int dimension() {
        if (vectors.isEmpty()) return 2;
        return vectors.stream().anyMatch(v -> v.dimension() >= 3 && v.getZ() != 0) ? 3 : 2;
    }
}
