package ma3052.gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manages the visualization and interaction with graph data structure
 */

public class GraphGUI {
    // Graph data structure
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private Graph graph;
    private List<NodeGUI> nodeGUIList;
    private List<EdgeGUI> edgeGUIList;
    private Map<Node, NodeGUI> nodeMap;
    private Map<Edge, EdgeGUI> edgeMap;

    // Draw options
    private boolean drawNodeValue = false;
    private boolean drawEdgeWeight = false;

    // Animation parameters
    private static final double MIN_DISTANCE = 120;
    private static final double COULOMB_CONSTANT = 400;
    private static final double CENTER_GRAVITY_CONSTANT = 200;

    // Force-directed layout parameters
    private static final double FIXED_DELTA_TIME = 0.02; // 50 fps
    private static final long FIXED_DELTA_TIME_MS = 20; // 50 fps

    // Animation
    private volatile boolean isRunningAlgorithm = false;
    private volatile boolean isPhysicEnabled = true;
    private volatile boolean isDrawing = true;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

    // Node dragging
    private boolean isDragging = false;
    private Point2D dragOffset;
    private NodeGUI draggedNodeGUI;

    public GraphGUI(Canvas canvas) {
        graph = new Graph();
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.nodeMap = new HashMap<>();
        this.edgeMap = new HashMap<>();
        this.nodeGUIList = new ArrayList<>();
        this.edgeGUIList = new ArrayList<>();
        threadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        threadPoolExecutor.scheduleAtFixedRate(() -> {
            if (isPhysicEnabled)
                updatePhysics();
        }, 0, FIXED_DELTA_TIME_MS, TimeUnit.MILLISECONDS);
        threadPoolExecutor.scheduleWithFixedDelay(() -> {
            if (isDrawing)
                updateCanvas();
        }, 0, 16, TimeUnit.MILLISECONDS);

        canvas.setOnMousePressed(event -> {
            onCanvasDragStart(event);
        });

        canvas.setOnMouseDragged(event -> {
            onCanvasDragMove(event);
        });

        canvas.setOnMouseClicked(event -> {
            onCanvasDragEnd(event);
        });

        canvas.setOnMouseExited(event -> {
            onCanvasDragEnd(event);
        });

        Platform.runLater(() -> {
            canvas.getScene().getWindow().setOnCloseRequest(event -> {
                threadPoolExecutor.shutdown();
            });
        });
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        nodeMap.clear();
        edgeMap.clear();
        nodeGUIList.clear();
        edgeGUIList.clear();
        for (Node node : graph.getNodeList()) {
            NodeGUI nodeGUI = new NodeGUI(node);
            nodeMap.put(node, nodeGUI);
            nodeGUIList.add(nodeGUI);

            // Randomize position
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            double randomX = Math.random() * (width - 100) + 50;
            double randomY = Math.random() * (height - 100) + 50;
            nodeGUI.setPosition(new Point2D(randomX, randomY));
        }
        for (Edge edge : graph.getEdgeList()) {
            EdgeGUI edgeGUI = new EdgeGUI(edge, getNodeGUI(edge.getSource()), getNodeGUI(edge.getDestination()));
            edgeMap.put(edge, edgeGUI);
            edgeGUIList.add(edgeGUI);
        }
    }

    public void clearGraph() {
        setGraph(new Graph());
    }

    public NodeGUI getNodeGUI(Node node) {
        return nodeMap.get(node);
    }

    public EdgeGUI getEdgeGUI(Edge edge) {
        return edgeMap.get(edge);
    }

    public void addNode(Node node) {
        if (!graph.hasNode(node)) {
            graph.addNode(node);
            NodeGUI nodeGUI = new NodeGUI(node);
            nodeGUIList.add(nodeGUI);
            nodeMap.put(node, nodeGUI);

            // Randomize position
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            double randomX = Math.random() * (width - 100) + 50;
            double randomY = Math.random() * (height - 100) + 50;
            nodeGUI.setPosition(new Point2D(randomX, randomY));
        }
    }

    public void removeNode(Node node) {
        if (graph.hasNode(node)) {
            nodeGUIList.remove(nodeMap.get(node));
            nodeMap.remove(node);
            graph.removeNode(node);
        }
    }

    public void addEdge(Node source, Node destination) {
        addNode(source);
        addNode(destination);
        if (!graph.hasEdge(source, destination)) {
            graph.addEdge(source, destination);
            Edge edge = graph.getEdge(source, destination);
            EdgeGUI edgeGUI = new EdgeGUI(edge, nodeMap.get(source), nodeMap.get(destination));
            edgeGUIList.add(edgeGUI);
            edgeMap.put(edge, edgeGUI);
        }
    }

