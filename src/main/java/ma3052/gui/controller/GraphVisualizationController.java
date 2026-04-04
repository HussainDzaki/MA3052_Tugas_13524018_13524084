package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ma3052.App;
import ma3052.core.algorithm.CycleDetector;
import ma3052.core.algorithm.GraphComponent;
import ma3052.core.algorithm.IslandCounter;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.GridGraph;
import ma3052.core.graph.Node;
import ma3052.gui.animation.*;
import ma3052.gui.graph.EdgeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.GridGraphGUI;

import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private TextArea logArea;

    @FXML
    private Button btnLockMode;
    @FXML
    private Button btnAddMode;
    @FXML
    private Button btnDeleteMode;
    @FXML
    private Button btnEditMode;

    @FXML
    private ComboBox<String> algorithmCombo;

    @FXML
    private Button executeButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private Label labelStartNode;

    @FXML
    private TextField startNodeInput;

    @FXML
    private Label labelEndNode;

    @FXML
    private TextField endNodeInput;

    @FXML
    private Label labelSpeedSlider;

    @FXML
    private GraphInputMenuController graphInputMenuController;

    public enum ModeGUI {
        NODE_AND_EDGES_MODE, GRID_MODE;
    }

    private ModeGUI mode = ModeGUI.NODE_AND_EDGES_MODE;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

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

        // Setup algorithm options based on initial mode
        updateAlgorithmComboForMode();
        algorithmCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switchAlgorithm(newVal);
            }
        });

        // Setup speed slider
        if (speedSlider != null) {
            speedSlider.setSnapToTicks(true);
            speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                double multiplier = Math.pow(2, Math.round(newValue.doubleValue()));
                speedSlider.setValue(Math.round(newValue.doubleValue()));
                labelSpeedSlider.setText("Speed: (x" + multiplier + ")");
                TraversalAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                PathAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                ConnectivityAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                ComponentAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                KruskalAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                PrimsAnimation.setAnimationStepTime(Math.round(500 / multiplier));
            });
        }

        graphInputMenuController.setMainController(this);

        // Log initialization
        logMessage("Graph Visualization initialized successfully");

        threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        Platform.runLater(() -> {
            // Remove focus on the first button, make it focus a non button
            appLabel.requestFocus();
            switchAlgorithm(algorithmCombo.getValue());
            graphInputMenuController.updateListFromGraph();

            graphCanvas.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                // Stop both drawing thread
                graphGUI.stop();
                gridGraphGUI.stop();
                threadPoolExecutor.shutdown();
            });
        });
    }

    /**
     * Add a message to the log area
     */
    public void logMessage(String message) {
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(message + "\n"));
        }
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

    /**
     * Update algorithm combo options based on current mode
     */
    private void updateAlgorithmComboForMode() {
        if (algorithmCombo == null)
            return;

        algorithmCombo.getItems().clear();

        switch (mode) {
            case NODE_AND_EDGES_MODE:
                algorithmCombo.getItems().addAll(
                        "DFS Traversal",
                        "BFS Traversal",
                        "DFS Path Search",
                        "BFS Path Search",
                        "Connectivity",
                        "Component",
                        "Bipartite Checker",
                        "Find Diameter",
                        "Have Cycle Checker",
                        "Find Smallest Cycle",
                        "Kruskal's Algorithm",
                        "Prim's Algorithm",
                        "Djikstra");
                break;
            case GRID_MODE:
                algorithmCombo.getItems().addAll(
                        "Count Component",
                        "Biggest Component");
                break;
            default:
                break;
        }

        algorithmCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleExecuteAlgorithm() {
        if (isAnimating) {
            showError("Animation already in progress");
            return;
        }

        if (!validateGraphData()) {
            return;
        }

        String selectedAlgorithm = algorithmCombo.getValue();
        String startNodeName = null;
        String endNodeName = null;

        // Validate mode-specific requirements
        if (mode == ModeGUI.NODE_AND_EDGES_MODE) {
            startNodeName = startNodeInput.getText().trim();
            endNodeName = endNodeInput.getText().trim();
            logMessage("═══════════════════════════════════");
            logMessage("Starting " + selectedAlgorithm + " from node: " + startNodeName);
            logMessage("═══════════════════════════════════");
        }

        // Run animation in background thread
        final String finalStartNode = startNodeName;
        final String finalEndNode = endNodeName;
        threadPoolExecutor.schedule(() -> executeAlgorithm(selectedAlgorithm, finalStartNode, finalEndNode), 0,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Validate if graph data is available
     */
    private boolean validateGraphData() {
        boolean hasNodeEdgeGraph = graphGUI.getGraph() != null && graphGUI.getGraph().size() > 0;
        boolean hasGridGraph = gridGraphGUI.getGridGraph() != null;

        if ((mode == ModeGUI.NODE_AND_EDGES_MODE && !hasNodeEdgeGraph) ||
                (mode == ModeGUI.GRID_MODE && !hasGridGraph)) {
            showError("Graph is empty. Add nodes first.");
            return false;
        }
        return true;
    }

    /**
     * Validate starting node for NODE_AND_EDGES_MODE
     */
    private boolean validateStartingNode(String startNodeName) {
        if (startNodeName.isEmpty()) {
            showError("Please specify a starting node");
            return false;
        }
        if (!graphGUI.getGraph().hasNode(startNodeName)) {
            showError("There's no node with the name: " + startNodeName);
            return false;
        }
        return true;
    }

    /**
     * Validate ending node for NODE_AND_EDGES_MODE
     */
    private boolean validateEndNode(String endNodeName) {
        if (endNodeName.isEmpty()) {
            showError("Please specify an end node");
            return false;
        }
        if (!graphGUI.getGraph().hasNode(endNodeName)) {
            showError("There's no node with the name: " + endNodeName);
            return false;
        }
        return true;
    }

    /**
     * Execute the selected algorithm based on mode and algorithm type
     */
    private void executeAlgorithm(String selectedAlgorithm, String startNodeName, String endNodeName) {
        try {
            isAnimating = true;
            switch (mode) {
                case NODE_AND_EDGES_MODE:
                    graphGUI.resetColors();
                    executeNodeAndEdgesAlgorithm(selectedAlgorithm, startNodeName, endNodeName);
                    break;
                case GRID_MODE:
                    executeGridAlgorithm(selectedAlgorithm);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Platform.runLater(() -> showError("Algorithm execution error: " + e.getMessage()));
            e.printStackTrace();
        } finally {
            isAnimating = false;
        }
    }

    /**
     * Execute algorithms for Node and Edges mode
     */
    private void executeNodeAndEdgesAlgorithm(String algorithm, String startNodeName, String endNodeName) {
        switch (algorithm) {
            case "DFS Traversal":
                if (!validateStartingNode(startNodeName)) {
                    return;
                }
                TraversalAnimation.animateDFS(graphGUI, startNodeName);
                break;
            case "BFS Traversal":
                if (!validateStartingNode(startNodeName)) {
                    return;
                }
                TraversalAnimation.animateBFS(graphGUI, startNodeName);
                break;
            case "DFS Path Search":
                if (!validateStartingNode(startNodeName) && !validateEndNode(endNodeName)) {
                    return;
                }
                PathAnimation.animateDFS(graphGUI, startNodeName, endNodeName);
                break;
            case "BFS Path Search":
                if (!validateStartingNode(startNodeName) && !validateEndNode(endNodeName)) {
                    return;
                }
                PathAnimation.animateBFS(graphGUI, startNodeName, endNodeName);
                break;
            case "Connectivity":
                ConnectivityAnimation.animate(graphGUI);
                break;
            case "Component":
                ComponentAnimation.animate(graphGUI);
                break;
            case "Bipartite Checker":
                logMessage("The Graph is "
                        + (CycleDetector.isBipartite(graphGUI.getGraph()) ? " Bipartite" : "NOT Bipartite"));
                break;
            case "Find Diameter":
                List<Node> res = CycleDetector.getDiameterPath(graphGUI.getGraph());
                logMessage("The Graph have diameter " + Integer.toString(res.size() - 1));
                logMessage("Have the diameter path : " + CycleDetector.getResultPathString(res));
                break;
            case "Have Cycle Checker":
                List<Node> cyclePath;
                if (graphGUI.getGraph().isDirected()) {
                    cyclePath = CycleDetector.getDirectedCyclePath(graphGUI.getGraph());

                } else {
                    cyclePath = CycleDetector.getUndirectedCyclePath(graphGUI.getGraph());
                }
                logMessage("The graph have the cycle path : " + CycleDetector.getResultPathString(cyclePath));
                break;
            case "Find Smallest Cycle":
                List<Node> girthPath;
                girthPath = CycleDetector.getGirthPath(graphGUI.getGraph());
                logMessage("The Graph have cycle size: " + Integer.toString(girthPath.size() - 1));
                logMessage("Have the cycle path: " + CycleDetector.getResultPathString(girthPath));
                break;
            case "Kruskal's Algorithm":
                KruskalAnimation.animate(graphGUI);
                break;
            case "Prim's Algorithm":
                PrimsAnimation.animate(graphGUI);
                break;
            case "Djikstra":
                if (!validateStartingNode(startNodeName) && !validateEndNode(endNodeName)) {
                    return;
                }
                DijkstraAnimation.animate(graphGUI, startNodeName, endNodeName);
            default:
                break;
        }
    }

    /**
     * Execute algorithms for Grid mode
     */
    private void executeGridAlgorithm(String algorithm) {
        switch (algorithm) {
            case "Count Component":
                IslandAnimation.animate(gridGraphGUI, '#', () -> {
                    logMessage(String.format("Total component is %d", gridGraphGUI.getTotalIsland()));
                    logMessage("═══════════════════════════════════");
                    logMessage("Island animation completed!");
                    logMessage("═══════════════════════════════════");
                });
                break;
            case "Biggest Component":
                IslandAnimation.animateLargestComponent(gridGraphGUI, '#', () -> {
                    int biggestIslandSize = IslandCounter.getBiggestIsland(gridGraphGUI.getGridGraph(), '#');
                    logMessage(String.format("Biggest island size is %d", biggestIslandSize));
                    logMessage("═══════════════════════════════════");
                    logMessage("Largest component animation completed!");
                    logMessage("═══════════════════════════════════");
                });
                break;
            default:
                break;
        }
    }

    public void switchAlgorithm(String algorithm) {
        switch (mode) {
            case NODE_AND_EDGES_MODE:
                switch (algorithm) {
                    case "DFS Traversal":
                        startNodeInput.setVisible(true);
                        startNodeInput.setManaged(true);
                        labelStartNode.setVisible(true);
                        labelStartNode.setManaged(true);
                        endNodeInput.setVisible(false);
                        endNodeInput.setManaged(false);
                        labelEndNode.setVisible(false);
                        labelEndNode.setManaged(false);
                        break;
                    case "BFS Traversal":
                        startNodeInput.setVisible(true);
                        startNodeInput.setManaged(true);
                        labelStartNode.setVisible(true);
                        labelStartNode.setManaged(true);
                        endNodeInput.setVisible(false);
                        endNodeInput.setManaged(false);
                        labelEndNode.setVisible(false);
                        labelEndNode.setManaged(false);
                        break;
                    case "DFS Path Search":
                        startNodeInput.setVisible(true);
                        startNodeInput.setManaged(true);
                        labelStartNode.setVisible(true);
                        labelStartNode.setManaged(true);
                        endNodeInput.setVisible(true);
                        endNodeInput.setManaged(true);
                        labelEndNode.setVisible(true);
                        labelEndNode.setManaged(true);
                        break;
                    case "BFS Path Search":
                        startNodeInput.setVisible(true);
                        startNodeInput.setManaged(true);
                        labelStartNode.setVisible(true);
                        labelStartNode.setManaged(true);
                        endNodeInput.setVisible(true);
                        endNodeInput.setManaged(true);
                        labelEndNode.setVisible(true);
                        labelEndNode.setManaged(true);
                        break;
                    case "Djikstra":
                        startNodeInput.setVisible(true);
                        startNodeInput.setManaged(true);
                        labelStartNode.setVisible(true);
                        labelStartNode.setManaged(true);
                        endNodeInput.setVisible(true);
                        endNodeInput.setManaged(true);
                        labelEndNode.setVisible(true);
                        labelEndNode.setManaged(true);
                        break;
                    default:
                        startNodeInput.setVisible(false);
                        startNodeInput.setManaged(false);
                        labelStartNode.setVisible(false);
                        labelStartNode.setManaged(false);
                        endNodeInput.setVisible(false);
                        endNodeInput.setManaged(false);
                        labelEndNode.setVisible(false);
                        labelEndNode.setManaged(false);
                        break;
                }
                break;

            case GRID_MODE:
                break;

            default:
                break;
        }
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
