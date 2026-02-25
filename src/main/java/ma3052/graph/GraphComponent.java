package ma3052.graph;

import java.util.HashSet;
import java.util.LinkedList;

public class GraphComponent {
    public static boolean isOneComponent(Graph graph) {
        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        queue.add(graph.getNodeList().getFirst());
        while (!queue.isEmpty()) {
            Node currentNode = queue.pop();
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
        int totalComponent = 0;
        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        for (Node node : graph.getNodeList()) {
            if (visitedNodes.contains(node.getNodeID()))
                continue;
            totalComponent++;
            queue.add(node);
            while (!queue.isEmpty()) {
                Node currentNode = queue.pop();
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
        int biggestComponentSize = 0;
        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        for (Node node : graph.getNodeList()) {
            if (visitedNodes.contains(node.getNodeID()))
                continue;

            int currentComponentSize = 0;
            queue.add(node);
            while (!queue.isEmpty()) {
                Node currentNode = queue.pop();
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
