package ma3052.graph;

import java.util.HashSet;
import java.util.LinkedList;

public class Island {
    public static int getTotalIsland(GridGraph graph, char landChar) {
        if (graph == null) {
            return 0;
        }

        int islandCount = 0;
        HashSet<Long> visitedNodes = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<Node>();
        for (int i = 0; i < graph.getRowSize(); i++) {
            for (int j = 0; j < graph.getColSize(); j++) {
                Node node = graph.getNode(i, j);
                if (graph.getNodeType(node) != landChar)
                    continue;
                if (visitedNodes.contains(node.getNodeID()))
                    continue;

                islandCount++;
                queue.add(node);
                while (!queue.isEmpty()) {
                    Node currentNode = queue.pop();
                    if (visitedNodes.contains(currentNode.getNodeID()))
                        continue;
                    visitedNodes.add(currentNode.getNodeID());

                    for (Node nextNode : currentNode.getAdjacencyList()) {
                        if (graph.getNodeType(node) != landChar)
                            continue;
                        if (visitedNodes.contains(nextNode.getNodeID()))
                            continue;
                        queue.add(nextNode);
                    }
                }
            }
        }

        return islandCount;
    }
}
