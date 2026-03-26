package ma3052.graph;

import java.util.HashSet;
import java.util.PriorityQueue;

public class PrimsAlgorithm {
    public static Graph getMinimumSpanningTree(Graph graph) {
        Graph resultGraph = new Graph();
        for (Node node : graph.getNodeList()) {
            resultGraph.addNode(new Node(node.getNodeName(), node.getValue()));
        }

        PriorityQueue<Edge> primsQueue = new PriorityQueue<>((e1, e2) -> {
            if (e1.getWeight() < e2.getWeight())
                return -1;
            if (e1.getWeight() > e2.getWeight())
                return 1;
            return 0;
        });

        HashSet<Node> visitedNode = new HashSet<>();
        Node firstNode = graph.getNodeList().getFirst();
        primsQueue.add(new Edge(null, firstNode));
        while (!primsQueue.isEmpty()) {
            Edge currentEdge = primsQueue.poll();
            Node currentNode = currentEdge.getDestination();
            if (visitedNode.contains(currentNode))
                continue;
            visitedNode.add(currentNode);
            if (currentEdge.getSource() != null) { // Is not the first node
                resultGraph.addEdge(currentEdge.getSource().getNodeName(), currentEdge.getDestination().getNodeName());
            }
            for (Edge edge : currentNode.getAdjacencyList()) {
                Node nextNode = edge.getDestination();
                if (visitedNode.contains(nextNode))
                    continue;
                primsQueue.add(edge);
            }
        }

        return resultGraph;
    }
}
