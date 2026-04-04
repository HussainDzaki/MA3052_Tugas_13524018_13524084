package ma3052.core.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class GraphTraversal {
    public static List<Node> traversalOrderBFS(Graph graph, String startNodeName) {
        if (graph == null) {
            return null;
        } else {
            return traversalOrderBFS(graph, graph.getNode(startNodeName));
        }
    }

    public static List<Node> traversalOrderBFS(Graph graph, Node startNode) {
        if (graph == null || !graph.hasNode(startNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.remove();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            for (Edge edge : currentNode.getAdjacencyList()) {
                Node nextNode = edge.getDestination();
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.add(nextNode);
            }
        }

        return nodeOrder;
    }

    public static List<Node> traversalOrderDFS(Graph graph, String startNodeName) {
        if (graph == null) {
            return null;
        } else {
            return traversalOrderDFS(graph, graph.getNode(startNodeName));
        }
    }

    public static List<Node> traversalOrderDFS(Graph graph, Node startNode) {
        if (graph == null || !graph.hasNode(startNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        Deque<Node> queue = new LinkedList<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            for (Edge edge : currentNode.getAdjacencyList()) {
                Node nextNode = edge.getDestination();
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.push(nextNode);
            }
        }

        return nodeOrder;
    }
}
