package ma3052.graph;

public class Edge {
    public static final double DEFAULT_WEIGHT = 1;

    private Node source;
    private Node destination;
    private double weight;

    public Edge(Node source, Node destination) {
        this.source = source;
        this.destination = destination;
        this.weight = DEFAULT_WEIGHT;
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
