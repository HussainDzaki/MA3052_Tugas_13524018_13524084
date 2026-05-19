package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ma3052.App;
import ma3052.core.graph.Graph;
import ma3052.core.graph.GraphFactory;
import ma3052.core.graph.GridGraph;
import ma3052.core.graph.Node;
import ma3052.core.graph.PointGraph;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.GridGraphGUI;
import ma3052.gui.graph.PointGraphGUI;

/**
 * Controller for Graph Visualization GUI
 * Manages the visualization and interaction with graph data structure
 * with animated DFS/BFS algorithm visualization and force-directed layout
 */
public class GraphVisualizationController {
    public static GraphVisualizationController instance = null;

    // Graph data structure
    private GraphGUI graphGUI;
    private PointGraphGUI pointGraphGUI;
    private GridGraphGUI gridGraphGUI;
    private volatile boolean isAnimating = false;

    // FXML UI Components
    @FXML
    private Label appLabel;

    @FXML
    private Canvas graphCanvas1;
    @FXML
    private Canvas graphCanvas2;
    @FXML
    private Canvas graphCanvas3;

    @FXML
    private Button btnLockMode;
    @FXML
    private Button btnAddMode;
    @FXML
    private Button btnDeleteMode;
    @FXML
    private Button btnEditMode;

    @FXML
    private GraphInputMenuController graphInputMenuController;
    @FXML
    private GraphAlgorithmMenuController graphAlgorithmMenuController;

    public enum ModeGUI {
        NODE_AND_EDGES_MODE, GRID_MODE, POINT_MODE;
    }

    private ModeGUI mode = ModeGUI.NODE_AND_EDGES_MODE;

    public void setGraph(Graph graph) {
        graphGUI.setGraph(graph);
    }

    public Graph getGraph() {
        return graphGUI.getGraph();
    }

    public GraphGUI getGraphGUI() {
        return graphGUI;
    }

    public GridGraphGUI getGridGraphGUI() {
        return gridGraphGUI;
    }

    public PointGraphGUI getPointGraphGUI() {
        return pointGraphGUI;
    }

    public ModeGUI getMode() {
        return mode;
    }

    public void setMode(ModeGUI mode) {
        this.mode = mode;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean isAnimating) {
        this.isAnimating = isAnimating;
    }

    public GraphAlgorithmMenuController getGraphAlgorithmMenuController() {
        return graphAlgorithmMenuController;
    }

    public GraphInputMenuController getGraphInputMenuController() {
        return graphInputMenuController;
    }

