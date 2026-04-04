package ma3052.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.Button;

public class Graph {
    private ArrayList<Node> nodeList;
    private ArrayList<Edge> edgeList;
    private boolean directed = false;

    public Graph() {
        nodeList = new ArrayList<Node>();
        edgeList = new ArrayList<Edge>();
        directed = false;
    }

    public Graph(boolean directed) {
        nodeList = new ArrayList<Node>();
        edgeList = new ArrayList<Edge>();
        this.directed = directed;
    }

    public Graph(Collection<Node> nodes) {
        nodeList = new ArrayList<Node>(nodes);
        edgeList = new ArrayList<Edge>();
        directed = false;
    }

    public Graph(Collection<Node> nodes, boolean directed) {
        nodeList = new ArrayList<Node>(nodes);
        edgeList = new ArrayList<Edge>();
        this.directed = directed;
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
        if (hasNode(node)) {
            for (Edge edge : node.getAdjacencyList()) {
                edge.getDestination().removeEdge(node);
            }
            nodeList.remove(node);
        }
    }

    public void removeNodes(Collection<Node> nodes) {
        for (Node node : nodes) {
            removeNode(node);
        }
    }

    public List<Edge> getEdgeList() {
        return Collections.unmodifiableList(edgeList);
    }

    public Edge getEdge(String node1, String node2) {
        for (Edge edge : edgeList) {
            if (edge.getSource().getNodeName().equals(node1) && edge.getDestination().getNodeName().equals(node2)) {
                return edge;
            }
        }
        return null;
    }

    public Edge getEdge(Node node1, Node node2) {
        for (Edge edge : edgeList) {
            if (edge.getSource() == node1 && edge.getDestination() == node2) {
                return edge;
            }
        }
        return null;
    }

    public void addEdge(String node1, String node2) {
        if (isDirected()) {
            addDirectedEdge(node1, node2);
        } else {
            addUndirectedEdge(node1, node2);
        }
    }

    public void addEdge(String node1, String node2, double weight) {
        if (isDirected()) {
            addDirectedEdge(node1, node2, weight);
        } else {
            addUndirectedEdge(node1, node2, weight);
        }
    }

    public void addEdge(Node node1, Node node2) {
        if (isDirected()) {
            addDirectedEdge(node1, node2);
        } else {
            addUndirectedEdge(node1, node2);
        }
    }

    public void addEdge(Node node1, Node node2, double weight) {
        if (isDirected()) {
            addDirectedEdge(node1, node2, weight);
        } else {
            addUndirectedEdge(node1, node2, weight);
        }
    }

    public void addUndirectedEdge(String node1, String node2) {
        Node n1, n2;
        if (hasNode(node1)) {
            n1 = getNode(node1);
        } else {
            n1 = new Node(node1);
        }
        if (hasNode(node2)) {
            n2 = getNode(node2);
        } else {
            n2 = new Node(node2);
        }
        addUndirectedEdge(n1, n2);
    }

    public void addUndirectedEdge(String node1, String node2, double weight) {
        Node n1, n2;
        if (hasNode(node1)) {
            n1 = getNode(node1);
        } else {
            n1 = new Node(node1);
        }
        if (hasNode(node2)) {
            n2 = getNode(node2);
        } else {
            n2 = new Node(node2);
        }
        addUndirectedEdge(n1, n2, weight);
    }

    public void addUndirectedEdge(Node node1, Node node2) {
        if (!hasNode(node1)) {
            addNode(node1);
        }
        if (!hasNode(node2)) {
            addNode(node2);
        }
        node1.addEdge(node2);
        node2.addEdge(node1);
        edgeList.add(node1.getEdge(node2));
    }

    public void addUndirectedEdge(Node node1, Node node2, double weight) {
        if (!hasNode(node1)) {
            addNode(node1);
        }
        if (!hasNode(node2)) {
            addNode(node2);
        }
        node1.addEdge(node2, weight);
        node2.addEdge(node1, weight);
        edgeList.add(node1.getEdge(node2));
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
        edgeList.add(source.getEdge(destination));
    }

    public void addDirectedEdge(Node source, Node destination, double weight) {
        if (!hasNode(source)) {
            addNode(source);
        }
        if (!hasNode(destination)) {
            addNode(destination);
        }
        source.addEdge(destination, weight);
        edgeList.add(source.getEdge(destination));
    }

    public void removeEdge(String source, String destination) {
        if (hasNode(source) && hasNode(destination)) {
            Node sourceNode = getNode(source);
            Node destinationNode = getNode(destination);
            sourceNode.removeEdge(destinationNode);
            if (!isDirected()) {
                destinationNode.removeEdge(sourceNode);
            }
            edgeList.removeIf((e) -> {
                return e.getSource() == sourceNode && e.getDestination() == destinationNode;
            });
        }
    }

    public void removeEdge(Node source, Node destination) {
        if (hasNode(source) && hasNode(destination)) {
            source.removeEdge(destination);
            if (!isDirected()) {
                destination.removeEdge(source);
            }
            edgeList.removeIf((e) -> {
                return e.getSource() == source && e.getDestination() == destination;
            });
        }
    }

    public void removeEdge(Edge edge) {
        if (hasEdge(edge)) {
            edgeList.remove(edge);
            edge.getSource().removeEdge(edge.getDestination());
            if (!isDirected()) {
                edge.getDestination().removeEdge(edge.getSource());
            }
        }
    }

    public void clear() {
        nodeList.clear();
    }

    public boolean isEmpty() {
        return nodeList.isEmpty();
    }

    public int size() {
        return nodeList.size();
    }

    public boolean hasNode(String nodeName) {
        return getNode(nodeName) != null;
    }

    public boolean hasNode(Node node) {
        return nodeList.contains(node);
    }

    public boolean hasEdge(String source, String destination) {
        Node sourceNode = getNode(source);
        Node destinationNode = getNode(destination);
        if (sourceNode != null && destination != null) {
            return sourceNode.isNodeAdjacent(destinationNode);
        }
        return false;
    }

    public boolean hasEdge(Node source, Node destination) {
        return nodeList.contains(source) && source.isNodeAdjacent(destination);
    }

    public boolean hasEdge(Edge edge) {
        return edgeList.contains(edge);
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        if (directed != this.directed) {
            if (directed)
                convertToDirected();
            else
                convertToUndirected();
        }
        this.directed = directed;
    }

    private void convertToDirected() {
        ArrayList<Edge> edgeToRemove = new ArrayList<>();
        for (Node node : nodeList) {
            for (Edge edge : node.getAdjacencyList()) {
                if (!edgeList.contains(edge)) {
                    edgeToRemove.add(edge);
                }
            }
        }
        for (Edge edge : edgeToRemove) {
            edge.getSource().removeEdge(edge.getDestination());
        }
    }

    private void convertToUndirected() {
        for (Node node : nodeList) {
            for (Edge edge : node.getAdjacencyList()) {
                if (!hasEdge(edge.getDestination(), edge.getSource())) {
                    edge.getDestination().addEdge(edge.getSource());
                }
            }
        }
    }
}
