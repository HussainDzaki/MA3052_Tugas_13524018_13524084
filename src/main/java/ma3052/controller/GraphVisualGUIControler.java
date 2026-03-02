package ma3052.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.graph.GraphComponent;

import java.io.*;
import java.util.*;

/**
 * Controller for Graph Visualization GUI
 * Manages the visualization and interaction with graph data structure
 * with animated DFS/BFS algorithm visualization and force-directed layout
 */
public class GraphVisualGUIControler {
    
    // Graph data structure
    private Graph graph;
    private Map<Node, javafx.geometry.Point2D> nodePositions;
    private Map<Node, javafx.geometry.Point2D> nodeVelocities;
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
    private ComboBox<String> algorithmCombo;
    
    @FXML
    private Button executeButton;
    
    @FXML
    private Slider speedSlider;
    
    @FXML
    private TextField startNodeInput;
    
    // Animation parameters
    private static final int NODE_RADIUS = 20;
    private static final int MARGIN = 50;
    
    // Force-directed layout parameters
    private static final double COULOMB_CONSTANT = 5000.0; // Repulsive force strength
    private static final double DAMPENING = 0.8; // Friction/dampening
    private static final double MIN_DISTANCE = 80.0; // Minimum distance between nodes
    private static final double MAX_VELOCITY = 3.0; // Maximum node velocity
    private static final int ITERATIONS = 50; // Iterations for layout calculation
    
    /**
     * Initialize the controller
     * Called after FXML file has been loaded
     */
    @FXML
    public void initialize() {
        // Initialize graph
        graph = new Graph();
        nodePositions = new HashMap<>();
        nodeVelocities = new HashMap<>();
        
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
        
        if (executeButton != null) {
            executeButton.setOnAction(event -> handleExecuteAlgorithm());
        }
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
            Node newNode = new Node(nodeName);
            graph.addNode(newNode);
            
            // Initialize position and velocity for new node
            double width = graphCanvas.getWidth();
            double height = graphCanvas.getHeight();
            double randomX = Math.random() * (width - 100) + 50;
            double randomY = Math.random() * (height - 100) + 50;
            nodePositions.put(newNode, new javafx.geometry.Point2D(randomX, randomY));
            nodeVelocities.put(newNode, new javafx.geometry.Point2D(0, 0));
            
            logMessage("Added node: " + nodeName);
            nodeInput.clear();
            
            // Apply force-directed layout
            applyForceDirectedLayout();
            refreshCanvas();
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
            Node startNode = graph.getNode(startNodeName);
            Node endNode = graph.getNode(endNodeName);
            
            if (startNode == null || endNode == null) {
                showError("One or both nodes do not exist in the graph");
                return;
            }
            
            graph.addUndirectedEdge(startNode, endNode);
            logMessage("Added edge: " + startNodeName + " <-> " + endNodeName);
            edgeStartInput.clear();
            edgeEndInput.clear();
            refreshCanvas();
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
        graph = new Graph();
        nodePositions.clear();
        nodeVelocities.clear();
        logMessage("═══════════════════════════════════");
        logMessage("Graph cleared");
        logMessage("═══════════════════════════════════");
        refreshCanvas();
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
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
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
        Map<String, Node> nodeMap = new HashMap<>();
        for (String nodeName : nodeNames) {
            Node newNode = new Node(nodeName);
            graph.addNode(newNode);
            nodeMap.put(nodeName, newNode);
            
            // Initialize position and velocity for new node
            double width = graphCanvas.getWidth();
            double height = graphCanvas.getHeight();
            double randomX = Math.random() * (width - 100) + 50;
            double randomY = Math.random() * (height - 100) + 50;
            nodePositions.put(newNode, new javafx.geometry.Point2D(randomX, randomY));
            nodeVelocities.put(newNode, new javafx.geometry.Point2D(0, 0));
        }
        
        // Add all edges to the graph
        for (String[] edge : edges) {
            Node startNode = nodeMap.get(edge[0]);
            Node endNode = nodeMap.get(edge[1]);
            
            if (startNode == null || endNode == null) {
                throw new IllegalArgumentException("Edge references non-existent node: " + 
                    (startNode == null ? edge[0] : edge[1]));
            }
            
            graph.addUndirectedEdge(startNode, endNode);
        }
        
        logMessage("Loaded " + nodeNames.size() + " nodes and " + edges.size() + " edges");
        
        // Apply force-directed layout and refresh
        applyForceDirectedLayout();
        refreshCanvas();
    }
    
