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
            if (node.getNodeName().equals(nodeName)) {
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

    public void addUndirectedEdge(String node1, String node2) {
        addDirectedEdge(node1, node2);
        addDirectedEdge(node2, node1);
    }

    public void addUndirectedEdge(String node1, String node2, double weight) {
        addDirectedEdge(node1, node2, weight);
        addDirectedEdge(node2, node1, weight);
    }

    public void addUndirectedEdge(Node node1, Node node2) {
        addDirectedEdge(node1, node2);
        addDirectedEdge(node2, node1);
    }

    public void addUndirectedEdge(Node node1, Node node2, double weight) {
        addDirectedEdge(node1, node2, weight);
        addDirectedEdge(node2, node1, weight);
    }

    public void addDirectedEdge(String source, String destination) {
        Node n1, n2;
        if (hasNode(source)) {
            n1 = getNode(source);
        } else {
            n1 = new Node(source);
        }
        if (hasNode(destination)) {
            n2 = getNode(destination);
        } else {
            n2 = new Node(destination);
        }
        addDirectedEdge(n1, n2);
    }

    public void addDirectedEdge(String source, String destination, double weight) {
        Node n1, n2;
        if (hasNode(source)) {
            n1 = getNode(source);
        } else {
            n1 = new Node(source);
        }
        if (hasNode(destination)) {
            n2 = getNode(destination);
        } else {
            n2 = new Node(destination);
        }
        addDirectedEdge(n1, n2, weight);
    }

    public void addDirectedEdge(Node source, Node destination) {
        if (!hasNode(source)) {
            addNode(source);
        }
        if (!hasNode(destination)) {
            addNode(destination);
        }
        source.addEdge(destination);
    }

    public void addDirectedEdge(Node source, Node destination, double weight) {
        if (!hasNode(source)) {
            addNode(source);
        }
        if (!hasNode(destination)) {
            addNode(destination);
        }
        source.addEdge(destination, weight);
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
