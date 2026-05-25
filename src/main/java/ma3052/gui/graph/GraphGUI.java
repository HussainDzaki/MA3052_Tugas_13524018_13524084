package ma3052.gui.graph;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Manages the visualization and interaction with graph data structure
 */
public class GraphGUI {
    // What to do when canvas is interacted
    public static enum Mode {
        Lock, // Fix node position
        Add, // Add node and edges
        Edit, // Edit node name, node value, and edge weight on a window
        Delete, // Delete node and edges
    }

    private Mode mode = Mode.Lock;

    // Graph data structure
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private Graph graph;
    private List<NodeGUI> nodeGUIList;
    private List<EdgeGUI> edgeGUIList;
    private Map<Node, NodeGUI> nodeMap;
    private Map<Edge, EdgeGUI> edgeMap;

    // Draw options
    private boolean drawNodeValue = true;
    private boolean drawEdgeWeight = true;

    // Animation parameters
    private static final double MIN_DISTANCE = 120;
    private static final double COULOMB_CONSTANT = 400;
    private static final double CENTER_GRAVITY_CONSTANT = 2000;

    // Force-directed layout parameters
    private static final double FIXED_DELTA_TIME = 0.02; // 50 fps
    private static final long FIXED_DELTA_TIME_MS = 20; // 50 fps

    // Detection parameters
    private static final double MAX_DISTANCE_FROM_EDGE_TO_CLICK = 10;

    // Animation
    private volatile boolean isPhysicEnabled = true;
    private volatile boolean isDrawing = true;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

    // Canvas Update
    private Runnable onGraphUpdateRunnable = null;

    // Canvas moving
    private boolean isMoving = false;
    private Point2D movePosition = new Point2D(0, 0);

    // Canvas scale
    private double canvasScale = 1;

    // Node dragging
    private boolean isDragging = false;
    private boolean initialLockPosition = false;
    private NodeGUI draggedNodeGUI;

    // Node clicking
    private Point2D startClickPosition;

    // Cursor state
    private boolean isCursorPointing = false;

    // Adding edge
    private NodeGUI dummyNodeGUI = new NodeGUI(new Node()); // For drawing edge to the cursor
    private EdgeGUI dummyEdgeGUI; // For drawing edge to the cursor
    private NodeGUI sourceNodeGUI;

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

