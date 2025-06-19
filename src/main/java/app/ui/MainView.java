package app.ui;

import app.controller.VectorTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MainView extends VBox {
    public MainView() {
        TabPane tabs = new TabPane();
        VBox.setVgrow(tabs, Priority.ALWAYS);

        javafx.scene.layout.VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);

        Tab tab1 = new Tab("Vetores", new VectorTab());

        tabs.getTabs().addAll(tab1);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.getChildren().add(tabs);
    }
}
