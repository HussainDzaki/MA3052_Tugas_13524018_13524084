package ma3052.gui.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import java.util.Set;

import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class DijkstraAnimation {
    private static class NodeEntry implements Comparable<NodeEntry> {
        Node node;
        double distance;

        NodeEntry(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeEntry other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    private static volatile long animationStepTime = 500; // in milliseconds

    // Node colors
    private static final Color UNVISITED_COLOR = Color.web("#ffffff");     // Putih 
    private static final Color PROCESSING_COLOR = Color.web("#ffaa3b");    // Oranye
    private static final Color RELAXED_COLOR = Color.web("#3b82f6");       // Biru
    private static final Color SETTLED_COLOR = Color.web("#ffea3e");       // Kuning
    private static final Color PATH_COLOR = Color.web("#2dab00");          // Hijau
    private static final Color NO_PATH_COLOR = Color.web("#cc0631");       // Merah

    public static void setAnimationStepTime(long animationStepTime) {
        DijkstraAnimation.animationStepTime = animationStepTime;
    }

    public static void animate(GraphGUI graphGUI, String startNode, String endNode) {
        if (GraphVisualizationController.instance == null) {
            return;
        }
        try {
            Graph graph = graphGUI.getGraph();
            Node sourceNode = graph.getNode(startNode);
            Node destinationNode = graph.getNode(endNode);
            
            if (sourceNode == null || destinationNode == null) {
                GraphVisualizationController.instance.logMessage("ERROR: Invalid nodes");
                return;
            }

            Map<Node, Node> visited = new HashMap<>();
            Map<Node, Double> tabelVertex = new HashMap<>();
            PriorityQueue<NodeEntry> nodeQueue = new PriorityQueue<>();
            Set<Node> settledNodes = new HashSet<>();

            // STEP 1: Initialization
            logMessage("═════════════════");
            logMessage("INITIALIZING DIJKSTRA ALGORITHM");
            logMessage("═════════════════");
            
            for (Node node : graph.getNodeList()) {
                if (node == sourceNode) {
                    tabelVertex.put(node, 0.0);
                    final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                    Platform.runLater(() -> nodeGUI.setColor(UNVISITED_COLOR));
                } else {
                    tabelVertex.put(node, Double.MAX_VALUE);
                    final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                    Platform.runLater(() -> nodeGUI.setColor(UNVISITED_COLOR));
                }
            }
            Thread.sleep(500);
            logMessage("[INIT]    Source: " + sourceNode.getNodeName() + " (dist=0)");
            logMessage("[INIT]    Target: " + destinationNode.getNodeName());
            Thread.sleep(200);

            nodeQueue.add(new NodeEntry(sourceNode, 0.0));

            // STEP 2: Main loop
            logMessage("═════════════════════");
            logMessage("STARTING RELAXATION PROCESS");
            logMessage("═════════════════════");
            Thread.sleep(500);

            while (!nodeQueue.isEmpty()) {
                NodeEntry entry = nodeQueue.poll();
                Node currNode = entry.node;

                if (settledNodes.contains(currNode)) {
                    continue;
                }

                // PROCESSING: Change to orange (processing)
                final NodeGUI nodeGUIProcessing = graphGUI.getNodeGUI(currNode);
                Platform.runLater(() -> {
                    if (nodeGUIProcessing != null) {
                        nodeGUIProcessing.setColor(PROCESSING_COLOR);
                    }
                });
                
                double currDist = tabelVertex.get(currNode);
                logMessage("[PICK] Node " + currNode.getNodeName() + " dipilih, jarak = " + formatDistance(currDist));
                
                Thread.sleep(animationStepTime);

                settledNodes.add(currNode);
                
                // SETTLED: Change to yellow
                Platform.runLater(() -> {
                    if (nodeGUIProcessing != null) {
                        nodeGUIProcessing.setColor(SETTLED_COLOR);
                    }
                });
                logMessage("[SETTLED] Node " + currNode.getNodeName() + " selesai, jarak final = " + formatDistance(currDist));
                Thread.sleep(animationStepTime / 2);

                // RELAXATION: Process neighbors
                for (Edge edge : currNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (settledNodes.contains(neighbor)) {
                        continue;
                    }

                    Double distToNeighbor = currDist + edge.getWeight();
                    Double currentNeighborDist = tabelVertex.get(neighbor);

                    if (distToNeighbor < currentNeighborDist) {
                        // RELAXED: Update and change to blue
                        tabelVertex.put(neighbor, distToNeighbor);
                        visited.put(neighbor, currNode);
                        nodeQueue.add(new NodeEntry(neighbor, distToNeighbor));

                        final NodeGUI relaxedNodeGUI = graphGUI.getNodeGUI(neighbor);
                        Platform.runLater(() -> {
                            if (relaxedNodeGUI != null) {
                                relaxedNodeGUI.setColor(RELAXED_COLOR);
                            }
                        });
                        
                        String oldDist = formatDistance(currentNeighborDist);
                        logMessage("[RELAX] " + currNode.getNodeName() + " -> " + neighbor.getNodeName() + 
                                   " diperbarui: " + oldDist + " -> " + formatDistance(distToNeighbor));
                        
                        Thread.sleep(animationStepTime / 2);
                    } else {
                        // SKIP: No update
                        logMessage("[SKIP] " + currNode.getNodeName() + " -> " + neighbor.getNodeName() + 
                                   " tidak diperbarui (" + formatDistance(distToNeighbor) + " ≥ " + formatDistance(currentNeighborDist) + ")");
                        Thread.sleep(100);
                    }
                }
            }

            Thread.sleep(500);
            logMessage("════════════════");
            logMessage("BACKTRACKING SHORTEST PATH");
            logMessage("════════════════");

            // STEP 3: Check if path exists
            Node currBack = destinationNode;
            if (tabelVertex.get(destinationNode) == Double.MAX_VALUE) {
                // NO PATH: Color destination red
                final NodeGUI noPathNode = graphGUI.getNodeGUI(destinationNode);
                Platform.runLater(() -> {
                    if (noPathNode != null) {
                        noPathNode.setColor(NO_PATH_COLOR);
                    }
                });
                
                Thread.sleep(animationStepTime);
                logMessage("[NO PATH] " + destinationNode.getNodeName() + " tidak dapat dijangkau!");
                logMessage("═════════════════════");
                logMessage("Dijkstra Complete!");
                logMessage("STATUS: NO PATH FOUND");
                logMessage("═════════════════════");
                return;
            }

            // STEP 4: Backtrack and highlight path
            List<Node> path = new ArrayList<>();
            while (currBack != null) {
                path.add(currBack);
                currBack = visited.get(currBack);
            }
            Collections.reverse(path);

            Thread.sleep(300);
            for (int i = 0; i < path.size(); i++) {
                Node nodeNow = path.get(i);
                final NodeGUI pathNodeGUI = graphGUI.getNodeGUI(nodeNow);
                Platform.runLater(() -> {
                    if (pathNodeGUI != null) {
                        pathNodeGUI.setColor(PATH_COLOR);
                        pathNodeGUI.setBorderColor(Color.DARKGREEN);
                    }
                });
                
                logMessage("[PATH] Node " + nodeNow.getNodeName() + " bagian dari jalur");
                Thread.sleep(animationStepTime / 2);

                // Color edge to next node
                if (i < path.size() - 1) {
                    Node nextNode = path.get(i + 1);
                    Edge edgeToColor = graph.getEdge(nodeNow, nextNode);

                    if (edgeToColor != null) {
                        Platform.runLater(() -> {
                            if (graphGUI.getEdgeGUI(edgeToColor) != null) {
                                graphGUI.getEdgeGUI(edgeToColor).setLineColor(PATH_COLOR);
                            }
                        });
                        Thread.sleep(animationStepTime / 2);
                    }
                }
            }

            Thread.sleep(500);
            double totalDistance = tabelVertex.get(destinationNode);
            logMessage("══════════════════");
            logMessage("Dijkstra Complete!");
            logMessage("[PATH] " + pathToString(path) + " | Total = " + formatDistance(totalDistance));
            logMessage("══════════════════");

        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR: " + e.getMessage());
        }
    }

    private static String pathToString(List<Node> path) {
        boolean first = true;
        String res = "";
        for (int i = 0; i < path.size(); i++) {
            if (first) {
                res += path.get(i).getNodeName().toString();
                first = false;
            } else {
                res += "->" + path.get(i).getNodeName().toString();
            }
        }
        return res;
    }

    private static String formatDistance(Double distance) {
        if (distance == Double.MAX_VALUE) {
            return "∞";
        }
        // Format with max 2 decimal places
        if (distance == Math.floor(distance)) {
            return String.format("%.0f", distance);
        }
        return String.format("%.2f", distance);
    }

    private static void logMessage(String message) {
        if (GraphVisualizationController.instance != null) {
            GraphVisualizationController.instance.logMessage(message);
        }
    }
}