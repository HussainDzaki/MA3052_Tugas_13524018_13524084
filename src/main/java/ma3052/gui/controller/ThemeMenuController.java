package ma3052.gui.controller;

import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import ma3052.App;

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
        Parent thisRoot = clickedButton.getScene().getRoot();
        mainRoot.getStyleClass().removeIf((style) -> style.contains("theme"));
        mainRoot.getStyleClass().addAll(theme, theme + "-root");
        thisRoot.getStyleClass().removeIf((style) -> style.contains("-root"));
        thisRoot.getStyleClass().add(theme + "-root");

        Preferences prefs = Preferences.userNodeForPackage(App.class);
        prefs.put("Theme", theme);
        try {
            prefs.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
