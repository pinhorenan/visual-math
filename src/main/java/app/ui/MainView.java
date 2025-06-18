package app.ui;

import app.controller.Vector2DTab;
import app.controller.Vector3DTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class MainView extends VBox {
    public MainView() {
        TabPane tabs = new TabPane();

        Tab tab1 = new Tab("Vetores 2D", new Vector2DTab());
        Tab tab2 = new Tab("Vetores 3D", new Vector3DTab());
        Tab tab3 = new Tab("Álgebra Linear");
        Tab tab4 = new Tab("Cálculo");

        tab2.setContent(new VBox());
        tab3.setContent(new VBox());
        tab4.setContent(new VBox());

        tabs.getTabs().addAll(tab1, tab2, tab3, tab4);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.getChildren().add(tabs);
    }

}