        dummyNodeGUI.setColor(Color.TRANSPARENT);
        dummyNodeGUI.setTextColor(Color.TRANSPARENT);

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            onCanvasClickStart(event);
            onCanvasDragStart(event);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            onCanvasClickEnd(event);
            onCanvasDragEnd(event);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            onCanvasHover(event);
        });

        Platform.runLater(() -> {
            canvas.getScene().addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                onCanvasDragMove(event);
            });
            canvas.getScene().addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                onCanvasDragEnd(event);
                sourceNodeGUI = null;
                dummyEdgeGUI = null;
            });
            canvas.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                threadPoolExecutor.shutdownNow();
            });
        });
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Canvas getCanvas() {
        return canvas;
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
            // Set drawWeight based on whether edge has a non-default weight
            edgeGUI.setDrawWeight(edge.getWeight() != Edge.DEFAULT_WEIGHT);
        }
    }

    public void clearGraph() {
        setGraph(new Graph());
    }

    public NodeGUI getNodeGUI(Node node) {
        return nodeMap.get(node);
    }

    public List<NodeGUI> getNodeGUIList() {
        return Collections.unmodifiableList(nodeGUIList);
    }

    public EdgeGUI getEdgeGUI(Edge edge) {
        return edgeMap.get(edge);
    }

    public List<EdgeGUI> getEdgeGUIList() {
        return Collections.unmodifiableList(edgeGUIList);
    }

    /**
     * Find EdgeGUI by matching source and destination nodes.
     * Handles both directed and undirected graphs.
     * Single-pass implementation for better performance.
     */
    public EdgeGUI getEdgeGUI(Node source, Node destination) {
        for (Edge edge : graph.getEdgeList()) {
            // Check direct direction
            if (edge.getSource().equals(source) && edge.getDestination().equals(destination)) {
                return edgeMap.get(edge);
            }

            // Check reverse direction (for undirected or bidirectional graphs)
            if (edge.getSource().equals(destination) && edge.getDestination().equals(source)) {
                return edgeMap.get(edge);
            }
        }
        return null;
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
            edgeGUIList.removeIf((edgeGUI) -> {
                if (edgeGUI.getEdge().getSource() == node || edgeGUI.getEdge().getDestination() == node) {
                    edgeMap.remove(edgeGUI.getEdge());
                    graph.removeEdge(edgeGUI.getEdge());
                    return true;
                }
                return false;
            });
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
        for (Edge edge : getGraph().getEdgeList()) {
            getEdgeGUI(edge).setDrawWeight(drawEdgeWeight);
        }
    }

    public boolean isDrawNodeValue() {
        return drawNodeValue;
    }

    public void setDrawNodeValue(boolean drawNodeValue) {
        this.drawNodeValue = drawNodeValue;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
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

    private void updatePhysics() {
        // System.out.println("Updating physics");
        // Add force to the center
        for (NodeGUI nodeGUI : nodeGUIList) {
            Point2D offset = canvasToNodePosition(new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2))
                    .subtract(nodeGUI.getPosition());
            if (offset.magnitude() > CENTER_GRAVITY_CONSTANT * FIXED_DELTA_TIME) {
                Point2D gravityForce = offset.normalize().multiply(CENTER_GRAVITY_CONSTANT);
                nodeGUI.addForce(gravityForce);
            }
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
        }
    }

    private void drawNodes() {
        for (NodeGUI nodeGUI : nodeGUIList) {
            nodeGUI.draw(graphicsContext);
        }
    }

    private void drawEdges() {
        if (dummyEdgeGUI != null) {
            dummyEdgeGUI.draw(graphicsContext, graph.isDirected());
        }
        for (EdgeGUI edgeGUI : edgeGUIList) {
            edgeGUI.draw(graphicsContext, graph.isDirected(), graph.isDirected()
                    && graph.hasEdge(edgeGUI.getEdge().getDestination(), edgeGUI.getEdge().getSource()));
        }
    }

    private Point2D canvasToNodePosition(Point2D point) {
        try {
            return graphicsContext.getTransform().inverseTransform(point);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return point;
    }

    private Point2D sceneToNodePosition(Point2D point) {
        try {
            return graphicsContext.getTransform().inverseTransform(canvas.sceneToLocal(point));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return point;
    }

    private void clearCanvas() {
        graphicsContext.save();
        graphicsContext.setFill(Color.WHITE);
        Point2D start = canvasToNodePosition(new Point2D(0, 0));
        Point2D end = canvasToNodePosition(new Point2D(canvas.getWidth(), canvas.getHeight()));
        graphicsContext.fillRect(start.getX(), start.getY(), end.getX() - start.getX(), end.getY() - start.getY());
        graphicsContext.restore();
    }

    private void updateCanvas() {
        // System.out.println("Updating canvas...");
        Platform.runLater(() -> {
            clearCanvas();
            drawEdges();
            drawNodes();
        });
    }

    public void zoomIn() {
        if (canvasScale < 1.80) {
            try {
                Point2D graphCenter = graphicsContext.getTransform()
                        .inverseTransform(new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2));
                graphicsContext.translate(-graphCenter.getX() * 0.25, -graphCenter.getY() * 0.25);
                graphicsContext.scale(1.25, 1.25);
                canvasScale *= 1.25;
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public void zoomOut() {
        if (canvasScale > 0.25) {
            try {
                Point2D graphCenter = graphicsContext.getTransform()
                        .inverseTransform(new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2));
                graphicsContext.translate(graphCenter.getX() * 0.20, graphCenter.getY() * 0.20);
                graphicsContext.scale(0.80, 0.80);
                canvasScale *= 0.80;
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private NodeGUI getNodeGUIOnPosition(Point2D pos) {
        for (NodeGUI nodeGUI : nodeGUIList) {
            if (nodeGUI.getPosition().subtract(pos).magnitude() <= nodeGUI
                    .getRadius()) {
                return nodeGUI;
            }
        }
        return null;
    }

    private EdgeGUI getEdgeGUIOnPosition(Point2D pos) {
        for (EdgeGUI edgeGUI : edgeGUIList) {
            Point2D node1 = edgeGUI.getSourceGUI().getPosition();
            Point2D node2 = edgeGUI.getDestinationGUI().getPosition();
            Point2D pos1 = pos.subtract(node1);
            Point2D pos2 = pos.subtract(node2);
            Point2D segment1 = node2.subtract(node1);
            Point2D segment2 = node1.subtract(node2);

            // Tidak berada di antara dua node
            if (pos1.dotProduct(segment1) < 0 || pos2.dotProduct(segment2) < 0) {
                continue;
            }

            // Hitung garis ax + by + c = 0;
            // Diket: (x2 - x1) * (y - y1) = (x - x1) * (y2 - y1)
            double a = (node2.getY() - node1.getY());
            double b = -(node2.getX() - node1.getX());
            double c = -node1.getX() * a + -node1.getY() * b;

            // Jarak = |ax + by + c| / sqrt(a^2 + b^2)
            double distance = Math.abs((a * pos.getX() + b * pos.getY() + c) / Math.sqrt(a * a + b * b));
            if (distance < MAX_DISTANCE_FROM_EDGE_TO_CLICK) {
                return edgeGUI;
            }
        }
        return null;
    }

    private String getNextNodeName() {
        if (graph.isEmpty()) {
            return "1";
        } else {
            int nextNumber = 1;
            for (Node node : graph.getNodeList()) {
                try {
                    nextNumber = Math.max(nextNumber, Integer.parseInt(node.getNodeName()) + 1);
                } catch (Exception e) {
                    // Do nothing
                }
            }
            return Integer.toString(nextNumber);
        }
    }

    private void onCanvasDragStart(MouseEvent event) {
        movePosition = new Point2D(event.getSceneX(), event.getSceneY());
        NodeGUI nodeGUI = getNodeGUIOnPosition(sceneToNodePosition(movePosition));
        if (nodeGUI != null) {
            isDragging = true;
            draggedNodeGUI = nodeGUI;
            initialLockPosition = draggedNodeGUI.isLockPosition();
            draggedNodeGUI.setLockPosition(true);
        } else {
            isMoving = true;
        }
    }

    private void onCanvasDragMove(MouseEvent event) {
        Point2D deltaPosition = new Point2D(
                (event.getSceneX() - movePosition.getX()) / canvasScale,
                (event.getSceneY() - movePosition.getY()) / canvasScale);
        movePosition = new Point2D(event.getSceneX(), event.getSceneY());
        if (isDragging) {
            draggedNodeGUI.setPosition(draggedNodeGUI.getPosition().add(deltaPosition));
        }
        if (isMoving) {
            graphicsContext.translate(deltaPosition.getX(), deltaPosition.getY());
        }
        dummyNodeGUI.setPosition(sceneToNodePosition(movePosition));
    }

    private void onCanvasDragEnd(MouseEvent event) {
        if (isDragging) {
            // If is actually dragged, not just a click
            if (mode != Mode.Lock || startClickPosition.distance(event.getX(), event.getY()) > 5) {
                draggedNodeGUI.setLockPosition(initialLockPosition);
            }
            isDragging = false;
        }
        if (isMoving) {
            isMoving = false;
        }
    }

    private void onCanvasClickStart(MouseEvent event) {
        startClickPosition = new Point2D(event.getX(), event.getY());
    }

    private void onCanvasClickEnd(MouseEvent event) {
        // If is not a drag
        if (startClickPosition.distance(event.getX(), event.getY()) <= 5) {
            onCanvasClick(event);
        }
    }

    private void onCanvasClick(MouseEvent event) {
        Point2D pos = new Point2D(event.getSceneX(), event.getSceneY());
        NodeGUI nodeGUI = getNodeGUIOnPosition(sceneToNodePosition(pos));
        EdgeGUI edgeGUI = getEdgeGUIOnPosition(sceneToNodePosition(pos));
        switch (mode) {
            case Lock:
                if (nodeGUI != null) {
                    if (initialLockPosition) {
                        nodeGUI.setBorderWidth(3);
                        nodeGUI.setLockPosition(false);
                    } else {
                        nodeGUI.setBorderWidth(5);
                        nodeGUI.setLockPosition(true);
                    }
                }
                break;
            case Add:
                if (sourceNodeGUI == null) {
                    if (nodeGUI == null) {
                        Node newNode = new Node(getNextNodeName());
                        addNode(newNode);
                        NodeGUI newNodeGUI = getNodeGUI(newNode);
                        newNodeGUI.setPosition(sceneToNodePosition(pos));
                        if (onGraphUpdateRunnable != null) {
                            onGraphUpdateRunnable.run();
                        }
                    } else {
                        sourceNodeGUI = nodeGUI;
                        dummyEdgeGUI = new EdgeGUI(null, sourceNodeGUI, dummyNodeGUI);
                    }
                } else {
                    if (nodeGUI != null) {
                        addEdge(sourceNodeGUI.getNode(), nodeGUI.getNode());
                        if (onGraphUpdateRunnable != null) {
                            onGraphUpdateRunnable.run();
                        }
                    }
                    sourceNodeGUI = null;
                    dummyEdgeGUI = null;
                }
                break;
            case Edit:
                System.out.println("Not implemented yet");
                break;
            case Delete:
                if (nodeGUI != null) {
                    removeNode(nodeGUI.getNode());
                    if (onGraphUpdateRunnable != null) {
                        onGraphUpdateRunnable.run();
                    }
                } else if (edgeGUI != null) {
                    removeEdge(edgeGUI.getEdge());
                    if (onGraphUpdateRunnable != null) {
                        onGraphUpdateRunnable.run();
                    }
                }
                break;

            default:
                break;
        }
    }

    private void onCanvasHover(MouseEvent event) {
        Point2D pos = new Point2D(event.getSceneX(), event.getSceneY());
        NodeGUI nodeGUI = getNodeGUIOnPosition(sceneToNodePosition(pos));
        EdgeGUI edgeGUI = getEdgeGUIOnPosition(sceneToNodePosition(pos));
        if (nodeGUI != null || edgeGUI != null && (mode == Mode.Delete || mode == Mode.Edit)) {
            if (!isCursorPointing) {
                canvas.getStyleClass().add("cursor-pointer");
                // canvas.getStyleClass().remove("cursor-grab");
                isCursorPointing = true;
            }
        } else {
            if (isCursorPointing) {
                canvas.getStyleClass().remove("cursor-pointer");
                // canvas.getStyleClass().add("cursor-grab");
                isCursorPointing = false;
            }
        }
        dummyNodeGUI.setPosition(sceneToNodePosition(pos));
    }

    public void updateGraph() {
        if (onGraphUpdateRunnable != null) {
            onGraphUpdateRunnable.run();
        }
    }

    public void setOnGraphUpdate(Runnable runnable) {
        onGraphUpdateRunnable = runnable;
    }

    /**
     * Stop the rendering thread
     */
    public void stop() {
        isDrawing = false;
        threadPoolExecutor.shutdownNow();
    }

    public String pathToString(List<Node> path) {
        boolean first = true;
        String res = "";
        for (int i = 0; i < path.size(); i++) {
            if (first) {
                res += path.get(i).getNodeName().toString();
                first = false;
            } else {
                res += " -> " + path.get(i).getNodeName().toString();
            }
        }
        return res;
    }
}