    /**
     * Handle executing the selected algorithm
     */
    @FXML
    private void handleExecuteAlgorithm() {
        if (isAnimating) {
            showError("Animation already in progress");
            return;
        }
        
        if (graph.getNodeList().isEmpty()) {
            showError("Graph is empty. Add nodes first.");
            return;
        }
        
        String startNodeName = startNodeInput.getText().trim();
        if (startNodeName.isEmpty()) {
            showError("Please specify a starting node");
            return;
        }
        
        Node startNode = graph.getNode(startNodeName);
        if (startNode == null) {
            showError("Starting node '" + startNodeName + "' not found");
            return;
        }
        
        String selectedAlgorithm = algorithmCombo.getValue();
        logMessage("═══════════════════════════════════");
        logMessage("Starting " + selectedAlgorithm + " from node: " + startNodeName);
        logMessage("═══════════════════════════════════");
        
        // Run animation in background thread
        Thread animationThread = new Thread(() -> {
            try {
                isAnimating = true;
                if ("DFS Traversal".equals(selectedAlgorithm)) {
                    animateDFS(startNode);
                } else if ("BFS Traversal".equals(selectedAlgorithm)) {
                    animateBFS(startNode);
                } else if("Connectivity".equals(selectedAlgorithm)){
                    connectivity();
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Algorithm execution error: " + e.getMessage()));
            } finally {
                isAnimating = false;
                Platform.runLater(this::refreshCanvas);
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }
    
    /**
     * Apply force-directed layout using Coulomb's law (repulsive forces)
     * Nodes repel each other to spread out naturally
     */
    private void applyForceDirectedLayout() {
        double width = graphCanvas.getWidth();
        double height = graphCanvas.getHeight();
        
        // Run multiple iterations to stabilize positions
        for (int iter = 0; iter < ITERATIONS; iter++) {
            List<Node> nodes = graph.getNodeList();
            
            // Initialize forces for all nodes
            Map<Node, double[]> forces = new HashMap<>();
            for (Node node : nodes) {
                forces.put(node, new double[]{0, 0});
            }
            
            // Calculate repulsive forces between all node pairs
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node node1 = nodes.get(i);
                    Node node2 = nodes.get(j);
                    
                    javafx.geometry.Point2D pos1 = nodePositions.get(node1);
                    javafx.geometry.Point2D pos2 = nodePositions.get(node2);
                    
                    if (pos1 != null && pos2 != null) {
                        double dx = pos2.getX() - pos1.getX();
                        double dy = pos2.getY() - pos1.getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        
                        // Avoid division by zero
                        if (distance < 1) distance = 1;
                        
                        // Calculate repulsive force (Coulomb's law: F = k*q1*q2/r^2)
                        double force = COULOMB_CONSTANT / (distance * distance);
                        
                        // Normalize direction and apply force
                        double fx = (dx / distance) * force;
                        double fy = (dy / distance) * force;
                        
                        // Node1 is pushed away from Node2 (opposite direction)
                        forces.get(node1)[0] -= fx;
                        forces.get(node1)[1] -= fy;
                        
                        // Node2 is pushed away from Node1
                        forces.get(node2)[0] += fx;
                        forces.get(node2)[1] += fy;
                    }
                }
            }
            
            // Apply forces and update positions with dampening
            for (Node node : nodes) {
                javafx.geometry.Point2D pos = nodePositions.get(node);        javafx.geometry.Point2D vel = nodeVelocities.get(node);
                
                if (pos != null && vel != null) {
                    double[] force = forces.get(node);
                    
                    // Update velocity
                    double vx = (vel.getX() + force[0]) * DAMPENING;
                    double vy = (vel.getY() + force[1]) * DAMPENING;
                    
                    // Limit velocity
                    double vMagnitude = Math.sqrt(vx * vx + vy * vy);
                    if (vMagnitude > MAX_VELOCITY) {
                        vx = (vx / vMagnitude) * MAX_VELOCITY;
                        vy = (vy / vMagnitude) * MAX_VELOCITY;
                    }
                    
                    nodeVelocities.put(node, new javafx.geometry.Point2D(vx, vy));
                    
                    // Update position
                    double newX = pos.getX() + vx;
                    double newY = pos.getY() + vy;
                    
                    // Keep nodes within bounds
                    newX = Math.max(NODE_RADIUS + 10, Math.min(width - NODE_RADIUS - 10, newX));
                    newY = Math.max(NODE_RADIUS + 10, Math.min(height - NODE_RADIUS - 10, newY));
                    
                    nodePositions.put(node, new javafx.geometry.Point2D(newX, newY));
                }
            }
        }
    }
    
    /**
     * Animate DFS traversal with step-by-step visualization
     * Handles disconnected graphs by visiting all connected components
     */
    private void animateDFS(Node startNode) throws InterruptedException {
        Set<Node> visited = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        List<String> traversalOrder = new ArrayList<>();
        int stepCount = 0;
        
        // List of all nodes in graph
        List<Node> allNodes = graph.getNodeList();
        
        // Start with the provided starting node
        Node currentStart = startNode;
        
        // Continue until all nodes are visited (handle disconnected components)
        while (visited.size() < allNodes.size()) {
            if (Thread.currentThread().isInterrupted()) break;
            
            // If stack is empty, find an unvisited node to start a new component
            if (stack.isEmpty()) {
                Node unvisitedNode = null;
                for (Node node : allNodes) {
                    if (!visited.contains(node)) {
                        unvisitedNode = node;
                        break;
                    }
                }
                
                if (unvisitedNode == null) break; // All nodes visited
                
                // Log new component
                logMessage("[Component] Starting new component from: " + unvisitedNode.getNodeName());
                stack.push(unvisitedNode);
            }
            
            Node current = stack.pop();
            
            if (!visited.contains(current)) {
                visited.add(current);
                traversalOrder.add(current.getNodeName());
                stepCount++;
                
                final int step = stepCount;
                final Node nodeToDraw = current;
                final Set<Node> visitedCopy = new HashSet<>(visited);
                
                Platform.runLater(() -> {
                    logMessage("[Step " + step + "] Visiting: " + nodeToDraw.getNodeName());
                    drawGraphWithHighlight(visitedCopy, nodeToDraw);
                });
                
                // Wait based on speed slider
                Thread.sleep((long)(3000 - speedSlider.getValue()));
                
                // Add neighbors in reverse order for DFS stack (so they're processed in order)
                List<Node> neighbors = new ArrayList<>(current.getAdjacencyList());
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    Node neighbor = neighbors.get(i);
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                    }
                }
            }
        }
        
        logMessage("═══════════════════════════════════");
        logMessage("DFS Complete! Visited " + visited.size() + " nodes");
        logMessage("Order: " + traversalOrder);
        logMessage("Graph has " + getNumberOfComponents(visited) + " connected component(s)");
        logMessage("═══════════════════════════════════");
    }
    
