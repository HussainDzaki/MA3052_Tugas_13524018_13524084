package ma3052.core.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javafx.util.Pair;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class TravellingSalesman {
    public static List<Node> getHamiltonianCycle(Graph graph) {
        if (graph == null) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        getHamiltonianCycleDFS(graph.getNodeList().getFirst(), graph, path, visitedNodes);

        return new ArrayList<>(path.reversed());
    }

    private static boolean getHamiltonianCycleDFS(Node currentNode, Graph graph, Deque<Node> path,
            Set<Node> visitedNodes) {
        visitedNodes.add(currentNode);
        path.push(currentNode);
        if (visitedNodes.size() == graph.size()) {
            if (currentNode.isNodeAdjacent(path.peekLast())) {
                return true;
            }
        } else {
            List<Edge> edges = new ArrayList<>(currentNode.getAdjacencyList());
            edges.sort((e1, e2) -> (int) (e1.getWeight() - e2.getWeight()));
            for (Edge edge : edges) {
                if (visitedNodes.contains(edge.getDestination()))
                    continue;
                if (getHamiltonianCycleDFS(edge.getDestination(), graph, path, visitedNodes)) {
                    return true;
                }
            }
        }
        visitedNodes.remove(currentNode);
        path.pop();
        return false;
    }

    public static List<Node> solve(Graph graph) {
        ArrayList<Node> cycle = new ArrayList<>(getHamiltonianCycle(graph));
        if (cycle.isEmpty()) {
            return null;
        }

        boolean hasChanges = true;
        do {
            hasChanges = false;
            for (int i = 0; i < cycle.size(); i++) {
                for (int j = i + 2; j < cycle.size(); j++) {
                    Node node1 = cycle.get(i);
                    Node node2 = cycle.get(i + 1);
                    Node node3 = cycle.get(j);
                    Node node4 = cycle.get((j + 1) % cycle.size());
                    Edge edge1 = node1.getEdge(node2);
                    Edge edge2 = node3.getEdge(node4);
                    Edge edge3 = node1.getEdge(node3);
                    Edge edge4 = node2.getEdge(node4);
                    if (edge1 == null || edge2 == null || edge3 == null || edge4 == null)
                        continue;
                    if (edge1.getWeight() + edge2.getWeight() > edge3.getWeight() + edge4.getWeight()) {
                        for (int k = 1; k <= (j - i) / 2; k++) {
                            Node temp = cycle.get(i + k);
                            cycle.set(i + k, cycle.get(j - k + 1));
                            cycle.set(j - k + 1, temp);
                        }
                        hasChanges = true;
                    }
                }
            }
        } while (hasChanges);

        return cycle;
    }
}