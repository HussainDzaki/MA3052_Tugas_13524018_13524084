package ma3052.gui.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.core.algorithm.CycleDetector;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.EdgeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class BipartiteMatchingAnimation {
    static final int INF = Integer.MAX_VALUE;
    static final int NIL = 0;

    private static volatile long animationStepTime = 500; // in milliseconds
    private int m, n;
    private List<Integer>[] adj;
    private int[] pairU, pairV, dist;
    private Map<Integer, Node> indexToNodeU;
    private Map<Integer, Node> indexToNodeV;
    private Map<Node, Integer> nodeToIndexU;
    private Map<Node, Integer> nodeToIndexV;
    private GraphGUI graphGUI;
    private Graph graph;
    private Set<String> exploredEdges; // Track explored edges for visualization

    private static final Color MATCHED_COLOR = Color.web("#ff3b83");
    private static final Color MATCHED_COLOR_EDGE = Color.web("rgb(255, 40, 6)");
    private static final Color BFS_EXPLORE_COLOR = Color.web("#FFE66D"); // Yellow for BFS exploration
    private static final Color DFS_EXPLORE_COLOR = Color.web("#1c9b4f"); // Teal for DFS exploration
    private static final Color AUGMENTING_PATH_COLOR = Color.web("#78cdfe"); // Light blue for augmenting path

    // Predefined color palette for different periods
    private static final Color[] COLOR_PALETTE = {
            Color.web("#FF6B6B"), // Red
            Color.web("#10ab41"), // Yellow
            Color.web("#bf4616"), // Light Salmon
            Color.web("#1e4193"), // Teal
            Color.web("#45B7D1"), // Blue
            Color.web("#0b8e6d"), // Mint
            Color.web("#9d3ac7"), // Purple
            Color.web("#9bdcff"), // Sky Blue
            Color.web("#ff9950"), // Peach
            Color.web("#52C77D"), // Green
    };

    public static void setAnimationStepTime(long animationStepTime) {
        BipartiteMatchingAnimation.animationStepTime = Math.max(1, animationStepTime);
    }

    /**
     * Generate a distinct color based on input color index
     * Uses a color palette and generates different hues for colors beyond the
     * palette
     */
    private static Color getColorByPeriod(int color) {
        if (color <= 0)
            color = 1;

        // Use predefined palette if within range
        if (color <= COLOR_PALETTE.length) {
            return COLOR_PALETTE[color - 1];
        }

        // Generate colors dynamically using HSB for colors beyond palette
        int index = color - COLOR_PALETTE.length - 1;
        double hue = (index * 36.0) % 360.0;
        double saturation = 0.7 + ((index % 3) * 0.1);
        double brightness = 0.8 + ((index % 2) * 0.1);

        return Color.hsb(hue, saturation, brightness);
    }

    // Constructor
    public BipartiteMatchingAnimation(GraphGUI graphGUI) {
        this.graphGUI = graphGUI;
        this.graph = graphGUI.getGraph();
        this.indexToNodeU = new HashMap<>();
        this.indexToNodeV = new HashMap<>();
        this.nodeToIndexU = new HashMap<>();
        this.nodeToIndexV = new HashMap<>();
        this.exploredEdges = new HashSet<>();
    }

    public static void animateHopCroftKarp(GraphGUI graphGUI) {
        new Thread(() -> {
            BipartiteMatchingAnimation animation = new BipartiteMatchingAnimation(graphGUI);
            animation.runHopcroftKarp();
        }).start();
    }

    public static void animateHopCroftKarpTimeTabling(GraphGUI graphGUI) {
        new Thread(() -> {
            BipartiteMatchingAnimation animation = new BipartiteMatchingAnimation(graphGUI);
            animation.runtimeTablingWithHopcroftKarp();
        }).start();
    }

    // Main algorithm execution with animation
    private void runHopcroftKarp() {
        List<Node> nodes = graph.getNodeList();

        // Check if graph is bipartite
        if (!CycleDetector.isBipartite(graph)) {
            logMessage("Graph is not bipartite! Cannot perform matching.");
            return;
        }

        // Use BFS coloring to partition nodes into U and V sets
        Map<Node, Integer> nodeColor = new HashMap<>();
        List<Node> setU = new ArrayList<>();
        List<Node> setV = new ArrayList<>();

        // BFS coloring from each unvisited node
        for (Node startNode : nodes) {
            if (!nodeColor.containsKey(startNode)) {
                Queue<Node> queue = new LinkedList<>();
                queue.add(startNode);
                nodeColor.put(startNode, 0);

                while (!queue.isEmpty()) {
                    Node current = queue.poll();
                    int currentColor = nodeColor.get(current);
                    int nextColor = 1 - currentColor; // Toggle between 0 and 1

                    // Get all neighbors of current node
                    for (Edge edge : graph.getEdgeList()) {
                        Node neighbor = null;
                        if (edge.getSource().equals(current)) {
                            neighbor = edge.getDestination();
                        } else if (edge.getDestination().equals(current)) {
                            neighbor = edge.getSource();
                        }

                        if (neighbor != null && !nodeColor.containsKey(neighbor)) {
                            nodeColor.put(neighbor, nextColor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Partition nodes into U and V based on color
        for (Node node : nodes) {
            if (nodeColor.getOrDefault(node, 0) == 0) {
                setU.add(node);
            } else {
                setV.add(node);
            }
        }

        m = setU.size();
        n = setV.size();

        // Initialize matching arrays
        adj = new ArrayList[m + 1];
        for (int i = 0; i <= m; i++) {
            adj[i] = new ArrayList<>();
        }

        pairU = new int[m + 1];
        pairV = new int[n + 1];
        dist = new int[m + 1];

        Arrays.fill(pairU, NIL);
        Arrays.fill(pairV, NIL);

        // Map nodes to partition indices
        for (int i = 0; i < setU.size(); i++) {
            nodeToIndexU.put(setU.get(i), i + 1);
            indexToNodeU.put(i + 1, setU.get(i));
        }

        for (int i = 0; i < setV.size(); i++) {
            nodeToIndexV.put(setV.get(i), i + 1);
            indexToNodeV.put(i + 1, setV.get(i));
        }

        // Build adjacency list from edges
        for (Edge edge : graph.getEdgeList()) {
            Node source = edge.getSource();
            Node dest = edge.getDestination();

            Integer uIdx = nodeToIndexU.get(source);
            Integer vIdx = nodeToIndexV.get(dest);

            // One must be in U, one must be in V
            if (uIdx != null && vIdx != null) {
                // source is in U, dest is in V
                adj[uIdx].add(vIdx);
            } else {
                // Try the other direction: dest in U, source in V
                uIdx = nodeToIndexU.get(dest);
                vIdx = nodeToIndexV.get(source);
                if (uIdx != null && vIdx != null) {
                    adj[uIdx].add(vIdx);
                }
            }
        }

        // Run matching algorithm WITH intermediate visualization
        int matchingCount = 0;
        while (bfs()) {
            for (int u = 1; u <= m; u++) {
                if (pairU[u] == NIL && dfs(u)) {
                    matchingCount++;
                    // Highlight the augmenting path found
                    highlightAugmentingPath(u);
                    sleep(animationStepTime);
                    // Clear exploration colors for next iteration
                    clearExplorationColors();
                    exploredEdges.clear();
                }
            }
        }

        logMessage("══════════════════");
        logMessage("Hopcroft-Karp Algorithm Completed!");
        logMessage("Total Maximum Matching: " + matchingCount);
        logMessage("══════════════════");

        // Display final matching result visually and in log
        sleep(500);
        for (int u = 1; u <= m; u++) {
            if (pairU[u] != NIL) {
                Node nodeU = indexToNodeU.get(u);
                Node nodeV = indexToNodeV.get(pairU[u]);
                if (nodeU != null && nodeV != null) {
                    // Display edge with matched color
                    Platform.runLater(() -> {
                        EdgeGUI edgeGUI = graphGUI.getEdgeGUI(nodeU, nodeV);
                        if (edgeGUI != null) {
                            edgeGUI.setLineColor(MATCHED_COLOR_EDGE);
                        }
                    });
                    sleep(animationStepTime / 2);
                    logMessage("Match: " + nodeU.getNodeName() + " - " + nodeV.getNodeName());
                }
            }
        }
    }

    private void runtimeTablingWithHopcroftKarp() {
        List<Node> nodes = graph.getNodeList();

        // Check if graph is bipartite
        if (!CycleDetector.isBipartite(graph)) {
            logMessage("Graph is not bipartite! Cannot perform time tabling.");
            return;
        }

        // Use BFS coloring to partition nodes into U and V sets once
        Map<Node, Integer> nodeColor = new HashMap<>();
        List<Node> setU = new ArrayList<>();
        List<Node> setV = new ArrayList<>();

        // BFS coloring from each unvisited node
        for (Node startNode : nodes) {
            if (!nodeColor.containsKey(startNode)) {
                Queue<Node> queue = new LinkedList<>();
                queue.add(startNode);
                nodeColor.put(startNode, 0);

                while (!queue.isEmpty()) {
                    Node current = queue.poll();
                    int currentColor = nodeColor.get(current);
                    int nextColor = 1 - currentColor;

                    for (Edge edge : graph.getEdgeList()) {
                        Node neighbor = null;
                        if (edge.getSource().equals(current)) {
                            neighbor = edge.getDestination();
                        } else if (edge.getDestination().equals(current)) {
                            neighbor = edge.getSource();
                        }

                        if (neighbor != null && !nodeColor.containsKey(neighbor)) {
                            nodeColor.put(neighbor, nextColor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // Partition nodes into U and V
        for (Node node : nodes) {
            if (nodeColor.getOrDefault(node, 0) == 0) {
                setU.add(node);
            } else {
                setV.add(node);
            }
        }

        m = setU.size();
        n = setV.size();

        // Map nodes to indices once
        for (int i = 0; i < setU.size(); i++) {
            nodeToIndexU.put(setU.get(i), i + 1);
            indexToNodeU.put(i + 1, setU.get(i));
        }

        for (int i = 0; i < setV.size(); i++) {
            nodeToIndexV.put(setV.get(i), i + 1);
            indexToNodeV.put(i + 1, setV.get(i));
        }

        // Create a working copy of the graph edges
        List<Edge> remainingEdges = new ArrayList<>(graph.getEdgeList());
        int color = 1;
        Map<Edge, Integer> finalSchedule = new HashMap<>();

        logMessage("══════════════════");
        logMessage("Time Tabling with Hopcroft-Karp");
        logMessage("══════════════════");

        while (!remainingEdges.isEmpty()) {
            // Reinitialize matching arrays for this iteration
            adj = new ArrayList[m + 1];
            for (int i = 0; i <= m; i++) {
                adj[i] = new ArrayList<>();
            }

            pairU = new int[m + 1];
            pairV = new int[n + 1];
            dist = new int[m + 1];

            Arrays.fill(pairU, NIL);
            Arrays.fill(pairV, NIL);

            // Build adjacency list from remaining edges only
            for (Edge edge : remainingEdges) {
                Node source = edge.getSource();
                Node dest = edge.getDestination();

                Integer uIdx = nodeToIndexU.get(source);
                Integer vIdx = nodeToIndexV.get(dest);

                // One must be in U, one must be in V
                if (uIdx != null && vIdx != null) {
                    // source is in U, dest is in V
                    adj[uIdx].add(vIdx);
                } else {
                    // Try the other direction: dest in U, source in V
                    uIdx = nodeToIndexU.get(dest);
                    vIdx = nodeToIndexV.get(source);
                    if (uIdx != null && vIdx != null) {
                        adj[uIdx].add(vIdx);
                    }
                }
            }

            // Find maximum matching for this period
            List<Edge> matchedEdges = new ArrayList<>();
            boolean foundAugmentingPath = true;

            while (foundAugmentingPath) {
                foundAugmentingPath = false;
                if (!bfs()) {
                    break; // No more augmenting paths available
                }

                for (int u = 1; u <= m; u++) {
                    if (pairU[u] == NIL && dfs(u)) {
                        foundAugmentingPath = true;
                        Node nodeU = indexToNodeU.get(u);
                        Node nodeV = indexToNodeV.get(pairU[u]);

                        // Find the edge object
                        for (Edge edge : remainingEdges) {
                            if ((edge.getSource().equals(nodeU) && edge.getDestination().equals(nodeV)) ||
                                    (edge.getSource().equals(nodeV) && edge.getDestination().equals(nodeU))) {
                                matchedEdges.add(edge);
                                finalSchedule.put(edge, color);
                                animateMatchWithColor(nodeU, nodeV, color);
                                sleep(animationStepTime);
                                break;
                            }
                        }
                    }
                }
            }

            // Remove matched edges from remaining edges
            remainingEdges.removeAll(matchedEdges);

            // If no edges were matched in this period, exit
            if (matchedEdges.isEmpty()) {
                break;
            }
            logMessage("[Period " + color + "] Scheduled " + matchedEdges.size() + " edges");

            color++;
        }

        logMessage("═════════════════════");
        logMessage("Time Tabling Completed!");
        logMessage("Total Periods Required: " + (color - 1));
        logMessage("══════════════════════");
    }

    private void animateMatchWithColor(Node nodeU, Node nodeV, int color) {
        Platform.runLater(() -> {
            Color periodColor = getColorByPeriod(color);

            NodeGUI nodeUGUI = graphGUI.getNodeGUI(nodeU);
            NodeGUI nodeVGUI = graphGUI.getNodeGUI(nodeV);

            EdgeGUI edgeGUI = graphGUI.getEdgeGUI(nodeU, nodeV);
            if (edgeGUI != null) {
                edgeGUI.setLineColor(periodColor);
            }

            logMessage("[Period " + color + "] Matched edge: " + nodeU.getNodeName() + " - " + nodeV.getNodeName());
        });
    }

    private void animateMatch(int u, int v) {
        Platform.runLater(() -> {
            Node nodeU = indexToNodeU.get(u);
            Node nodeV = indexToNodeV.get(v);

            if (nodeU != null && nodeV != null) {
                NodeGUI nodeUGUI = graphGUI.getNodeGUI(nodeU);
                NodeGUI nodeVGUI = graphGUI.getNodeGUI(nodeV);

                if (nodeUGUI != null) {
                    nodeUGUI.setColor(MATCHED_COLOR);
                }
                if (nodeVGUI != null) {
                    nodeVGUI.setColor(MATCHED_COLOR);
                }

                EdgeGUI edgeGUI = graphGUI.getEdgeGUI(nodeU, nodeV);
                if (edgeGUI != null) {
                    edgeGUI.setLineColor(MATCHED_COLOR_EDGE);
                }
            }
        });
    }

    private void clearVisualization() {
        Platform.runLater(() -> {
            // Clear all node colors to default (WHITE)
            for (Node node : graph.getNodeList()) {
                NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                if (nodeGUI != null) {
                    nodeGUI.setColor(Color.WHITE);
                }
            }
            // Clear all edge colors to default (BLACK)
            for (Edge edge : graph.getEdgeList()) {
                EdgeGUI edgeGUI = graphGUI.getEdgeGUI(edge.getSource(), edge.getDestination());
                if (edgeGUI != null) {
                    edgeGUI.setLineColor(Color.BLACK);
                }
            }
        });
        sleep(100);
    }

    private void highlightAugmentingPath(int u) {
        Platform.runLater(() -> {
            // Trace back through the augmenting path and highlight it
            int v = pairU[u];
            if (v != NIL) {
                Node nodeU = indexToNodeU.get(u);
                Node nodeV = indexToNodeV.get(v);

                if (nodeU != null && nodeV != null) {
                    EdgeGUI edgeGUI = graphGUI.getEdgeGUI(nodeU, nodeV);
                    if (edgeGUI != null) {
                        edgeGUI.setLineColor(AUGMENTING_PATH_COLOR);
                    }
                }
            }
        });
    }

    private void clearExplorationColors() {
        Platform.runLater(() -> {
            // Clear all edge colors back to BLACK (revert BFS/DFS/augmenting path colors)
            for (Edge edge : graph.getEdgeList()) {
                EdgeGUI edgeGUI = graphGUI.getEdgeGUI(edge.getSource(), edge.getDestination());
                if (edgeGUI != null) {
                    edgeGUI.setLineColor(Color.BLACK);
                }
            }
        });
        sleep(100);
    }

    private void colorEdgeExploration(Node nodeU, Node nodeV, Color color) {
        Platform.runLater(() -> {
            EdgeGUI edgeGUI = graphGUI.getEdgeGUI(nodeU, nodeV);
            if (edgeGUI != null) {
                edgeGUI.setLineColor(color);
            }
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean bfs() {
        Queue<Integer> Q = new LinkedList<>();

        for (int u = 1; u <= m; u++) {
            if (pairU[u] == NIL) {
                dist[u] = 0;
                Q.add(u);
            } else
                dist[u] = INF;
        }
        dist[NIL] = INF;

        while (!Q.isEmpty()) {
            int u = Q.poll();
            if (dist[u] < dist[NIL]) {
                for (int i : adj[u]) {
                    int v = i;
                    if (dist[pairV[v]] == INF) {
                        // Visualize BFS exploration
                        Node nodeU = indexToNodeU.get(u);
                        Node nodeV = indexToNodeV.get(v);
                        if (nodeU != null && nodeV != null) {
                            String edgeKey = nodeU.getNodeName() + "-" + nodeV.getNodeName();
                            if (!exploredEdges.contains(edgeKey)) {
                                exploredEdges.add(edgeKey);
                                colorEdgeExploration(nodeU, nodeV, BFS_EXPLORE_COLOR);
                                try {
                                    Thread.sleep(animationStepTime / 4);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }

                        dist[pairV[v]] = dist[u] + 1;
                        Q.add(pairV[v]);
                    }
                }
            }
        }
        return (dist[NIL] != INF);
    }

    private boolean dfs(int u) {
        if (u != NIL) {
            for (int i : adj[u]) {
                int v = i;
                if (dist[pairV[v]] == dist[u] + 1) {
                    // Visualize DFS exploration
                    Node nodeU = indexToNodeU.get(u);
                    Node nodeV = indexToNodeV.get(v);
                    if (nodeU != null && nodeV != null) {
                        colorEdgeExploration(nodeU, nodeV, DFS_EXPLORE_COLOR);
                        try {
                            Thread.sleep(animationStepTime / 4);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    if (dfs(pairV[v])) {
                        pairV[v] = u;
                        pairU[u] = v;
                        return true;
                    }

                    // Backtrack - revert edge color
                    if (nodeU != null && nodeV != null) {
                        colorEdgeExploration(nodeU, nodeV, Color.BLACK);
                    }
                }
            }
            dist[u] = INF;
            return false;
        }
        return true;
    }

    private static void logMessage(String message) {
        if (GraphVisualizationController.instance != null) {
            GraphVisualizationController.instance.logMessage(message);
        }
    }
}
