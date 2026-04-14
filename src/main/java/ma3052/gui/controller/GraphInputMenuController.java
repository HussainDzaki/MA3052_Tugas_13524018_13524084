package ma3052.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import ma3052.App;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.GridGraph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualizationController.ModeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.GridGraphGUI;

import java.io.*;
import java.util.*;

public class GraphInputMenuController {
    private GraphVisualizationController mainController;

    // Graph type
    @FXML
    private Button btnNodeAndEdges;
    @FXML
    private Button btnSwitchToGrid;

    // Node input
    @FXML
    private VBox addNodeVbox;
    @FXML
    private Label labelAddNode;
    @FXML
    private TextField nodeInput;
    @FXML
    private TextField nodeValueInput;
    @FXML
    private Button addNodeButton;

    // Edge input
    @FXML
    private VBox addEdgeVbox;
    @FXML
    private Label labelAddEdge;
    @FXML
    private TextField edgeStartInput;
    @FXML
    private TextField edgeEndInput;
    @FXML
    private TextField edgeWeightInput;
    @FXML
    private Button addEdgeButton;

    // Buttons
    @FXML
    private Button clearButton;

    @FXML
    private Button addFromFile;

    @FXML
    private Button advancedInput;

    // Node and edge list
    @FXML
    private HBox nodeEdgeListHbox;
    @FXML
    private TextArea nodeListTextArea;
    @FXML
    private TextArea edgeListTextArea;

    // Config
    @FXML
    private CheckBox directedCheckBox;

    // Graph
    private GraphGUI graphGUI;
    private GridGraphGUI gridGraphGUI;
    private Timer updateGraphFromListTimer;

