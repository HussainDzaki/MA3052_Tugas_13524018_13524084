package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class ThemeMenuController {
    private GraphVisualizationController mainController;

    public void setMainController(GraphVisualizationController controller) {
        mainController = controller;
    }

    @FXML
    private void initialize() {

    }

    @FXML
    private void handleApplyTheme(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String theme = clickedButton.getId();
        
        Parent mainRoot = mainController.getGraphGUI().getCanvas().getScene().getRoot();
        Platform.runLater(() -> {
            mainRoot.getStyleClass().removeAll("theme-1", "theme-2");
            mainRoot.getStyleClass().add(theme);
        });
    }
}
