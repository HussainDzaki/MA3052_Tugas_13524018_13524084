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

public class GraphAlgorithmMenuController {
    private GraphVisualizationController mainController;

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
            });
        }

        Platform.runLater(() -> {
            switchAlgorithm(algorithmCombo.getValue());
            executeButton.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                threadPoolExecutor.shutdown();
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
                        "Time Labeling"
                    );
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
        String startNodeName = null;
        String endNodeName = null;

        // Validate mode-specific requirements
        if (mainController.getMode() == ModeGUI.NODE_AND_EDGES_MODE) {
            startNodeName = startNodeInput.getText().trim();
            endNodeName = endNodeInput.getText().trim();
            logMessage("═══════════════════════════════════");
            logMessage("Starting " + selectedAlgorithm + " from node: " + startNodeName);
            logMessage("═══════════════════════════════════");
        }
        if (mainController.getMode() == ModeGUI.POINT_MODE) {
            logMessage("═══════════════════════════════════");
            logMessage("Starting " + selectedAlgorithm);
            logMessage("═══════════════════════════════════");
        }

        // Run animation in background thread
        final String finalStartNode = startNodeName;
        final String finalEndNode = endNodeName;
        threadPoolExecutor.schedule(() -> executeAlgorithm(selectedAlgorithm, finalStartNode, finalEndNode), 0,
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
    private void executeAlgorithm(String selectedAlgorithm, String startNodeName, String endNodeName) {
        try {
            mainController.setAnimating(true);
            switch (mainController.getMode()) {
                case NODE_AND_EDGES_MODE:
                    graphGUI.resetColors();
                    executeNodeAndEdgesAlgorithm(selectedAlgorithm, startNodeName, endNodeName);
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
            case "Travelling Salesman":
                TravellingSalesmanAnimation.animate(graphGUI);
                break;
            case "Maximum Matching":
                BipartiteMatchingAnimation.animateHopCroftKarp(graphGUI);
                break;
            case "Time Labeling":
                BipartiteMatchingAnimation.animateHopCroftKarpTimeLabeling(graphGUI);
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
                TravellingSalesmanAnimation.animate(mainController.getPointGraphGUI());
                break;

            default:
                break;
        }
    }

    public void switchAlgorithm(String algorithm) {
        switch (mainController.getMode()) {
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

    /**
     * Add a message to the log area
     */
    public void logMessage(String message) {
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(message + "\n"));
        }
    }
}
