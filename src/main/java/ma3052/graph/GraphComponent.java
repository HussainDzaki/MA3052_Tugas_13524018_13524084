package ma3052.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

public class GraphComponent {
    public static boolean isOneComponent(Graph graph) {
        if (graph == null) {
            return false;
        }

        HashSet<Long> visitedNodes = new HashSet<>();
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(graph.getNodeList().get(0));
        while (!queue.isEmpty()) {
            Node currentNode = queue.remove();
            if (visitedNodes.contains(currentNode.getNodeID()))
                continue;
            visitedNodes.add(currentNode.getNodeID());

            for (Node nextNode : currentNode.getAdjacencyList()) {
                if (visitedNodes.contains(nextNode.getNodeID()))
                    continue;
                queue.add(nextNode);
            }
        }
        return visitedNodes.size() == graph.size();
    }

    public static int getTotalComponent(Graph graph) {
        if (graph == null) {
            return 0;
        }

        int totalComponent = 0;
        HashSet<Long> visitedNodes = new HashSet<>();
        Queue<Node> queue = new LinkedList<Node>();
        for (Node node : graph.getNodeList()) {
            if (visitedNodes.contains(node.getNodeID()))
                continue;
            totalComponent++;
            queue.add(node);
            while (!queue.isEmpty()) {
                Node currentNode = queue.remove();
                if (visitedNodes.contains(currentNode.getNodeID()))
                    continue;
                visitedNodes.add(currentNode.getNodeID());

                for (Node nextNode : currentNode.getAdjacencyList()) {
                    if (visitedNodes.contains(nextNode.getNodeID()))
                        continue;
                    queue.add(nextNode);
                }
            }
        }
        return totalComponent;
    }

    public static int getBiggestComponentSize(Graph graph) {
        if (graph == null) {
            return 0;
        }

        int biggestComponentSize = 0;
        HashSet<Long> visitedNodes = new HashSet<>();
        Queue<Node> queue = new LinkedList<Node>();
        for (Node node : graph.getNodeList()) {
            if (visitedNodes.contains(node.getNodeID()))
                continue;

            int currentComponentSize = 0;
            queue.add(node);
            while (!queue.isEmpty()) {
                Node currentNode = queue.remove();
                if (visitedNodes.contains(currentNode.getNodeID()))
                    continue;
                visitedNodes.add(currentNode.getNodeID());
                currentComponentSize++;

                for (Node nextNode : currentNode.getAdjacencyList()) {
                    if (visitedNodes.contains(nextNode.getNodeID()))
                        continue;
                    queue.add(nextNode);
                }
            }
            biggestComponentSize = Math.max(currentComponentSize, biggestComponentSize);
        }
        return biggestComponentSize;
    }
}