    @FXML
    private void initialize() {
        addFromFile.setVisible(false);
        addFromFile.setManaged(false);

        advancedInput.setVisible(true);
        advancedInput.setManaged(true);

        nodeInput.setOnAction((event) -> {
            nodeValueInput.requestFocus();
        });
        nodeValueInput.setOnAction((event) -> {
            nodeInput.requestFocus();
            handleAddNode();
        });

        edgeStartInput.setOnAction((event) -> {
            edgeEndInput.requestFocus();
        });
        edgeEndInput.setOnAction((event) -> {
            edgeWeightInput.requestFocus();
        });
        edgeWeightInput.setOnAction((event) -> {
            edgeStartInput.requestFocus();
            handleAddEdge();
        });

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

        directedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            graphGUI.getGraph().setDirected(newValue);
        });

        Platform.runLater(() -> {
            nodeInput.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                // Stop timer
                if (updateGraphFromListTimer != null) {
                    updateGraphFromListTimer.cancel();
                    updateGraphFromListTimer.purge();
                }
            });
        });
    }

    public void setMainController(GraphVisualizationController mainController) {
        this.mainController = mainController;
        graphGUI = mainController.getGraphGUI();
        gridGraphGUI = mainController.getGridGraphGUI();
    }

    /**
     * Handle adding a new node to the graph
     */
    @FXML
    private void handleAddNode() {
        String nodeName = nodeInput.getText().strip();
        if (nodeName.isEmpty()) {
            mainController.showError("Node name cannot be empty");
            return;
        }

        Canvas graphCanvas = graphGUI.getCanvas();

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
                        mainController.showError("Node value must be a number!");
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

                mainController.logMessage("Added node: " + nodeName);
            } else {
                mainController.logMessage("Node already present: " + nodeName);
            }

            nodeInput.clear();
            nodeValueInput.clear();
        } catch (Exception e) {
            mainController.showError("Error adding node: " + e.getMessage());
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
            mainController.showError("Both node names must be specified");
            return;
        }

        try {
            Node startNode = graphGUI.getGraph().getNode(startNodeName);
            Node endNode = graphGUI.getGraph().getNode(endNodeName);

            if (startNode == null || endNode == null) {
                mainController.showError("One or both nodes do not exist in the graph");
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
                    mainController.showError("Node value must be a number!");
                    return;
                }
            } else {
                graphGUI.addEdge(startNode, endNode);
            }

            mainController.logMessage("Added edge: " + startNodeName + " <-> " + endNodeName);
            edgeStartInput.clear();
            edgeEndInput.clear();
            edgeWeightInput.clear();
        } catch (Exception e) {
            mainController.showError("Error adding edge: " + e.getMessage());
        }

        updateListFromGraph();

    }

    /**
     * Handle clearing the graph
     */
    @FXML
    private void handleClear() {
        if (mainController.isAnimating()) {
            mainController.showError("Cannot clear while animation is running");
            return;
        }
        switch (mainController.getMode()) {
            case NODE_AND_EDGES_MODE:
                graphGUI.setGraph(new Graph());
                mainController.logMessage("═══════════════════════════════════");
                mainController.logMessage("Graph cleared");
                mainController.logMessage("═══════════════════════════════════");
                break;
            case GRID_MODE:
                GridGraph graph = gridGraphGUI.getGridGraph();
                for (int i = 0; i < graph.getRowSize(); i++) {
                    for (int j = 0; j < graph.getRowSize(); j++) {
                        graph.setNodeType(i, j, '.');
                    }
                }
                mainController.logMessage("═══════════════════════════════════");
                mainController.logMessage("Grid Graph Cleared");
                mainController.logMessage("═══════════════════════════════════");
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
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/AdvancedInput.fxml"));
            Parent root = loader.load();
            ((AdvancedInputController) loader.<AdvancedInputController>getController())
                    .setMainController(mainController);
            

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
            mainController.showError(e.getMessage());
        }
    }

    /**
     * Handle loading graph or grid from file based on current mode
     */
    @FXML
    private void handleAddFromFile() {
        if (mainController.isAnimating()) {
            mainController.showError("Cannot load file while animation is running");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(addFromFile.getScene().getWindow());
        if (selectedFile != null) {
            try {
                if (mainController.getMode() == ModeGUI.GRID_MODE) {
                    loadGridFromFile(selectedFile);
                } else {
                    loadGraphFromFile(selectedFile);
                }
            } catch (IOException e) {
                mainController.showError("Error reading file: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                mainController.showError("Invalid file format: " + e.getMessage());
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
                mainController.logMessage("Node parsed: " + tokens[0]);
            } else if (tokens.length == 2) {
                // Two tokens: it's an edge
                edges.add(tokens);
                mainController.logMessage("Edge parsed: " + tokens[0] + " - " + tokens[1]);
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

        mainController.logMessage("Loaded " + newGraph.size() + " nodes and " + edges.size() + " edges");
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
        mainController.setMode(ModeGUI.GRID_MODE);

        mainController.logMessage("═══════════════════════════════════");
        mainController.logMessage("Grid loaded from: " + file.getName());
        mainController
                .logMessage("Grid size: " + loadedGrid.getRowSize() + " rows x " + loadedGrid.getColSize() + " cols");
        mainController.logMessage("═══════════════════════════════════");
    }

    public void updateGraphFromList() {
        if (mainController.isAnimating()) {
            return;
        }

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
        if (mainController.isAnimating()) {
            return;
        }
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
     * Switch to Node and Edges visualization mode
     */
    @FXML
    private void switchToNodeAndEdges() {
        if (mainController.getMode() == ModeGUI.NODE_AND_EDGES_MODE)
            return;
        mainController.setMode(ModeGUI.NODE_AND_EDGES_MODE);

        addNodeVbox.setVisible(true);
        addNodeVbox.setManaged(true);

        addEdgeVbox.setVisible(true);
        addEdgeVbox.setManaged(true);

        nodeEdgeListHbox.setVisible(true);
        nodeEdgeListHbox.setManaged(true);

        addFromFile.setVisible(false);
        addFromFile.setManaged(false);

        advancedInput.setVisible(true);
        advancedInput.setManaged(true);

        // updateAlgorithmComboForMode();

        btnNodeAndEdges.getStyleClass().add("button-light-blue");
        btnNodeAndEdges.getStyleClass().remove("button-dark-blue");
        btnSwitchToGrid.getStyleClass().add("button-dark-blue");
        btnSwitchToGrid.getStyleClass().remove("button-light-blue");

        graphGUI.setGraph(graphGUI.getGraph()); // Refresh the graph view
        graphGUI.setDrawing(true);
        gridGraphGUI.setDrawing(false);

        mainController.logMessage("═══════════════════════════════════");
        mainController.logMessage("Switched to Node and Edges view");
        mainController.logMessage("═══════════════════════════════════");
    }

    /**
     * Switch to Grid visualization mode
     */
    @FXML
    private void switchToGrid() {
        if (mainController.getMode() == ModeGUI.GRID_MODE)
            return;
        mainController.setMode(ModeGUI.GRID_MODE);
        addNodeVbox.setVisible(false);
        addNodeVbox.setManaged(false);

        addEdgeVbox.setVisible(false);
        addEdgeVbox.setManaged(false);

        nodeEdgeListHbox.setVisible(false);
        nodeEdgeListHbox.setManaged(false);

        // updateAlgorithmComboForMode();

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

        mainController.logMessage("═══════════════════════════════════");
        mainController.logMessage("Switched to Grid view");
        mainController.logMessage("═══════════════════════════════════");
    }
}
