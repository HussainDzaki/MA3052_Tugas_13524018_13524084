package ma3052.controller;

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
import javafx.scene.layout.VBox;
import ma3052.App;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.gui.EdgeGUI;
import ma3052.gui.GraphGUI;
import ma3052.gui.GridGraphGUI;
import ma3052.gui.animation.*;
import ma3052.graph.GraphComponent;
import ma3052.graph.GridGraph;
import ma3052.graph.IslandCounter;
import ma3052.graph.CycleDetector;

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
public class GraphVisualGUIController {
    public static GraphVisualGUIController instance = null;

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
    private TextField nodeInput;

    @FXML
    private TextField nodeValueInput;

    @FXML
    private TextField edgeStartInput;

    @FXML
    private TextField edgeEndInput;

    @FXML
    private TextField edgeWeightInput;

    @FXML
    private Button addNodeButton;

    @FXML
    private Button addEdgeButton;

    @FXML
    private Button clearButton;

    @FXML
    private TextArea nodeListTextArea;

    @FXML
    private TextArea edgeListTextArea;

    @FXML
    private Button addFromFile;

    @FXML
    private Button advancedInput;

    @FXML
    private Button btnNodeAndEdges;

    @FXML
    private Button btnSwitchToGrid;

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
    private TextField startNodeInput;

    @FXML
    private Label labelStartNode;

    @FXML
    private TextField endNodeInput;

    @FXML
    private Label labelEndNode;

    @FXML
    private Label labelAddEdge;

    @FXML
    private VBox addNodeVbox;

    @FXML
    private Label labelSpeedSlider;

    public enum ModeGUI {
        NODE_AND_EDGES_MODE, GRID_MODE;
    }

    private ModeGUI mode = ModeGUI.NODE_AND_EDGES_MODE;

    private ScheduledThreadPoolExecutor threadPoolExecutor;
    private Timer updateGraphFromListTimer;

    public void setGraph(Graph graph) {
        graphGUI.setGraph(graph);
    }

