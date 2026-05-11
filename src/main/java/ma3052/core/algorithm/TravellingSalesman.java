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
import ma3052.core.graph.PointGraph;

public class TravellingSalesman {
    public static List<Node> getHamiltonianCycle(Graph graph) {
        if (graph == null) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        getHamiltonianCycleDFS(graph.getNodeList().get((int) Math.floor(Math.random() * graph.size())),
                graph, path, visitedNodes);

        return new ArrayList<>(path.reversed());
    }

    public static List<Node> getHamiltonianCycle(Graph graph, Node startNode) {
        if (graph == null || !graph.hasNode(startNode)) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        getHamiltonianCycleDFS(startNode, graph, path, visitedNodes);

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

    public static List<Node> getHamiltonianCycle(PointGraph graph) {
        if (graph == null) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        getHamiltonianCycleDFS(graph.getNodeList().get((int) Math.floor(Math.random() * graph.size())),
                graph, path, visitedNodes);

        return new ArrayList<>(path.reversed());
    }

    public static List<Node> getHamiltonianCycle(PointGraph graph, Node startNode) {
        if (graph == null || !graph.hasNode(startNode)) {
            return null;
        }

        Set<Node> visitedNodes = new HashSet<Node>();
        Deque<Node> path = new ArrayDeque<Node>();
        getHamiltonianCycleDFS(startNode, graph, path, visitedNodes);

        return new ArrayList<>(path.reversed());
    }

    private static boolean getHamiltonianCycleDFS(Node currentNode, PointGraph graph, Deque<Node> path,
            Set<Node> visitedNodes) {
        visitedNodes.add(currentNode);
        path.push(currentNode);
        if (visitedNodes.size() == graph.size()) {
            return true;
        } else {
            List<Node> nodes = new ArrayList<>(graph.getNodeList());
            nodes.sort((n1, n2) -> {
                if (graph.getDistance(currentNode, n1) > graph.getDistance(currentNode, n2)) {
                    return 1;
                } else if (graph.getDistance(currentNode, n1) < graph.getDistance(currentNode, n2)) {
                    return -1;
                } else {
                    return 0;
                }
            });
            for (Node nextNode : nodes) {
                if (visitedNodes.contains(nextNode))
                    continue;
                if (getHamiltonianCycleDFS(nextNode, graph, path, visitedNodes)) {
                    return true;
                }
            }
        }
        visitedNodes.remove(currentNode);
        path.pop();
        return false;
    }

    public static List<Node> solve(Graph graph, Node startNode) {
        ArrayList<Node> cycle = new ArrayList<>(getHamiltonianCycle(graph, startNode));
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

    public static List<Node> solve(PointGraph graph) {
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
                    double distance12 = graph.getDistance(node1, node2);
                    double distance34 = graph.getDistance(node3, node4);
                    double distance13 = graph.getDistance(node1, node3);
                    double distance24 = graph.getDistance(node2, node4);
                    if (distance12 + distance34 > distance13 + distance24) {
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

    public static List<Node> solve(PointGraph graph, Node startNode) {
        ArrayList<Node> cycle = new ArrayList<>(getHamiltonianCycle(graph, startNode));
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
                    double distance12 = graph.getDistance(node1, node2);
                    double distance34 = graph.getDistance(node3, node4);
                    double distance13 = graph.getDistance(node1, node3);
                    double distance24 = graph.getDistance(node2, node4);
                    if (distance12 + distance34 > distance13 + distance24) {
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

    public static List<Node> solveBest(Graph graph) {
        List<Node> bestCycle = null;
        double bestTotalCost = 0;
        for (Node node : graph.getNodeList()) {
            List<Node> currentCycle = solve(graph, node);

            double totalCost = 0;
            for (int i = 0; i < currentCycle.size(); i++) {
                totalCost += graph.getEdge(currentCycle.get(i), currentCycle.get((i + 1) % currentCycle.size()))
                        .getWeight();
            }

            if (bestCycle == null || bestTotalCost > totalCost) {
                bestCycle = currentCycle;
                bestTotalCost = totalCost;
            }
        }
        return bestCycle;
    }

    public static List<Node> solveBest(PointGraph graph) {
        List<Node> bestCycle = null;
        double bestTotalCost = 0;
        for (Node node : graph.getNodeList()) {
            List<Node> currentCycle = solve(graph, node);

            double totalCost = 0;
            for (int i = 0; i < currentCycle.size(); i++) {
                totalCost += graph.getDistance(currentCycle.get(i), currentCycle.get((i + 1) % currentCycle.size()));
            }

            if (bestCycle == null || bestTotalCost > totalCost) {
                bestCycle = currentCycle;
                bestTotalCost = totalCost;
            }
        }
        return bestCycle;
    }

}