package ma3052;

import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX Application Entry Point
 * Launches the Graph Visualization GUI
 */
public class App extends Application {

    /**
     * Start the JavaFX application
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        loadFonts();

        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/GraphVisualization.fxml"));
        Parent root = loader.load();

        Preferences prefs = Preferences.userNodeForPackage(App.class);
        String theme = prefs.get("Theme", "theme-1");
        root.getStyleClass().removeIf((style) -> style.contains("theme"));
        root.getStyleClass().addAll(theme, theme + "-root");

        // Create scene
        Scene scene = new Scene(root, 1280, 720);

        // Setup stage
        primaryStage.setTitle("Graph Visualization");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void loadFonts() {
        Font.loadFont(App.class.getResourceAsStream("font/Cascadia_Code/static/CascadiaCode-Regular.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Thin.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Thin.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-ExtraLight.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Light.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Regular.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Medium.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-SemiBold.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Bold.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-ExtraBold.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Bold.ttf"), 14);
        Font.loadFont(App.class.getResourceAsStream("font/Lexend/static/Lexend-Black.ttf"), 14);
    }
}