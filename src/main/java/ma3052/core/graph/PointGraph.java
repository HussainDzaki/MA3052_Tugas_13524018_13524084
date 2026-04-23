package ma3052.core.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;

public class PointGraph {
    private Graph graph;
    private Map<Node, Point2D> nodePosition;
    private double scale = 1;

    public PointGraph() {
        graph = new Graph();
        nodePosition = new HashMap<Node, Point2D>();
    }

    public Node getNode(String nodeName) {
        return graph.getNode(nodeName);
    }

    public Edge getEdge(String node1, String node2) {
        return graph.getEdge(node1, node2);
    }

    public Edge getEdge(Node node1, Node node2) {
        return graph.getEdge(node1, node2);
    }

    public List<Node> getNodeList() {
        return graph.getNodeList();
    }

    public List<Edge> getEdgeList() {
        return graph.getEdgeList();
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Point2D getPosition(Node node) {
        return nodePosition.get(node);
    }

    public void setPosition(Node node, Point2D position) {
        nodePosition.put(node, position);
    }

    public void addNode(Node node, Point2D position) {
        graph.addNode(node);
        nodePosition.put(node, position);
    }

    public void removeNode(Node node) {
        graph.removeNode(node);
        nodePosition.remove(node);
    }

    public void addEdge(Node source, Node destination) {
        if (hasNode(source) && hasNode(destination)) {
            graph.addEdge(source, destination);
        }
    }

    public void removeEdge(Node source, Node destination) {
        graph.removeEdge(source, destination);
    }

    public void removeEdge(Edge edge) {
        graph.removeEdge(edge);
    }

    public void clear() {
        graph.clear();
    }

    public boolean isEmpty() {
        return graph.isEmpty();
    }

    public int size() {
        return graph.size();
    }

    public boolean hasNode(String nodeName) {
        return graph.hasNode(nodeName);
    }

    public boolean hasNode(Node node) {
        return graph.hasNode(node);
    }

    public boolean hasEdge(String source, String destination) {
        return graph.hasEdge(source, destination);
    }

    public boolean hasEdge(Node source, Node destination) {
        return graph.hasEdge(source, destination);
    }

    public boolean hasEdge(Edge edge) {
        return graph.hasEdge(edge);
    }

    public double getDistance(Node node1, Node node2) {
        return nodePosition.get(node2).distance(nodePosition.get(node1)) / scale;
    }
}