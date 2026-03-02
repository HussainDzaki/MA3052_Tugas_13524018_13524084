package ma3052;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("graphvisualization.fxml"));
        Parent root = loader.load();
        
        // Create scene
        Scene scene = new Scene(root, 1100, 600);
        
        // Setup stage
        primaryStage.setTitle("Graph Visualization - DFS/BFS Algorithm");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1100);
        primaryStage.setHeight(700);
        primaryStage.show();
    }

    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}