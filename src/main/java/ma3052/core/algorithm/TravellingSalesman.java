package ma3052.core.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
    private static List<Node> getHamiltonianCycle(Graph graph) {
        if (graph == null) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        visitedNodes.add(graph.getNodeList().getFirst());
        path.push(graph.getNodeList().getFirst());
        getHamiltonianCycleDFS(graph, path, visitedNodes);

        return new ArrayList<>(path);
    }

    private static void getHamiltonianCycleDFS(Graph graph, Deque<Node> path, Set<Node> visitedNodes) {
        Node currentNode = path.peek();
        for (Edge edge : currentNode.getAdjacencyList()) {
            if (visitedNodes.contains(edge.getDestination()))
                continue;
            visitedNodes.add(edge.getDestination());
            path.add(edge.getDestination());
            if (path.size() == graph.size()) {
                if (path.getLast().getEdge(path.getFirst()) != null) {
                    return;
                }
            }
            else {
                getHamiltonianCycleDFS(graph, path, visitedNodes);
            }
            visitedNodes.remove(edge.getDestination());
            path.remove(edge.getDestination());
        }
    }

    public static List<Node> solve(Graph graph) {
        ArrayList<Node> hamiltonianPath = new ArrayList<>(getHamiltonianCycle(graph));
        if (hamiltonianPath.isEmpty()) {
            return null;
        }
        
        ArrayList<Pair<Node, Node>> cycle = new ArrayList<>();
        for (int i = 0; i < hamiltonianPath.size(); i++) {
            cycle.add(new Pair<Node,Node>(hamiltonianPath.get(i), hamiltonianPath.get((i + 1) % hamiltonianPath.size())));
        }

        boolean hasChanges = true;
        do {
            hasChanges = false;
            for (int i = 0; i < cycle.size(); i++) {
                for (int j = i + 1; j < cycle.size(); j++) {
                    Node node1 = cycle.get(i).getKey();
                    Node node2 = cycle.get(i).getValue();
                    Node node3 = cycle.get(j).getKey();
                    Node node4 = cycle.get(j).getValue();
                    if (node1 == node4 || node2 == node3) continue;

                    Edge edge1 = node1.getEdge(node2);
                    Edge edge2 = node1.getEdge(node3);
                    Edge edge3 = node3.getEdge(node4);
                    Edge edge4 = node2.getEdge(node4);
                    if (edge1 == null || edge2 == null || edge3 == null || edge4 == null) continue;
                    if (edge1.getWeight() + edge3.getWeight() > edge2.getWeight() + edge4.getWeight()) {
                        cycle.set(i, new Pair<Node, Node>(node1, node3));
                        cycle.set(j, new Pair<Node, Node>(node2, node4));
                        hasChanges = true;
                    }
                }
            }
        } while (hasChanges);

        List<Node> result = new ArrayList<>(cycle.size());
        for (Pair<Node, Node> pair : cycle) {
            result.add(pair.getKey());
        }
        return result;
    }
}
