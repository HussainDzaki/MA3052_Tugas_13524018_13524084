package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import ma3052.core.algorithm.CycleDetector;
import ma3052.core.algorithm.IslandCounter;
import ma3052.core.graph.Node;
import ma3052.gui.animation.*;
import ma3052.gui.controller.GraphVisualizationController.ModeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.GridGraphGUI;
import ma3052.gui.graph.PointGraphGUI;
import ma3052.core.algorithm.TravellingSalesman;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class GraphAlgorithmMenuController {
    private GraphVisualizationController mainController;

    @FXML
    private ComboBox<String> algorithmCombo;

    @FXML
    private Button executeButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label labelSpeedSlider;
    @FXML
    private Slider speedSlider;

    @FXML
    private VBox startNodeVbox;
    @FXML
    private Label labelStartNode;
    @FXML
    private TextField startNodeInput;

    @FXML
    private VBox endNodeVbox;
    @FXML
    private Label labelEndNode;
    @FXML
    private TextField endNodeInput;

    @FXML
    private VBox optionVbox;
    @FXML
    private Label labelOption;
    @FXML
    private ComboBox<String> optionCombo;

    @FXML
    private TextArea logArea;

    private GraphGUI graphGUI;
    private PointGraphGUI pointGraphGUI;
    private GridGraphGUI gridGraphGUI;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

    @FXML
    private void initialize() {
        threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        // Setup algorithm options based on initial mode
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
                DijkstraAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                TravellingSalesmanAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                BipartiteMatchingAnimation.setAnimationStepTime(Math.round(500 / multiplier));
                BandwidthAnimation.setAnimationStepTime(Math.round(500 / multiplier));
            });
        }

        Platform.runLater(() -> {
            switchAlgorithm(algorithmCombo.getValue());
            executeButton.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                threadPoolExecutor.shutdownNow();
            });
        });
    }

    public void setMainController(GraphVisualizationController mainController) {
        this.mainController = mainController;
        graphGUI = mainController.getGraphGUI();
        pointGraphGUI = mainController.getPointGraphGUI();
        gridGraphGUI = mainController.getGridGraphGUI();
        updateAlgorithmComboForMode();
    }

    /**
     * Update algorithm combo options based on current mode
     */
    public void updateAlgorithmComboForMode() {
        if (algorithmCombo == null)
            return;

        algorithmCombo.getItems().clear();

        switch (mainController.getMode()) {
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
                        "Djikstra",
                        "Travelling Salesman",
                        "Maximum Matching",
                        "Time Tabling",
                        "Minimum Bandwidth");
                break;
            case GRID_MODE:
                algorithmCombo.getItems().addAll(
                        "Count Component",
                        "Biggest Component");
                break;
            case POINT_MODE:
                algorithmCombo.getItems().addAll(
                        "Travelling Salesman");
                break;
            default:
                break;
        }

        algorithmCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleExecuteAlgorithm() {
        if (mainController.isAnimating()) {
            mainController.showError("Animation already in progress");
            return;
        }

        if (!validateGraphData()) {
            return;
        }

        String selectedAlgorithm = algorithmCombo.getValue();

        // Start message
        logMessage("═══════════════════════════════════");
        logMessage("Starting " + selectedAlgorithm);
        logMessage("═══════════════════════════════════");

        // Run animation in background thread
        threadPoolExecutor.schedule(() -> executeAlgorithm(selectedAlgorithm), 0,
                TimeUnit.MILLISECONDS);
    }

    @FXML
    private void handleResetAlgorithm() {
        if (mainController.isAnimating()) {
            mainController.showError("Animation already in progress");
            return;
        }
        graphGUI.resetColors();
        pointGraphGUI.resetEdges();
        pointGraphGUI.resetColors();
    }

    /**
     * Validate if graph data is available
     */
    private boolean validateGraphData() {
        boolean hasNodeEdgeGraph = graphGUI.getGraph() != null && graphGUI.getGraph().size() > 0;
        boolean hasGridGraph = gridGraphGUI.getGridGraph() != null;

        if ((mainController.getMode() == ModeGUI.NODE_AND_EDGES_MODE && !hasNodeEdgeGraph) ||
                (mainController.getMode() == ModeGUI.GRID_MODE && !hasGridGraph)) {
            mainController.showError("Graph is empty. Add nodes first.");
            return false;
        }
        return true;
    }

    /**
     * Validate starting node for NODE_AND_EDGES_MODE
     */
    private boolean validateStartingNode(String startNodeName) {
        if (startNodeName.isEmpty()) {
            mainController.showError("Please specify a starting node");
            return false;
        }
        if (!graphGUI.getGraph().hasNode(startNodeName)) {
            mainController.showError("There's no node with the name: " + startNodeName);
            return false;
        }
        return true;
    }

    /**
     * Validate ending node for NODE_AND_EDGES_MODE
     */
    private boolean validateEndNode(String endNodeName) {
        if (endNodeName.isEmpty()) {
            mainController.showError("Please specify an end node");
            return false;
        }
        if (!graphGUI.getGraph().hasNode(endNodeName)) {
            mainController.showError("There's no node with the name: " + endNodeName);
            return false;
        }
        return true;
    }

    /**
     * Execute the selected algorithm based on mode and algorithm type
     */
    private void executeAlgorithm(String selectedAlgorithm) {
        try {
            mainController.setAnimating(true);
            switch (mainController.getMode()) {
                case NODE_AND_EDGES_MODE:
                    graphGUI.resetColors();
                    executeNodeAndEdgesAlgorithm(selectedAlgorithm);
                    break;
                case GRID_MODE:
                    executeGridAlgorithm(selectedAlgorithm);
                    break;
                case POINT_MODE:
                    pointGraphGUI.resetEdges();
                    pointGraphGUI.resetColors();
                    executePointAlgorithm(selectedAlgorithm);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Platform.runLater(() -> mainController.showError("Algorithm execution error: " + e.getMessage()));
            e.printStackTrace();
        } finally {
            mainController.setAnimating(false);
        }
    }

    /**
     * Execute algorithms for Node and Edges mode
     */
    private void executeNodeAndEdgesAlgorithm(String algorithm) {
        String startNodeName = startNodeInput.getText().trim();
        String endNodeName = endNodeInput.getText().trim();
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
                break;
            case "Travelling Salesman":
                switch (optionCombo.getValue()) {
                    case "Random Starting Node":
                        TravellingSalesmanAnimation.animate(graphGUI);
                        break;
                    case "Best of All Starting Node":
                        TravellingSalesmanAnimation.animateBest(graphGUI);
                        break;
                    default:
                        TravellingSalesmanAnimation.animate(graphGUI);
                        break;
                }
                break;
            case "Maximum Matching":
                BipartiteMatchingAnimation.animateHopCroftKarp(graphGUI);
                break;
            case "Time Tabling":
                BipartiteMatchingAnimation.animateHopCroftKarpTimeTabling(graphGUI);
                break;
            case "Minimum Bandwidth":
                BandwidthAnimation.animate(graphGUI);
                break;
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

    private void executePointAlgorithm(String algorithm) {
        switch (algorithm) {
            case "Travelling Salesman":
                switch (optionCombo.getValue()) {
                    case "Random Starting Node":
                        TravellingSalesmanAnimation.animate(mainController.getPointGraphGUI());
                        break;
                    case "Best of All Starting Node":
                        TravellingSalesmanAnimation.animateBest(mainController.getPointGraphGUI());
                        break;
                    default:
                        TravellingSalesmanAnimation.animate(mainController.getPointGraphGUI());
                        break;
                }
                break;
        }
    }

    public void switchAlgorithm(String algorithm) {
        startNodeVbox.setVisible(false);
        startNodeVbox.setManaged(false);
        endNodeVbox.setVisible(false);
        endNodeVbox.setManaged(false);
        optionVbox.setVisible(false);
        optionVbox.setManaged(false);

        switch (mainController.getMode()) {
            case NODE_AND_EDGES_MODE:
                switch (algorithm) {
                    case "DFS Traversal":
                        startNodeVbox.setVisible(true);
                        startNodeVbox.setManaged(true);
                        break;
                    case "BFS Traversal":
                        startNodeVbox.setVisible(true);
                        startNodeVbox.setManaged(true);
                        break;
                    case "DFS Path Search":
                        startNodeVbox.setVisible(true);
                        startNodeVbox.setManaged(true);
                        endNodeVbox.setVisible(true);
                        endNodeVbox.setManaged(true);
                        break;
                    case "BFS Path Search":
                        startNodeVbox.setVisible(true);
                        startNodeVbox.setManaged(true);
                        endNodeVbox.setVisible(true);
                        endNodeVbox.setManaged(true);
                        break;
                    case "Djikstra":
                        startNodeVbox.setVisible(true);
                        startNodeVbox.setManaged(true);
                        endNodeVbox.setVisible(true);
                        endNodeVbox.setManaged(true);
                        break;
                    case "Travelling Salesman":
                        optionVbox.setVisible(true);
                        optionVbox.setManaged(true);

                        optionCombo.getItems().clear();
                        optionCombo.getItems().addAll("Random Starting Node", "Best of All Starting Node");
                        optionCombo.setValue(optionCombo.getItems().getFirst());

                        break;
                }
                break;

            case GRID_MODE:
                break;

            case POINT_MODE:
                switch (algorithm) {
                    case "Travelling Salesman":
                        optionVbox.setVisible(true);
                        optionVbox.setManaged(true);

                        optionCombo.getItems().clear();
                        optionCombo.getItems().addAll("Random Starting Node", "Best of All Starting Node");
                        optionCombo.setValue(optionCombo.getItems().getFirst());
                }
            default:
                break;
        }
    }

    /**
     * Add a message to the log area
     */
    public void logMessage(String message) {
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(message + "\n"));
        }
    }
}
