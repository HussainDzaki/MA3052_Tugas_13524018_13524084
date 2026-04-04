package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import ma3052.core.graph.Graph;
import ma3052.core.graph.GridGraph;
import ma3052.core.graph.Node;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.GridGraphGUI;

/**
 * Controller for Graph Visualization GUI
 * Manages the visualization and interaction with graph data structure
 * with animated DFS/BFS algorithm visualization and force-directed layout
 */
public class GraphVisualizationController {
    public static GraphVisualizationController instance = null;

    // Graph data structure
    private GraphGUI graphGUI;
    private GridGraphGUI gridGraphGUI;
    private volatile boolean isAnimating = false;

    // FXML UI Components
    @FXML
    private Label appLabel;

    @FXML
    private Canvas graphCanvas;

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
        NODE_AND_EDGES_MODE, GRID_MODE;
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

    /**
     * Initialize the controller
     * Called after FXML file has been loaded
     */
    @FXML
    public void initialize() {
        instance = this;

        // Initialize graph
        graphGUI = new GraphGUI(graphCanvas);
        gridGraphGUI = new GridGraphGUI(graphCanvas);

        graphGUI.setDrawing(true);
        gridGraphGUI.setDrawing(false);

        graphGUI.setGraph(getDefaultGraph());
        gridGraphGUI.setGridGraph(getDefaultGridGraph());

        graphInputMenuController.setMainController(this);
        graphAlgorithmMenuController.setMainController(this);

        // Log initialization
        logMessage("Graph Visualization initialized successfully");

        Platform.runLater(() -> {
            // Remove focus on the first button, make it focus a non button
            appLabel.requestFocus();
            graphInputMenuController.updateListFromGraph();

            graphCanvas.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                // Stop both drawing thread
                graphGUI.stop();
                gridGraphGUI.stop();
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
        if (graphGUI.getMode() == GraphGUI.Mode.Lock)
            return;
        graphGUI.setMode(GraphGUI.Mode.Lock);
        btnLockMode.getStyleClass().remove("button-dark-blue");
        btnLockMode.getStyleClass().add("button-light-blue");
        btnAddMode.getStyleClass().remove("button-light-blue");
        btnAddMode.getStyleClass().add("button-dark-blue");
        btnDeleteMode.getStyleClass().remove("button-light-blue");
        btnDeleteMode.getStyleClass().add("button-dark-blue");
        btnEditMode.getStyleClass().remove("button-light-blue");
        btnEditMode.getStyleClass().add("button-dark-blue");
    }

    public void switchToAddMode() {
        if (graphGUI.getMode() == GraphGUI.Mode.Add)
            return;
        graphGUI.setMode(GraphGUI.Mode.Add);
        btnLockMode.getStyleClass().remove("button-light-blue");
        btnLockMode.getStyleClass().add("button-dark-blue");
        btnAddMode.getStyleClass().remove("button-dark-blue");
        btnAddMode.getStyleClass().add("button-light-blue");
        btnDeleteMode.getStyleClass().remove("button-light-blue");
        btnDeleteMode.getStyleClass().add("button-dark-blue");
        btnEditMode.getStyleClass().remove("button-light-blue");
        btnEditMode.getStyleClass().add("button-dark-blue");
    }

    public void switchToDeleteMode() {
        if (graphGUI.getMode() == GraphGUI.Mode.Delete)
            return;
        graphGUI.setMode(GraphGUI.Mode.Delete);
        btnLockMode.getStyleClass().remove("button-light-blue");
        btnLockMode.getStyleClass().add("button-dark-blue");
        btnAddMode.getStyleClass().remove("button-light-blue");
        btnAddMode.getStyleClass().add("button-dark-blue");
        btnDeleteMode.getStyleClass().remove("button-dark-blue");
        btnDeleteMode.getStyleClass().add("button-light-blue");
        btnEditMode.getStyleClass().remove("button-light-blue");
        btnEditMode.getStyleClass().add("button-dark-blue");
    }

    public void switchToEditMode() {
        showError("Not implemented yet");
        return;
        // if (graphGUI.getMode() == GraphGUI.Mode.Edit)
        // return;
        // graphGUI.setMode(GraphGUI.Mode.Edit);
        // btnLockMode.getStyleClass().remove("button-light-blue");
        // btnLockMode.getStyleClass().add("button-dark-blue");
        // btnAddMode.getStyleClass().remove("button-light-blue");
        // btnAddMode.getStyleClass().add("button-dark-blue");
        // btnDeleteMode.getStyleClass().remove("button-light-blue");
        // btnDeleteMode.getStyleClass().add("button-dark-blue");
        // btnEditMode.getStyleClass().remove("button-dark-blue");
        // btnEditMode.getStyleClass().add("button-light-blue");
    }

    private Graph getDefaultGraph() {
        Graph graph = new Graph();
        graph.addNode(new Node("1"));
        graph.addNode(new Node("2"));
        graph.addNode(new Node("3"));
        graph.addNode(new Node("4"));
        graph.addNode(new Node("5"));
        graph.addNode(new Node("6"));
        graph.addNode(new Node("7"));
        graph.addEdge("1", "2");
        graph.addEdge("1", "5");
        graph.addEdge("1", "6");
        graph.addEdge("2", "5");
        graph.addEdge("2", "3");
        graph.addEdge("3", "6");
        graph.addEdge("4", "5");
        graph.addEdge("4", "6");
        graph.addEdge("4", "7");
        graph.addEdge("5", "7");
        return graph;
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
}