    /**
     * Animate BFS traversal with step-by-step visualization
     * Handles disconnected graphs by visiting all connected components
     */
    private void animateBFS(Node startNode) throws InterruptedException {
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        List<String> traversalOrder = new ArrayList<>();
        int stepCount = 0;
        
        // List of all nodes in graph
        List<Node> allNodes = graph.getNodeList();
        
        // Continue until all nodes are visited (handle disconnected components)
        queue.add(startNode);
        visited.add(startNode);
        
        while (visited.size() < allNodes.size()) {
            if (Thread.currentThread().isInterrupted()) break;
            
            // Process current queue level
            while (!queue.isEmpty()) {
                if (Thread.currentThread().isInterrupted()) break;
                
                Node current = queue.poll();
                traversalOrder.add(current.getNodeName());
                stepCount++;
                
                final int step = stepCount;
                final Node nodeToDraw = current;
                final Set<Node> visitedCopy = new HashSet<>(visited);
                
                Platform.runLater(() -> {
                    logMessage("[Step " + step + "] Visiting: " + nodeToDraw.getNodeName());
                    drawGraphWithHighlight(visitedCopy, nodeToDraw);
                });
                
                // Wait based on speed slider
                Thread.sleep((long)(3000 - speedSlider.getValue()));
                
                for (Node neighbor : current.getAdjacencyList()) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            
            // If queue is empty but not all nodes visited, find next unvisited node
            if (queue.isEmpty() && visited.size() < allNodes.size()) {
                Node unvisitedNode = null;
                for (Node node : allNodes) {
                    if (!visited.contains(node)) {
                        unvisitedNode = node;
                        break;
                    }
                }
                
                if (unvisitedNode != null) {
                    logMessage("[Component] Starting new component from: " + unvisitedNode.getNodeName());
                    queue.add(unvisitedNode);
                    visited.add(unvisitedNode);
                }
            }
        }
        
        logMessage("═══════════════════════════════════");
        logMessage("BFS Complete! Visited " + visited.size() + " nodes");
        logMessage("Order: " + traversalOrder);
        logMessage("Graph has " + getNumberOfComponents(visited) + " connected component(s)");
        logMessage("═══════════════════════════════════");
    }
    
    /**
     * Draw graph with highlighted nodes
     */
    private void drawGraphWithHighlight(Set<Node> visited, Node current) {
        if (graphCanvas == null) return;
        
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double width = graphCanvas.getWidth();
        double height = graphCanvas.getHeight();
        
        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        
        // Draw edges first
        drawEdges(gc);
        
        // Draw nodes
        for (Node node : graph.getNodeList()) {
            javafx.geometry.Point2D pos = nodePositions.get(node);
            if (pos == null) continue;
            
            Color nodeColor = Color.LIGHTBLUE;
            
            if (node.equals(current)) {
                nodeColor = Color.RED; // Current node
            } else if (visited.contains(node)) {
                nodeColor = Color.GREEN; // Already visited
            }
            
            drawNode(gc, pos.getX(), pos.getY(), node.getNodeName(), nodeColor);
        }
        
        // Draw border
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, width, height);
    }
    