    public Graph getGraph() {
        return graphGUI.getGraph();
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

        addFromFile.setVisible(false);
        addFromFile.setManaged(false);

        advancedInput.setVisible(true);
        advancedInput.setManaged(true);

        nodeListTextArea.textProperty().addListener((observable) -> {
            if (updateGraphFromListTimer != null) {
                updateGraphFromListTimer.cancel();
                updateGraphFromListTimer.purge();
            }
            updateGraphFromListTimer = new Timer();
            updateGraphFromListTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateGraphFromList();
                }
            }, 500);
        });
        edgeListTextArea.textProperty().addListener((observable) -> {
            if (updateGraphFromListTimer != null) {
                updateGraphFromListTimer.cancel();
                updateGraphFromListTimer.purge();
            }
            updateGraphFromListTimer = new Timer();
            updateGraphFromListTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    updateGraphFromList();
                }

            }, 500);
        });

        // Log initialization
        logMessage("Graph Visualization initialized successfully");

        threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        // Stop both drawing thread
        Platform.runLater(() -> {
            // Remove focus on the first button, make it focus a non button
            appLabel.requestFocus();
            switchAlgorithm(algorithmCombo.getValue());
            updateListFromGraph();
            graphCanvas.getScene().getWindow().setOnCloseRequest(e -> {
                graphGUI.stop();
                gridGraphGUI.stop();
                threadPoolExecutor.shutdown();
                if (updateGraphFromListTimer != null) {
                    updateGraphFromListTimer.cancel();
                    updateGraphFromListTimer.purge();
                }
            });
        });
    }

    /**
     * Handle adding a new node to the graph
     */
    @FXML
    private void handleAddNode() {
        String nodeName = nodeInput.getText().strip();
        if (nodeName.isEmpty()) {
            showError("Node name cannot be empty");
            return;
        }

        try {
            if (!graphGUI.getGraph().hasNode(nodeName)) {
                Node newNode = new Node(nodeName);
                if (!nodeValueInput.getText().isBlank()) {
                    try {
                        double value = Double.parseDouble(nodeValueInput.getText());
                        newNode.setValue(value);
                        graphGUI.addNode(newNode);
                        graphGUI.getNodeGUI(newNode).setDrawValue(true);
                    } catch (Exception e) {
                        showError("Node value must be a number!");
                        return;
                    }
                } else {
                    graphGUI.addNode(newNode);
                }

                // Initialize position and velocity for new node
                double width = graphCanvas.getWidth();
                double height = graphCanvas.getHeight();
                double randomX = Math.random() * (width - 100) + 50;
                double randomY = Math.random() * (height - 100) + 50;
                graphGUI.getNodeGUI(newNode).setPosition(new Point2D(randomX, randomY));

                logMessage("Added node: " + nodeName);
            } else {
                logMessage("Node already present: " + nodeName);
            }

            nodeInput.clear();
            nodeValueInput.clear();
        } catch (Exception e) {
            showError("Error adding node: " + e.getMessage());
        }
        updateListFromGraph();
    }

    /**
     * Handle adding a new edge between nodes
     */
    @FXML
    private void handleAddEdge() {
        String startNodeName = edgeStartInput.getText().strip();
        String endNodeName = edgeEndInput.getText().strip();

        if (startNodeName.isEmpty() || endNodeName.isEmpty()) {
            showError("Both node names must be specified");
            return;
        }

        try {
            Node startNode = graphGUI.getGraph().getNode(startNodeName);
            Node endNode = graphGUI.getGraph().getNode(endNodeName);

            if (startNode == null || endNode == null) {
                showError("One or both nodes do not exist in the graph");
                return;
            }

            if (!edgeWeightInput.getText().isBlank()) {
                try {
                    double weight = Double.parseDouble(edgeWeightInput.getText());
                    graphGUI.addEdge(startNode, endNode);
                    Edge edge = graphGUI.getGraph().getEdge(startNode, endNode);
                    edge.setWeight(weight);
                    graphGUI.getEdgeGUI(edge).setDrawWeight(true);
                } catch (Exception e) {
                    showError("Node value must be a number!");
                    return;
                }
            } else {
                graphGUI.addEdge(startNode, endNode);
            }

            logMessage("Added edge: " + startNodeName + " <-> " + endNodeName);
            edgeStartInput.clear();
            edgeEndInput.clear();
            edgeWeightInput.clear();
        } catch (

        Exception e) {
            showError("Error adding edge: " + e.getMessage());
        }

        updateListFromGraph();

    }

    /**
     * Handle clearing the graph
     */
    @FXML
    private void handleClear() {
        if (isAnimating) {
            showError("Cannot clear while animation is running");
            return;
        }
        switch (mode) {
            case NODE_AND_EDGES_MODE:
                graphGUI.setGraph(new Graph());
                logMessage("═══════════════════════════════════");
                logMessage("Graph cleared");
                logMessage("═══════════════════════════════════");
                break;
            case GRID_MODE:
                GridGraph graph = gridGraphGUI.getGridGraph();
                for (int i = 0; i < graph.getRowSize(); i++) {
                    for (int j = 0; j < graph.getRowSize(); j++) {
                        graph.setNodeType(i, j, '.');
                    }
                }
                logMessage("═══════════════════════════════════");
                logMessage("Grid Graph Cleared");
                logMessage("═══════════════════════════════════");
                break;
            default:
                break;
        }
        updateListFromGraph();
    }

    @FXML
    private void handleAdvancedInput() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ma3052/advancedinput.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 700, 500);

            // Setup stage
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(advancedInput.getScene().getWindow());
            stage.setTitle("Advanced Input");
            stage.setScene(scene);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.showAndWait();
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Handle loading graph or grid from file based on current mode
     */
    @FXML
    private void handleAddFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(addFromFile.getScene().getWindow());
        if (selectedFile != null) {
            try {
                if (mode == ModeGUI.GRID_MODE) {
                    loadGridFromFile(selectedFile);
                } else {
                    loadGraphFromFile(selectedFile);
                }
            } catch (IOException e) {
                showError("Error reading file: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                showError("Invalid file format: " + e.getMessage());
            }
        }
    }

    /**
     * Parse and load graph from a text file
     * File format:
     * Lines with single word: node names
     * Lines with two words: edges (space-separated node pairs)
     */
    private void loadGraphFromFile(File file) throws IOException, IllegalArgumentException {
        if (isAnimating) {
            throw new IllegalArgumentException("Cannot load file while animation is running");
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        Set<String> nodeNames = new HashSet<>();
        List<String[]> edges = new ArrayList<>();

        String line;
        int lineNumber = 0;

        // Parse file
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            String[] tokens = line.split("\\s+");

            if (tokens.length == 1) {
                // Single token: it's a node
                nodeNames.add(tokens[0]);
                logMessage("Node parsed: " + tokens[0]);
            } else if (tokens.length == 2) {
                // Two tokens: it's an edge
                edges.add(tokens);
                logMessage("Edge parsed: " + tokens[0] + " - " + tokens[1]);
            } else {
                reader.close();
                throw new IllegalArgumentException("Line " + lineNumber +
                        " has invalid format. Expected 1 or 2 space-separated values, got " + tokens.length);
            }
        }
        reader.close();

        // Add all nodes to the graph
        Graph newGraph = new Graph();
        for (String nodeName : nodeNames) {
            if (!newGraph.hasNode(nodeName)) {
                Node newNode = new Node(nodeName);
                newGraph.addNode(newNode);
            }
        }

        // Add all edges to the graph
        for (String[] edge : edges) {
            Node startNode = newGraph.getNode(edge[0]);
            Node endNode = newGraph.getNode(edge[1]);

            if (startNode == null || endNode == null) {
                throw new IllegalArgumentException("Edge references non-existent node: " +
                        (startNode == null ? edge[0] : edge[1]));
            }

            newGraph.addEdge(startNode, endNode);
        }

        graphGUI.setGraph(newGraph);

        logMessage("Loaded " + newGraph.size() + " nodes and " + edges.size() + " edges");
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

    /**
     * Switch to Node and Edges visualization mode
     */
    @FXML
    public void switchToNodeAndEdges() {
        if (mode == ModeGUI.NODE_AND_EDGES_MODE)
            return;
        mode = ModeGUI.NODE_AND_EDGES_MODE;
        updateAlgorithmComboForMode();
        startNodeInput.setVisible(true);
        startNodeInput.setManaged(true);
        labelStartNode.setVisible(true);
        labelStartNode.setManaged(true);
        endNodeInput.setVisible(true);
        endNodeInput.setManaged(true);
        labelEndNode.setVisible(true);
        labelEndNode.setManaged(true);
        addNodeVbox.setVisible(true);
        addNodeVbox.setManaged(true);
        edgeStartInput.setVisible(true);
        edgeStartInput.setManaged(true);
        edgeEndInput.setVisible(true);
        edgeEndInput.setManaged(true);
        addEdgeButton.setVisible(true);
        addEdgeButton.setManaged(true);
        labelSpeedSlider.setVisible(true);
        labelSpeedSlider.setManaged(true);

        speedSlider.setVisible(true);
        speedSlider.setManaged(true);
        labelAddEdge.setText("Add Edge: ");

        addFromFile.setVisible(false);
        addFromFile.setManaged(false);

        advancedInput.setVisible(true);
        advancedInput.setManaged(true);

        btnNodeAndEdges.getStyleClass().add("button-light-blue");
        btnNodeAndEdges.getStyleClass().remove("button-dark-blue");
        btnSwitchToGrid.getStyleClass().add("button-dark-blue");
        btnSwitchToGrid.getStyleClass().remove("button-light-blue");

        graphGUI.setGraph(graphGUI.getGraph()); // Refresh the graph view
        graphGUI.setDrawing(true);
        gridGraphGUI.setDrawing(false);

        logMessage("═══════════════════════════════════");
        logMessage("Switched to Node and Edges view");
        logMessage("═══════════════════════════════════");
    }

    /**
     * Switch to Grid visualization mode
     */
    @FXML
    public void switchToGrid() {
        if (mode == ModeGUI.GRID_MODE)
            return;
        mode = ModeGUI.GRID_MODE;
        startNodeInput.setVisible(false);
        startNodeInput.setManaged(false);
        labelStartNode.setVisible(false);
        labelStartNode.setManaged(false);
        endNodeInput.setVisible(false);
        endNodeInput.setManaged(false);
        labelEndNode.setVisible(false);
        labelEndNode.setManaged(false);
        addNodeVbox.setVisible(false);
        addNodeVbox.setManaged(false);

        edgeStartInput.setVisible(false);
        edgeStartInput.setManaged(false);
        edgeEndInput.setVisible(false);
        edgeEndInput.setManaged(false);
        labelSpeedSlider.setVisible(false);
        labelSpeedSlider.setManaged(false);
        labelAddEdge.setText("Grid Set Up");

        addEdgeButton.setVisible(false);
        addEdgeButton.setManaged(false);

        speedSlider.setVisible(false);
        speedSlider.setManaged(false);
        updateAlgorithmComboForMode();

        addFromFile.setVisible(true);
        addFromFile.setManaged(true);

        advancedInput.setVisible(false);
        advancedInput.setManaged(false);

        btnNodeAndEdges.getStyleClass().remove("button-light-blue");
        btnNodeAndEdges.getStyleClass().add("button-dark-blue");
        btnSwitchToGrid.getStyleClass().remove("button-dark-blue");
        btnSwitchToGrid.getStyleClass().add("button-light-blue");

        graphGUI.setDrawing(false);
        gridGraphGUI.setDrawing(true);

        logMessage("═══════════════════════════════════");
        logMessage("Switched to Grid view");
        logMessage("═══════════════════════════════════");
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

    public void updateGraphFromList() {
        Graph graph = graphGUI.getGraph();
        String[] nodeInput = nodeListTextArea.getText().split("\n+");
        String[] edgeInput = edgeListTextArea.getText().split("\n+");
        HashSet<Node> nodeSet = new HashSet<>();
        HashSet<Edge> edgeSet = new HashSet<>();
        for (String line : nodeInput) {
            String[] tokens = line.strip().split("[ \t]+");
            if (tokens.length == 0)
                continue;
            if (tokens[0].isBlank())
                continue;
            if (!graph.hasNode(tokens[0])) {
                if (tokens.length >= 2) {
                    double value = 0;
                    try {
                        value = Double.parseDouble(tokens[1]);
                    } catch (Exception e) {
                        value = 0;
                    }
                    Node newNode = new Node(tokens[0], value);
                    graphGUI.addNode(newNode);
                    graphGUI.getNodeGUI(newNode).setDrawValue(true);
                } else {
                    Node newNode = new Node(tokens[0]);
                    graphGUI.addNode(newNode);
                }
            } else {
                if (tokens.length >= 2) {
                    double value = 0;
                    try {
                        value = Double.parseDouble(tokens[1]);
                    } catch (Exception e) {
                        value = 0;
                    }
                    Node node = graph.getNode(tokens[0]);
                    node.setValue(value);
                    graphGUI.getNodeGUI(node).setDrawValue(true);
                } else {
                    Node node = graph.getNode(tokens[0]);
                    node.setValue(Node.DEFAULT_VALUE);
                    graphGUI.getNodeGUI(node).setDrawValue(false);
                }
            }
            nodeSet.add(graph.getNode(tokens[0]));
        }
        for (String line : edgeInput) {
            String[] tokens = line.strip().split("[ \t]+");
            if (tokens.length <= 1)
                continue;
            if (tokens[0].isBlank() || tokens[1].isBlank()) {
                continue;
            }
            if (!graph.hasNode(tokens[0])) {
                graphGUI.addNode(new Node(tokens[0]));
                String text = nodeListTextArea.getText();
                if (text.endsWith("\n") || text.isBlank()) {
                    nodeListTextArea.setText(text + tokens[0]);
                } else {
                    nodeListTextArea.setText(text + "\n" + tokens[0]);
                }
            }
            if (!graph.hasNode(tokens[1])) {
                graphGUI.addNode(new Node(tokens[1]));
                String text = nodeListTextArea.getText();
                if (text.endsWith("\n") || text.isBlank()) {
                    nodeListTextArea.setText(text + tokens[1]);
                } else {
                    nodeListTextArea.setText(text + "\n" + tokens[1]);
                }
            }
            nodeSet.add(graph.getNode(tokens[0]));
            nodeSet.add(graph.getNode(tokens[1]));

            if (!graph.hasEdge(tokens[0], tokens[1])) {
                Node node1 = graph.getNode(tokens[0]);
                Node node2 = graph.getNode(tokens[1]);
                graphGUI.addEdge(node1, node2);
                if (tokens.length >= 3) {
                    double weight = 1;
                    try {
                        weight = Double.parseDouble(tokens[2]);
                    } catch (Exception e) {
                        weight = 1;
                    }
                    Edge edge = graph.getEdge(node1, node2);
                    edge.setWeight(weight);
                    graphGUI.getEdgeGUI(edge).setDrawWeight(true);
                }
            } else {
                Node node1 = graph.getNode(tokens[0]);
                Node node2 = graph.getNode(tokens[1]);
                if (tokens.length >= 3) {
                    double weight = 1;
                    try {
                        weight = Double.parseDouble(tokens[2]);
                    } catch (Exception e) {
                        weight = 1;
                    }
                    Edge edge = graph.getEdge(node1, node2);
                    edge.setWeight(weight);
                    graphGUI.getEdgeGUI(edge).setDrawWeight(true);
                } else {
                    Edge edge = graph.getEdge(node1, node2);
                    edge.setWeight(Edge.DEFAULT_WEIGHT);
                    graphGUI.getEdgeGUI(edge).setDrawWeight(false);
                }
            }
            edgeSet.add(graph.getEdge(tokens[0], tokens[1]));
        }
        ArrayList<Node> nodesToRemove = new ArrayList<>();
        for (Node node : graph.getNodeList()) {
            if (!nodeSet.contains(node)) {
                nodesToRemove.add(node);
            }
        }
        for (Node node : nodesToRemove) {
            graphGUI.removeNode(node);
        }
        ArrayList<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : graph.getEdgeList()) {
            if (!edgeSet.contains(edge)) {
                edgesToRemove.add(edge);
            }
        }
        for (Edge edge : edgesToRemove) {
            graphGUI.removeEdge(edge);
        }
    }

    public void updateListFromGraph() {
        String nodeText = "";
        for (Node node : graphGUI.getGraph().getNodeList()) {
            if (graphGUI.getNodeGUI(node).isDrawValue()) {
                nodeText += node.getNodeName() + " " + Double.toString(node.getValue()) + "\n";
            } else {
                nodeText += node.getNodeName() + "\n";
            }
        }
        nodeListTextArea.setText(nodeText);
        String edgeText = "";
        for (Edge edge : graphGUI.getGraph().getEdgeList()) {
            if (graphGUI.getEdgeGUI(edge).isDrawWeight()) {
                edgeText += edge.getSource().getNodeName() + " " + edge.getDestination().getNodeName() + " "
                        + Double.toString(edge.getWeight()) + "\n";
            } else {
                edgeText += edge.getSource().getNodeName() + " " + edge.getDestination().getNodeName() + "\n";
            }
        }
        edgeListTextArea.setText(edgeText);
    }

    /**
     * Parse grid input from text format
     * Format: Each row is a line with characters like . and #
     * Example:
     * .#.
     * #.#
     * ###
     */
    private GridGraph parseGridFromText(String text) throws IllegalArgumentException {
        String[] lines = text.trim().split("\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("Grid cannot be empty");
        }

        int rows = lines.length;
        int cols = lines[0].length();

        // Validate all rows have same length
        for (int i = 0; i < rows; i++) {
            if (lines[i].length() != cols) {
                throw new IllegalArgumentException(
                        "Row " + (i + 1) + " has " + lines[i].length() +
                                " columns, expected " + cols);
            }
        }

        // Create GridGraph
        GridGraph newGrid = new GridGraph(rows, cols);

        // Parse grid content
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cellType = lines[i].charAt(j);

                // Validate character
                if (cellType != '.' && cellType != '#' && cellType != ' ') {
                    throw new IllegalArgumentException(
                            "Invalid character '" + cellType + "' at row " + (i + 1) +
                                    ", col " + (j + 1) + ". Only '.', '#', and ' ' are allowed.");
                }

                if (cellType != '.') {
                    newGrid.setNodeType(i, j, cellType);
                }
            }
        }

        return newGrid;
    }

    /**
     * Load grid from file
     * Modified version of loadGraphFromFile for grid format
     */
    private void loadGridFromFile(File file) throws IOException, IllegalArgumentException {
        if (isAnimating) {
            throw new IllegalArgumentException("Cannot load file while animation is running");
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder gridContent = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("\\s+$", ""); // Remove trailing whitespace
            if (!line.isEmpty()) {
                gridContent.append(line).append("\n");
            }
        }
        reader.close();

        // Parse grid from content
        GridGraph loadedGrid = parseGridFromText(gridContent.toString());
        gridGraphGUI.setGridGraph(loadedGrid);
        mode = ModeGUI.GRID_MODE;

        logMessage("═══════════════════════════════════");
        logMessage("Grid loaded from: " + file.getName());
        logMessage("Grid size: " + loadedGrid.getRowSize() + " rows x " + loadedGrid.getColSize() + " cols");
        logMessage("═══════════════════════════════════");
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

    public void switchWeightedGraph(boolean weighted) {
        for (Edge edge : graphGUI.getGraph().getEdgeList()) {
            graphGUI.getEdgeGUI(edge).setDrawWeight(weighted);
        }
    }
}
