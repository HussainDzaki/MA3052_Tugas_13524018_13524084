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
import javafx.stage.Stage;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.gui.GraphGUI;
import ma3052.graph.GraphComponent;

import java.io.*;
import java.util.*;

/**
 * Controller for Graph Visualization GUI
 * Manages the visualization and interaction with graph data structure
 * with animated DFS/BFS algorithm visualization and force-directed layout
 */
public class GraphVisualGUIController {
    public static GraphVisualGUIController instance = null;

    // Graph data structure
    private GraphGUI graphGUI;
    private volatile boolean isAnimating = false;

    // FXML UI Components
    @FXML
    private Canvas graphCanvas;

    @FXML
    private TextArea logArea;

    @FXML
    private TextField nodeInput;

    @FXML
    private TextField edgeStartInput;

    @FXML
    private TextField edgeEndInput;

    @FXML
    private Button addNodeButton;

    @FXML
    private Button addEdgeButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button addFromFile;

    @FXML
    private Button advancedInput;

    @FXML
    private ComboBox<String> algorithmCombo;

    @FXML
    private Button executeButton;

    @FXML
    private Slider speedSlider;

    @FXML
    private TextField startNodeInput;

    public void setGraph(Graph graph) {
        graphGUI.setGraph(graph);
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

        // Setup algorithm options
        if (algorithmCombo != null) {
            algorithmCombo.getItems().addAll(
                    "DFS Traversal",
                    "BFS Traversal",
                    "Connectivity"

            );
            algorithmCombo.getSelectionModel().selectFirst();
        }

        // Setup speed slider
        if (speedSlider != null) {
            speedSlider.setMin(100);
            speedSlider.setMax(2000);
            speedSlider.setValue(500);
            speedSlider.setBlockIncrement(100);
        }

        // Setup event handlers
        setupEventHandlers();

        // Setup canvas
        if (graphCanvas != null) {
            graphCanvas.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        }

        // Log initialization
        logMessage("Graph Visualization initialized successfully");
    }

    /**
     * Setup event handlers for buttons
     */
    private void setupEventHandlers() {
        if (addNodeButton != null) {
            addNodeButton.setOnAction(event -> handleAddNode());
        }

        if (addEdgeButton != null) {
            addEdgeButton.setOnAction(event -> handleAddEdge());
        }

        if (clearButton != null) {
            clearButton.setOnAction(event -> handleClear());
        }

        if (addFromFile != null) {
            addFromFile.setOnAction(event -> handleAddFromFile());
        }

        if (advancedInput != null) {
            advancedInput.setOnAction(event -> handleAdvancedInput());
        }
        // if (executeButton != null) {
        // executeButton.setOnAction(event -> handleExecuteAlgorithm());
        // }
    }

    /**
     * Handle adding a new node to the graph
     */
    @FXML
    private void handleAddNode() {
        String nodeName = nodeInput.getText().trim();
        if (nodeName.isEmpty()) {
            showError("Node name cannot be empty");
            return;
        }

        try {
            if (!graphGUI.getGraph().hasNode(nodeName)) {
                Node newNode = new Node(nodeName);
                graphGUI.addNode(newNode);

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
        } catch (Exception e) {
            showError("Error adding node: " + e.getMessage());
        }
    }

    /**
     * Handle adding a new edge between nodes
     */
    @FXML
    private void handleAddEdge() {
        String startNodeName = edgeStartInput.getText().trim();
        String endNodeName = edgeEndInput.getText().trim();

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

            graphGUI.addEdge(startNode, endNode);
            logMessage("Added edge: " + startNodeName + " <-> " + endNodeName);
            edgeStartInput.clear();
            edgeEndInput.clear();
        } catch (Exception e) {
            showError("Error adding edge: " + e.getMessage());
        }
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

        graphGUI.setGraph(new Graph());
        logMessage("═══════════════════════════════════");
        logMessage("Graph cleared");
        logMessage("═══════════════════════════════════");
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
            stage.setTitle("Advanced Input");
            stage.setScene(scene);
            stage.setWidth(700);
            stage.setHeight(500);
            stage.showAndWait();
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Handle loading graph from file
     */
    @FXML
    private void handleAddFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Graph from File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(addFromFile.getScene().getWindow());
        if (selectedFile != null) {
            try {
                loadGraphFromFile(selectedFile);
                logMessage("═══════════════════════════════════");
                logMessage("Graph loaded from: " + selectedFile.getName());
                logMessage("═══════════════════════════════════");
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
    private void logMessage(String message) {
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(message + "\n"));
        }
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logMessage("[ERROR] " + message);
    }
}
