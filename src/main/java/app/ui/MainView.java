package app.ui;

import app.controller.VectorTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class MainView extends VBox {
    public MainView() {
        TabPane tabs = new TabPane();

        Tab tab1 = new Tab("Vetores", new VectorTab());
        Tab tab2 = new Tab("Álgebra Linear");
        Tab tab3 = new Tab("Cálculo");

        tab2.setContent(new VBox());
        tab3.setContent(new VBox());

        tabs.getTabs().addAll(tab1, tab2, tab3);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.getChildren().add(tabs);
    }

}
