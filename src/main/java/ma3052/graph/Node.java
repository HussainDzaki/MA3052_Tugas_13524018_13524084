package ma3052.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Node {
    private static long nodeIDCount = 1;

    private long nodeID;
    private String nodeName;
    private double value;
    private ArrayList<Edge> adjacencyList;

    public Node() {
        this.nodeID = nodeIDCount;
        this.nodeName = Long.toString(nodeIDCount);
        this.value = 0;
        this.adjacencyList = new ArrayList<Edge>();
        nodeIDCount++;
    }

    public Node(double value) {
        this.nodeID = nodeIDCount;
        this.nodeName = Long.toString(nodeIDCount);
        this.value = value;
        this.adjacencyList = new ArrayList<Edge>();
        nodeIDCount++;
    }

    public Node(String nodeName) {
        this.nodeID = nodeIDCount;
        this.nodeName = nodeName;
        this.value = 0;
        this.adjacencyList = new ArrayList<Edge>();
        nodeIDCount++;
    }

    public Node(String nodeName, double value) {
        this.nodeID = nodeIDCount;
        this.nodeName = nodeName;
        this.value = value;
        this.adjacencyList = new ArrayList<Edge>();
        nodeIDCount++;
    }

    public long getNodeID() {
        return nodeID;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public List<Edge> getAdjacencyList() {
        return Collections.unmodifiableList(adjacencyList);
    }

    public Edge getEdge(Node destination) {
        for (Edge edge : adjacencyList) {
            if (edge.getDestination() == destination) {
                return edge;
            }
        }
        return null;
    }

    public void addEdge(Node otherNode) {
        adjacencyList.add(new Edge(this, otherNode));
    }

    public void addEdge(Node otherNode, double edgeWeight) {
        adjacencyList.add(new Edge(this, otherNode, edgeWeight));
    }

    public void addEdges(Collection<Node> otherNodes) {
        for (Node node : otherNodes) {
            addEdge(node);
        }
    }

    public void addEdges(Collection<Node> otherNodes, double edgeWeight) {
        for (Node node : otherNodes) {
            addEdge(node, edgeWeight);
        }
    }

    public void removeEdge(Node otherNode) {
        adjacencyList.removeIf(e -> e.getDestination() == otherNode);
    }

    public void removeEdges(Collection<Node> otherNode) {
        for (Node node : otherNode) {
            removeEdge(node);
        }
    }

    public void clearEdges() {
        adjacencyList.clear();
    }

    public boolean isNodeAdjacent(Node otherNode) {
        for (Edge edge : adjacencyList) {
            if (edge.getDestination() == otherNode) {
                return true;
            }
        }
        return false;
    }
}
