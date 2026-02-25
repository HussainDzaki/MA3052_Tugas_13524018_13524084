package ma3052.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Graph {
    private ArrayList<Node> nodeList;

    public Graph() {
        nodeList = new ArrayList<Node>();
    }

    public Graph(Collection<Node> nodes) {
        nodeList = new ArrayList<Node>(nodes);
    }

    public List<Node> getNodeList() {
        return Collections.unmodifiableList(nodeList);
    }

    public Node getNode(long nodeID) {
        for (Node node : nodeList) {
            if (node.getNodeID() == nodeID) {
                return node;
            }
        }
        return null;
    }

    public Node getNode(String nodeName) {
        for (Node node : nodeList) {
            if (node.getNodeName() == nodeName) {
                return node;
            }
        }
        return null;
    }

    public void addNode(Node node) {
        nodeList.add(node);
    }

    public void addNodes(Collection<Node> node) {
        nodeList.addAll(node);
    }

    public void removeNode(Node node) {
        nodeList.remove(node);
    }

    public void removeNodes(Collection<Node> node) {
        nodeList.removeAll(node);
    }

    public void addUndirectedEdge(Node node1, Node node2) {
        addDirectedEdge(node1, node2);
        addDirectedEdge(node2, node1);
    }

    public void addDirectedEdge(Node sourceNode, Node destinationNode) {
        if (!hasNode(sourceNode)) {
            addNode(sourceNode);
        }
        if (!hasNode(destinationNode)) {
            addNode(destinationNode);
        }
        sourceNode.addAdjacentNode(destinationNode);
    }

    public void clear() {
        nodeList.clear();
    }

    public boolean isEmpty() {
        return nodeList.isEmpty();
    }

    public boolean hasNode(Node node) {
        return nodeList.contains(node);
    }

    public boolean hasNode(String nodeName) {
        for (Node node : nodeList) {
            if (node.getNodeName() == nodeName) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return nodeList.size();
    }
}
