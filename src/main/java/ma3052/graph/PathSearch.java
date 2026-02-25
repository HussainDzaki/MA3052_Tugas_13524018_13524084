package ma3052.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class PathSearch {
    public static List<Node> searchPathBFS(Graph graph, Node startNode, Node endNode) {
        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID())) continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            if (currentNode == endNode) {
                break;
            }

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.add(nextNode);
            }
        }

        if (nodeOrder.getLast() == endNode) {
            return nodeOrder;
        } else {
            return null;
        }
    }

    public static List<Node> searchPathDFS(Graph graph, Node startNode, Node endNode) {
        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID())) continue;
            visitedNodes.add(currentNode.getNodeID());
            nodeOrder.add(currentNode);

            if (currentNode == endNode) {
                break;
            }

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.push(nextNode);
            }
        }

        if (nodeOrder.getLast() == endNode) {
            return nodeOrder;
        } else {
            return null;
        }
    }

    public static boolean hasPathBFS(Graph graph, Node startNode, Node endNode) {
        boolean hasPath = false;

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID())) continue;
            visitedNodes.add(currentNode.getNodeID());

            if (currentNode == endNode) {
                hasPath = true;
                break;
            }

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.add(nextNode);
            }
        }

        return hasPath;
    }

    public static boolean hasPathDFS(Graph graph, Node startNode, Node endNode) {
        boolean hasPath = false;

        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID())) continue;
            visitedNodes.add(currentNode.getNodeID());

            if (currentNode == endNode) {
                hasPath = true;
                break;
            }

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.push(nextNode);
            }
        }

        return hasPath;
    }
}
