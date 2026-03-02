package ma3052.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PathSearch {
    public static List<Node> searchPathBFS(Graph graph, String startNodeName, String endNodeName) {
        if (graph == null) {
            return null;
        } else {
            return searchPathBFS(graph, graph.getNode(startNodeName), graph.getNode(endNodeName));
        }
    }

    public static List<Node> searchPathBFS(Graph graph, Node startNode, Node endNode) {
        // If start node or end node is nowhere in the graph
        if (graph == null || !graph.hasNode(startNode) || !graph.hasNode(endNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
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

        if (nodeOrder.get(nodeOrder.size() - 1) == endNode) {
            return nodeOrder;
        } else {
            return null;
        }
    }

    public static List<Node> searchPathDFS(Graph graph, String startNodeName, String endNodeName) {
        if (graph == null) {
            return null;
        } else {
            return searchPathDFS(graph, graph.getNode(startNodeName), graph.getNode(endNodeName));
        }
    }

    public static List<Node> searchPathDFS(Graph graph, Node startNode, Node endNode) {
        // If start node or end node is nowhere in the graph
        if (graph == null || !graph.hasNode(startNode) || !graph.hasNode(endNode)) {
            return null;
        }

        ArrayList<Node> nodeOrder = new ArrayList<Node>(graph.size());

        HashSet<Long> visitedNodes = new HashSet<>();
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
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

        if (nodeOrder.get(nodeOrder.size() - 1) == endNode) {
            return nodeOrder;
        } else {
            return null;
        }
    }

    public static boolean hasPathBFS(Graph graph, String startNodeName, String endNodeName) {
        if (graph == null) {
            return false;
        } else {
            return hasPathBFS(graph, graph.getNode(startNodeName), graph.getNode(endNodeName));
        }
    }

    public static boolean hasPathBFS(Graph graph, Node startNode, Node endNode) {
        // If start node or end node is nowhere in the graph
        if (graph == null || !graph.hasNode(startNode) || !graph.hasNode(endNode)) {
            return false;
        }

        boolean hasPath = false;

        HashSet<Long> visitedNodes = new HashSet<>();
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
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

    public static boolean hasPathDFS(Graph graph, String startNodeName, String endNodeName) {
        if (graph == null) {
            return false;
        } else {
            return hasPathDFS(graph, graph.getNode(startNodeName), graph.getNode(endNodeName));
        }
    }

    public static boolean hasPathDFS(Graph graph, Node startNode, Node endNode) {
        // If start node or end node is nowhere in the graph
        if (graph == null || !graph.hasNode(startNode) || !graph.hasNode(endNode)) {
            return false;
        }

        boolean hasPath = false;

        HashSet<Long> visitedNodes = new HashSet<>();
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.push(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
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
