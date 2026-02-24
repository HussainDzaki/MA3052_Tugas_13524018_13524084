module ma3052 {
    requires javafx.controls;
    requires javafx.fxml;

    opens ma3052 to javafx.fxml;
    exports ma3052;
}
