package ma3052.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class GraphTraversal {
    public static List<Node> traversalOrderBFS(Graph graph, String startNodeName) {
        return traversalOrderBFS(graph, graph.getNode(startNodeName));
    }

    public static List<Node> traversalOrderBFS(Graph graph, Node startNode) {
        if (!graph.hasNode(startNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.add(nextNode);
            }
        }

        return nodeOrder;
    }

    public static List<Node> traversalOrderDFS(Graph graph, String startNodeName) {
        return traversalOrderDFS(graph, graph.getNode(startNodeName));
    }

    public static List<Node> traversalOrderDFS(Graph graph, Node startNode) {
        if (!graph.hasNode(startNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.push(nextNode);
            }
        }

        return nodeOrder;
    }
}
