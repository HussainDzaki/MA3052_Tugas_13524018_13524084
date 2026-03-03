package ma3052.gui;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Controller for Graph Visualization GUI
 * Manages the visualization and interaction with graph data structure
 * with animated DFS/BFS algorithm visualization and force-directed layout
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

    // Animation parameters
    private static final double MIN_DISTANCE = 100;
    private static final double COULOMB_CONSTANT = 400;
    private static final double CENTER_GRAVITY_CONSTANT = 400;

    // Force-directed layout parameters
    private static final double FIXED_DELTA_TIME = 0.02; // 50 fps
    private static final long FIXED_DELTA_TIME_MS = 20; // 50 fps

    // Animation
    private volatile boolean isRunningAlgorithm = false;
    private volatile boolean isPhysicEnabled = true;
    private volatile boolean isDrawing = true;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

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
        threadPoolExecutor.scheduleAtFixedRate(() -> {
            if (isDrawing)
                updateCanvas();
        }, 0, FIXED_DELTA_TIME_MS, TimeUnit.MILLISECONDS);
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
                    nodeGUI.getRadius(), nodeGUI.getRadius(),
                    canvas.getWidth() - nodeGUI.getRadius(), canvas.getHeight() - nodeGUI.getRadius());
        }
    }

    private void drawNodes() {
        for (NodeGUI nodeGUI : nodeGUIList) {
            nodeGUI.draw(graphicsContext, false);
        }
    }

    private void drawEdges() {
        for (EdgeGUI edgeGUI : edgeGUIList) {
            System.out.println(
                    "Drawing edge: " + edgeGUI.getSourceGUI().getNode().getNodeName() + " <-> "
                            + edgeGUI.getDestinationGUI().getNode().getNodeName());
            edgeGUI.draw(graphicsContext, false, false);
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
}