    public void removeEdge(Edge edge) {
        if (graph.hasEdge(edge)) {
            edgeGUIList.remove(edgeMap.get(edge));
            edgeMap.remove(edge);
            graph.removeEdge(edge);
        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public boolean isDrawEdgeWeight() {
        return drawEdgeWeight;
    }

    public void setDrawEdgeWeight(boolean drawEdgeWeight) {
        this.drawEdgeWeight = drawEdgeWeight;
    }

    public boolean isDrawNodeValue() {
        return drawNodeValue;
    }

    public void setDrawNodeValue(boolean drawNodeValue) {
        this.drawNodeValue = drawNodeValue;
    }

    private void updatePhysics() {
        // System.out.println("Updating physics");
        // Add force to the center
        for (NodeGUI nodeGUI : nodeGUIList) {
            Point2D offset = new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2).subtract(nodeGUI.getPosition());
            Point2D gravityForce = offset.normalize().multiply(CENTER_GRAVITY_CONSTANT);
            nodeGUI.addForce(gravityForce);
        }

        // Add force between nodes
        for (int i = 0; i < nodeGUIList.size(); i++) {
            for (int j = i + 1; j < nodeGUIList.size(); j++) {
                NodeGUI node1 = nodeGUIList.get(i);
                NodeGUI node2 = nodeGUIList.get(j);
                // Push away two nodes if too close
                if (node1.getPosition().subtract(node2.getPosition()).magnitude() < MIN_DISTANCE) {
                    Point2D offset = node2.getPosition().subtract(node1.getPosition());
                    Point2D springForce = offset.normalize()
                            .multiply(COULOMB_CONSTANT * (offset.magnitude() - MIN_DISTANCE));
                    node1.addForce(springForce);
                    node2.addForce(springForce.multiply(-1));
                }
            }
        }

        // Add force from edge
        for (EdgeGUI edgeGUI : edgeGUIList) {
            edgeGUI.update();
        }

        // Update nodes position and velocity
        for (NodeGUI nodeGUI : nodeGUIList) {
            nodeGUI.update(FIXED_DELTA_TIME);
            nodeGUI.setForce(Point2D.ZERO); // Reset force
            nodeGUI.clampPosition(
                    nodeGUI.getRadius() + 10, nodeGUI.getRadius() + 10,
                    canvas.getWidth() - nodeGUI.getRadius() - 10, canvas.getHeight() - nodeGUI.getRadius() - 10);
        }
    }

    private void drawNodes() {
        for (NodeGUI nodeGUI : nodeGUIList) {
            nodeGUI.draw(graphicsContext, drawNodeValue);
        }
    }

    private void drawEdges() {
        for (EdgeGUI edgeGUI : edgeGUIList) {
            edgeGUI.draw(graphicsContext, drawEdgeWeight, graph.isDirected());
        }
    }

    public void resetColors() {
        for (NodeGUI nodeGUI : nodeGUIList) {
            nodeGUI.setColor(Color.WHITE);
            nodeGUI.setBorderColor(Color.BLACK);
        }
        for (EdgeGUI edgeGUI : edgeGUIList) {
            edgeGUI.setLineColor(Color.BLACK);
        }
    }

    private void clearCanvas() {
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void updateCanvas() {
        // System.out.println("Updating canvas...");
        Platform.runLater(() -> {
            clearCanvas();
            drawEdges();
            drawNodes();
        });
    }

    private void onCanvasDragStart(MouseEvent event) {
        for (NodeGUI nodeGUI : nodeGUIList) {
            if (nodeGUI.getPosition().subtract(new Point2D(event.getX(), event.getY())).magnitude() <= nodeGUI
                    .getRadius()) {
                isDragging = true;
                draggedNodeGUI = nodeGUI;
                Point2D dragPosition = new Point2D(event.getX(), event.getY());
                dragOffset = nodeGUI.getPosition().subtract(dragPosition);
                draggedNodeGUI.setLockPosition(true);
                break;
            }
        }
    }

    private void onCanvasDragMove(MouseEvent event) {
        if (isDragging) {
            Point2D currentPosition = new Point2D(event.getX(), event.getY());
            draggedNodeGUI.setPosition(currentPosition.add(dragOffset));
            draggedNodeGUI.clampPosition(
                    draggedNodeGUI.getRadius() + 10, draggedNodeGUI.getRadius() + 10,
                    canvas.getWidth() - draggedNodeGUI.getRadius() - 10,
                    canvas.getHeight() - draggedNodeGUI.getRadius() - 10);
        }
    }

    private void onCanvasDragEnd(MouseEvent event) {
        if (isDragging) {
            draggedNodeGUI.setLockPosition(false);
            isDragging = false;
        }
    }

    /**
     * Stop the rendering thread
     */
    public void stop() {
        isDrawing = false;
        threadPoolExecutor.shutdown();
    }
}
