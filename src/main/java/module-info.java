module ma3052 {
    requires javafx.controls;
    requires javafx.fxml;

    opens ma3052 to javafx.fxml;
    opens ma3052.controller to javafx.fxml;
    opens ma3052.graph to javafx.fxml;
    exports ma3052;
    exports ma3052.controller;
    exports ma3052.graph;
}
