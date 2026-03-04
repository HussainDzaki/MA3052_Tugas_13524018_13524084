package ma3052.graph;

public class Edge {
    private Node source;
    private Node destination;
    private double weight;

    public Edge(Node source, Node destination) {
        this.source = source;
        this.destination = destination;
        this.weight = 1;
    }

    public Edge(Node source, Node destination, double value) {
        this.source = source;
        this.destination = destination;
        this.weight = value;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double value) {
        this.weight = value;
    }
}
