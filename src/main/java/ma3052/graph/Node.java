package ma3052.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Node {
    private static long nodeIDCount = 1;

    private long nodeID;
    private String nodeName;
    private ArrayList<Node> adjacencyList;

    public Node() {
        this.nodeID = nodeIDCount;
        this.nodeName = Long.toString(nodeIDCount);
        this.adjacencyList = new ArrayList<Node>();
        nodeIDCount++;
    }

    public Node(String nodeName) {
        this.nodeID = nodeIDCount;
        this.nodeName = nodeName;
        this.adjacencyList = new ArrayList<Node>();
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

    public List<Node> getAdjacencyList() {
        return Collections.unmodifiableList(adjacencyList);
    }

    public void addAdjacentNode(Node otherNode) {
        adjacencyList.add(otherNode);
    }

    public void addAdjacentNodes(Collection<Node> otherNode) {
        adjacencyList.addAll(otherNode);
    }

    public void removeAdjacentNode(Node otherNode) {
        adjacencyList.remove(otherNode);
    }

    public void removeAdjacentNodes(Collection<Node> otherNode) {
        adjacencyList.removeAll(otherNode);
    }

    public void clearAdjacencyList() {
        adjacencyList.clear();
    }

    public boolean isNodeAdjacent(Node otherNode) {
        return adjacencyList.contains(otherNode);
    }
}