    /**
     * Draw edges between nodes
     */
    private void drawEdges(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        
        Set<String> drawnEdges = new HashSet<>();
        
        for (Node node : graph.getNodeList()) {
            javafx.geometry.Point2D pos1 = nodePositions.get(node);
            if (pos1 == null) continue;
            
            for (Node neighbor : node.getAdjacencyList()) {
                javafx.geometry.Point2D pos2 = nodePositions.get(neighbor);
                if (pos2 == null) continue;
                
                // Avoid drawing the same edge twice
                String edgeKey = Math.min(node.hashCode(), neighbor.hashCode()) + "-" + 
                                Math.max(node.hashCode(), neighbor.hashCode());
                
                if (!drawnEdges.contains(edgeKey)) {
                    gc.strokeLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
                    drawnEdges.add(edgeKey);
                }
            }
        }
    }
    
    /**
     * Draw a single node with thick border for visibility
     */
    private void drawNode(GraphicsContext gc, double x, double y, String label, Color color) {
        // Draw node circle with color
        gc.setFill(color);
        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw thick border for clear visibility
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        
        // Draw label centered in node
        gc.setFill(Color.BLACK);
        gc.setFont(new javafx.scene.text.Font("Arial", 12));
        
        // Center the text
        double textWidth = label.length() * 7;
        double textX = x - textWidth / 2;
        double textY = y + 5;
        
        gc.fillText(label, textX, textY);
    }
    
    /**
     * Refresh the graph canvas visualization
     */
    private void refreshCanvas() {
        if (graphCanvas == null) return;
        
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        double width = graphCanvas.getWidth();
        double height = graphCanvas.getHeight();
        
        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        
        // Initialize positions if needed
        if (nodePositions.isEmpty() && !graph.getNodeList().isEmpty()) {
            initializeNodePositions();
        }
        
        // Draw edges
        drawEdges(gc);
        
        // Draw all nodes
        for (Node node : graph.getNodeList()) {
            javafx.geometry.Point2D pos = nodePositions.get(node);
            if (pos != null) {
                drawNode(gc, pos.getX(), pos.getY(), node.getNodeName(), Color.LIGHTBLUE);
            }
        }
        
        // Draw border
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, width, height);
    }
    
    /**
     * Initialize node positions randomly
     */
    private void initializeNodePositions() {
        double width = graphCanvas.getWidth();
        double height = graphCanvas.getHeight();
        
        for (Node node : graph.getNodeList()) {
            double randomX = Math.random() * (width - 100) + 50;
            double randomY = Math.random() * (height - 100) + 50;
            nodePositions.put(node, new javafx.geometry.Point2D(randomX, randomY));
            nodeVelocities.put(node, new javafx.geometry.Point2D(0, 0));
        }
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
    
    /**
     * Get the graph instance
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Count the number of connected components in the graph
     * Uses DFS to properly count disconnected components
     */
    private int getNumberOfComponents(Set<Node> visited) {
        if (visited.isEmpty()) return 0;
        
        List<Node> allNodes = graph.getNodeList();
        Set<Node> componentVisited = new HashSet<>();
        int componentCount = 0;
        
        for (Node node : allNodes) {
            if (!componentVisited.contains(node)) {
                // Start a new component
                componentCount++;
                dfsCountComponent(node, componentVisited);
            }
        }
        
        return componentCount;
    }
    
    /**
     * DFS helper to mark all nodes in a connected component
     */
    private void dfsCountComponent(Node node, Set<Node> visited) {
        visited.add(node);
        for (Node neighbor : node.getAdjacencyList()) {
            if (!visited.contains(neighbor)) {
                dfsCountComponent(neighbor, visited);
            }
        }
    }
    
    /**
     * Check graph connectivity status
     */
    private void connectivity(){
        if (GraphComponent.isOneComponent(graph)) {
            logMessage("Graph is Connected");
        } else {
            logMessage("Graph is NOT Connected");
        }
    }
}