    /**
     * Initialize the controller
     * Called after FXML file has been loaded
     */
    @FXML
    public void initialize() {
        instance = this;

        // Initialize graph
        graphGUI = new GraphGUI(graphCanvas1);
        gridGraphGUI = new GridGraphGUI(graphCanvas2);
        pointGraphGUI = new PointGraphGUI(graphCanvas3);

        graphGUI.setDrawing(true);
        gridGraphGUI.setDrawing(false);
        pointGraphGUI.setDrawing(false);

        graphGUI.getCanvas().setManaged(true);
        gridGraphGUI.getCanvas().setManaged(false);
        pointGraphGUI.getCanvas().setManaged(false);

        graphGUI.getCanvas().setVisible(true);
        gridGraphGUI.getCanvas().setVisible(false);
        pointGraphGUI.getCanvas().setVisible(false);

        graphGUI.setGraph(getDefaultGraph());
        gridGraphGUI.setGridGraph(getDefaultGridGraph());
        pointGraphGUI.setGraph(new PointGraph());

        graphInputMenuController.setMainController(this);
        graphAlgorithmMenuController.setMainController(this);

        // Log initialization
        logMessage("Graph Visualization initialized successfully");

        Platform.runLater(() -> {
            // Remove focus on the first button, make it focus a non button
            appLabel.requestFocus();
            graphInputMenuController.updateListFromGraph();

            graphCanvas1.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                // Stop both drawing thread
                graphGUI.stop();
                gridGraphGUI.stop();
                pointGraphGUI.stop();
            });
        });
    }

    /**
     * Add a message to the log area
     */
    public void logMessage(String message) {
        graphAlgorithmMenuController.logMessage(message);
    }

    /**
     * Show an error message
     */
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logMessage("[ERROR] " + message);
    }

    public void switchToLockMode() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE && graphGUI.getMode() == GraphGUI.Mode.Lock)
            return;
        if (mode == ModeGUI.POINT_MODE && pointGraphGUI.getMode() == PointGraphGUI.Mode.Lock)
            return;
        graphGUI.setMode(GraphGUI.Mode.Lock);
        pointGraphGUI.setMode(PointGraphGUI.Mode.Lock);
        Platform.runLater(() -> {
            btnLockMode.getStyleClass().removeAll("button-secondary-3");
            btnLockMode.getStyleClass().add("button-secondary-1");
            btnAddMode.getStyleClass().removeAll("button-secondary-1");
            btnAddMode.getStyleClass().add("button-secondary-3");
            btnDeleteMode.getStyleClass().removeAll("button-secondary-1");
            btnDeleteMode.getStyleClass().add("button-secondary-3");
            btnEditMode.getStyleClass().removeAll("button-secondary-1");
            btnEditMode.getStyleClass().add("button-secondary-3");
        });
    }

    public void switchToAddMode() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE && graphGUI.getMode() == GraphGUI.Mode.Add)
            return;
        if (mode == ModeGUI.POINT_MODE && pointGraphGUI.getMode() == PointGraphGUI.Mode.Add)
            return;
        graphGUI.setMode(GraphGUI.Mode.Add);
        pointGraphGUI.setMode(PointGraphGUI.Mode.Add);
        Platform.runLater(() -> {
            btnLockMode.getStyleClass().removeAll("button-secondary-1");
            btnLockMode.getStyleClass().add("button-secondary-3");
            btnAddMode.getStyleClass().removeAll("button-secondary-3");
            btnAddMode.getStyleClass().add("button-secondary-1");
            btnDeleteMode.getStyleClass().removeAll("button-secondary-1");
            btnDeleteMode.getStyleClass().add("button-secondary-3");
            btnEditMode.getStyleClass().removeAll("button-secondary-1");
            btnEditMode.getStyleClass().add("button-secondary-3");
        });
    }

    public void switchToDeleteMode() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE && graphGUI.getMode() == GraphGUI.Mode.Delete)
            return;
        if (mode == ModeGUI.POINT_MODE && pointGraphGUI.getMode() == PointGraphGUI.Mode.Delete)
            return;
        graphGUI.setMode(GraphGUI.Mode.Delete);
        pointGraphGUI.setMode(PointGraphGUI.Mode.Delete);
        Platform.runLater(() -> {
            btnLockMode.getStyleClass().removeAll("button-secondary-1");
            btnLockMode.getStyleClass().add("button-secondary-3");
            btnAddMode.getStyleClass().removeAll("button-secondary-1");
            btnAddMode.getStyleClass().add("button-secondary-3");
            btnDeleteMode.getStyleClass().removeAll("button-secondary-3");
            btnDeleteMode.getStyleClass().add("button-secondary-1");
            btnEditMode.getStyleClass().removeAll("button-secondary-1");
            btnEditMode.getStyleClass().add("button-secondary-3");
        });
    }

    public void switchToEditMode() {
        showError("Not implemented yet");
        return;
        // if (graphGUI.getMode() == GraphGUI.Mode.Edit)
        // return;
        // graphGUI.setMode(GraphGUI.Mode.Edit);
        // btnLockMode.getStyleClass().removeAll("button-secondary-1");
        // btnLockMode.getStyleClass().add("button-secondary-3");
        // btnAddMode.getStyleClass().removeAll("button-secondary-1");
        // btnAddMode.getStyleClass().add("button-secondary-3");
        // btnDeleteMode.getStyleClass().removeAll("button-secondary-1");
        // btnDeleteMode.getStyleClass().add("button-secondary-3");
        // btnEditMode.getStyleClass().removeAll("button-secondary-3");
        // btnEditMode.getStyleClass().add("button-secondary-1");
    }

    private Graph getDefaultGraph() {
        return GraphFactory.createCirculantGraph(7, 1, 2);
        // Graph graph = new Graph();
        // graph.addNode(new Node("1"));
        // graph.addNode(new Node("2"));
        // graph.addNode(new Node("3"));
        // graph.addNode(new Node("4"));
        // graph.addNode(new Node("5"));
        // graph.addNode(new Node("6"));
        // graph.addNode(new Node("7"));
        // graph.addEdge("1", "2");
        // graph.addEdge("1", "5");
        // graph.addEdge("1", "6");
        // graph.addEdge("2", "5");
        // graph.addEdge("2", "3");
        // graph.addEdge("3", "6");
        // graph.addEdge("4", "5");
        // graph.addEdge("4", "6");
        // graph.addEdge("4", "7");
        // graph.addEdge("5", "7");
        // return graph;
    }

    private GridGraph getDefaultGridGraph() {
        GridGraph graph = new GridGraph(7, 7);
        char[][] grid = {
                { '.', '#', '.', '#', '.', '.', '.' },
                { '#', '#', '.', '.', '.', '#', '#' },
                { '.', '#', '#', '#', '#', '#', '#' },
                { '.', '.', '.', '.', '.', '#', '.' },
                { '.', '#', '.', '#', '#', '.', '.' },
                { '#', '#', '.', '#', '#', '#', '.' },
                { '.', '#', '.', '#', '#', '#', '.' },
                { '.', '.', '.', '.', '#', '#', '.' },
        };
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                graph.setNodeType(i, j, grid[i][j]);
            }
        }
        return graph;
    }

    @FXML
    private void handleSelectTheme() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/ThemeMenu.fxml"));

            Parent root = loader.load();
            loader.<ThemeMenuController>getController().setMainController(this);

            Parent thisRoot = appLabel.getScene().getRoot();
            root.getStyleClass().removeIf((style) -> style.contains("-root"));
            thisRoot.getStyleClass().forEach((style) -> {
                if (style.contains("-root")) {
                    root.getStyleClass().add(style);
                }
            });

            // Create scene
            Scene scene = new Scene(root, 700, 500);

            // Setup stage
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(appLabel.getScene().getWindow());
            stage.setTitle("Theme Select");
            stage.setScene(scene);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.showAndWait();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void zoomIn() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE) {
            graphGUI.zoomIn();
        } else if (mode == ModeGUI.POINT_MODE) {
            pointGraphGUI.zoomIn();
        }
    }

    @FXML
    private void zoomOut() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE) {
            graphGUI.zoomOut();
        } else if (mode == ModeGUI.POINT_MODE) {
            pointGraphGUI.zoomOut();
        }
    }
}
