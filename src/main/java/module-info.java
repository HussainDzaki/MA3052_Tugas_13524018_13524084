module ma3052 {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens ma3052 to javafx.fxml;
    opens ma3052.gui.controller to javafx.fxml;
    opens ma3052.gui.graph to javafx.fxml;
    opens ma3052.core.graph to javafx.fxml;
    exports ma3052;
    exports ma3052.gui.controller;
    exports ma3052.gui.graph;
    exports ma3052.core.graph;
}
