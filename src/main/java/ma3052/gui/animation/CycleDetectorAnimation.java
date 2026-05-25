package ma3052.gui.animation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.paint.Color;

import ma3052.core.algorithm.CycleDetector;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class CycleDetectorAnimation {
    // Node colors for bipartite coloring
    private static final Color UNVISITED_COLOR = Color.web("#ffffff"); // Putih
    private static final Color PROCESSING_COLOR = Color.web("#ffaa3b"); // Oranye
    private static final Color COLOR_0 = Color.web("#3b82f6"); // Biru (Warna 0)
    private static final Color COLOR_1 = Color.web("#ff6b6b"); // Merah (Warna 1)
    private static final Color NOT_BIPARTITE_COLOR = Color.web("#fbbf24"); // Kuning (Konflik)
    private static final Color PATH_COLOR = Color.web("#2dab00"); // Hijau
    private static final Color CYCLE_NODE_COLOR = Color.web("#8b5cf6"); // Ungu (Cycle)
    private static final Color CYCLE_EDGE_COLOR = Color.web("#8b5cf6"); // Ungu (Cycle)

    private static volatile long animationStepTime = 500;

    public static void setAnimationStepTime(long stepTime) {
        CycleDetectorAnimation.animationStepTime = Math.max(1, stepTime);
    }

    // ============= BIPARTITE ANIMATION =============
    public static void animateIsBipartite(GraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null) {
            return;
        }

        try {
            Graph graph = graphGUI.getGraph();
            if (graph == null || graph.isEmpty()) {
                logMessage("Graph is empty!");
                return;
            }

            logMessage("═════════════════");
            logMessage("CHECKING BIPARTITENESS");
            logMessage("═════════════════");
            Thread.sleep(500);

            HashMap<Long, Integer> nodeColors = new HashMap<>();
            boolean isBipartite = true;

            for (Node startNode : graph.getNodeList()) {
                if (!nodeColors.containsKey(startNode.getNodeID())) {
                    ArrayDeque<Node> queue = new ArrayDeque<>();
                    queue.add(startNode);
                    nodeColors.put(startNode.getNodeID(), 0);

                    final NodeGUI startNodeGUI = graphGUI.getNodeGUI(startNode);
                    Platform.runLater(() -> {
                        if (startNodeGUI != null) {
                            startNodeGUI.setColor(COLOR_0);
                        }
                    });
                    logMessage("[START] Node " + startNode.getNodeName() + " colored with 0");
                    Thread.sleep(animationStepTime);

                    while (!queue.isEmpty() && isBipartite) {
                        Node currNode = queue.poll();
                        int currColor = nodeColors.get(currNode.getNodeID());
                        int neighborColor = 1 - currColor;

                        final NodeGUI currNodeGUI = graphGUI.getNodeGUI(currNode);
                        Platform.runLater(() -> {
                            if (currNodeGUI != null) {
                                currNodeGUI.setColor(PROCESSING_COLOR);
                            }
                        });
                        Thread.sleep(animationStepTime / 2);

                        for (Edge edge : currNode.getAdjacencyList()) {
                            Node nextNode = edge.getDestination();
                            long nextNodeID = nextNode.getNodeID();

                            if (!nodeColors.containsKey(nextNodeID)) {
                                nodeColors.put(nextNodeID, neighborColor);
                                queue.add(nextNode);

                                final NodeGUI nextNodeGUI = graphGUI.getNodeGUI(nextNode);
                                final Color colorToSet = (neighborColor == 0) ? COLOR_0 : COLOR_1;
                                Platform.runLater(() -> {
                                    if (nextNodeGUI != null) {
                                        nextNodeGUI.setColor(colorToSet);
                                    }
                                });

                                logMessage("[COLOR] Node " + nextNode.getNodeName() + " colored with " + neighborColor);
                                Thread.sleep(animationStepTime / 2);
                            } else if (nodeColors.get(nextNodeID) == currColor) {
                                isBipartite = false;

                                final NodeGUI conflictNode = graphGUI.getNodeGUI(nextNode);
                                Platform.runLater(() -> {
                                    if (conflictNode != null) {
                                        conflictNode.setColor(NOT_BIPARTITE_COLOR);
                                    }
                                });

                                logMessage("[CONFLICT] Node " + currNode.getNodeName() + " (color " + currColor
                                        + ") connected to " + nextNode.getNodeName() + " (same color)!");
                                Thread.sleep(animationStepTime);
                                break;
                            }
                        }

                        final NodeGUI processedNodeGUI = graphGUI.getNodeGUI(currNode);
                        final Color finalColor = (currColor == 0) ? COLOR_0 : COLOR_1;
                        Platform.runLater(() -> {
                            if (processedNodeGUI != null) {
                                processedNodeGUI.setColor(finalColor);
                            }
                        });
                    }

                    if (!isBipartite) {
                        break;
                    }
                }
            }

            Thread.sleep(500);
            logMessage("════════════════");
            if (isBipartite) {
                logMessage("Result: GRAPH IS BIPARTITE");
                logMessage("Color 0 (Blue) and Color 1 (Red) can form 2 independent sets");
            } else {
                logMessage("Result: GRAPH IS NOT BIPARTITE");
                logMessage("Graph contains odd cycles");
            }
            logMessage("════════════════");

        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR: " + e.getMessage());
        }
    }

    // ============= DIAMETER ANIMATION =============
    public static void animateFindDiameter(GraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null) {
            return;
        }

        try {
            Graph graph = graphGUI.getGraph();
            if (graph == null || graph.isEmpty()) {
                logMessage("Graph is empty!");
                return;
            }

            logMessage("═════════════════");
            logMessage("FINDING DIAMETER PATH");
            logMessage("═════════════════");
            Thread.sleep(500);

            List<Node> longestPathFound = new ArrayList<>();

            for (final Node startNode : graph.getNodeList()) {
                logMessage("[SEARCH] Starting from node " + startNode.getNodeName());
                final NodeGUI startNodeGUI = graphGUI.getNodeGUI(startNode);
                Platform.runLater(() -> {
                    if (startNodeGUI != null) {
                        startNodeGUI.setColor(PROCESSING_COLOR);
                    }
                });
                Thread.sleep(animationStepTime / 2);

                List<Node> currPath = animateFarthestPathDFS(graphGUI, startNode);

                logMessage("[RESULT] Path length: " + (currPath.size() - 1 )+ " : " + nodePathToString(currPath));
                Thread.sleep(animationStepTime);

                if (currPath.size() > longestPathFound.size()) {
                    // Clear previous longest path colors
                    for (Node node : longestPathFound) {
                        final NodeGUI oldPathNode = graphGUI.getNodeGUI(node);
                        Platform.runLater(() -> {
                            if (oldPathNode != null) {
                                oldPathNode.setColor(UNVISITED_COLOR);
                            }
                        });
                    }

                    longestPathFound = currPath;
                    logMessage("[UPDATE] New longest path found!");
                    Thread.sleep(animationStepTime / 2);
                }

                // Reset processing node
                Platform.runLater(() -> {
                    if (startNodeGUI != null) {
                        startNodeGUI.setColor(UNVISITED_COLOR);
                    }
                });
            }

            Thread.sleep(500);
            logMessage("════════════════");
            logMessage("DIAMETER PATH FOUND");
            logMessage("Path: " + nodePathToString(longestPathFound));
            logMessage("Diameter: " + (longestPathFound.size() - 1) + " edges");
            logMessage("════════════════");

            // Highlight final path
            for (int i = 0; i < longestPathFound.size(); i++) {
                final Node node = longestPathFound.get(i);
                final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                Platform.runLater(() -> {
                    if (nodeGUI != null) {
                        nodeGUI.setColor(PATH_COLOR);
                    }
                });
                Thread.sleep(animationStepTime / 2);

                if (i < longestPathFound.size() - 1) {
                    final Node nextNode = longestPathFound.get(i + 1);
                    Edge edge = graph.getEdge(node, nextNode);
                    if (edge != null) {
                        Platform.runLater(() -> {
                            if (graphGUI.getEdgeGUI(edge) != null) {
                                graphGUI.getEdgeGUI(edge).setLineColor(PATH_COLOR);
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR: " + e.getMessage());
        }
    }

    private static List<Node> animateFarthestPathDFS(GraphGUI graphGUI, Node startNode) throws InterruptedException {
        Graph graph = graphGUI.getGraph();
        HashMap<Long, Node> pred = new HashMap<>();
        HashMap<Long, Integer> distances = new HashMap<>();

        ArrayDeque<Node> queue = new ArrayDeque<>();
        distances.put(startNode.getNodeID(), 0);
        pred.put(startNode.getNodeID(), null);
        queue.add(startNode);

        Node farthestNode = startNode;

        while (!queue.isEmpty()) {
            Node currNode = queue.poll();
            int currDist = distances.get(currNode.getNodeID());

            if (currDist > distances.get(farthestNode.getNodeID())) {
                farthestNode = currNode;
            }

            for (Edge edge : currNode.getAdjacencyList()) {
                Node neighbor = edge.getDestination();
                Long neighborID = neighbor.getNodeID();

                if (!distances.containsKey(neighborID)) {
                    distances.put(neighborID, currDist + 1);
                    pred.put(neighborID, currNode);
                    queue.add(neighbor);

                    final NodeGUI neighborGUI = graphGUI.getNodeGUI(neighbor);
                    Platform.runLater(() -> {
                        if (neighborGUI != null) {
                            neighborGUI.setColor(Color.web("#e0e0e0"));
                        }
                    });
                }
            }
        }

        return reconstructPath(farthestNode, pred);
    }

    private static List<Node> reconstructPath(Node target, HashMap<Long, Node> pred) {
        ArrayList<Node> path = new ArrayList<>();
        Node step = target;
        while (step != null) {
            path.add(0, step);
            step = pred.get(step.getNodeID());
        }
        return path;
    }

    // ============= CYCLE DETECTION ANIMATION =============
    public static void animateHaveACycle(GraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null) {
            return;
        }

        try {
            Graph graph = graphGUI.getGraph();
            if (graph == null || graph.isEmpty()) {
                logMessage("Graph is empty!");
                return;
            }

            logMessage("═════════════════");
            logMessage("DETECTING CYCLE");
            logMessage("═════════════════");
            Thread.sleep(500);

            List<Node> cycle;
            if (graph.isDirected()) {
                cycle = CycleDetector.getDirectedCyclePath(graph);
            } else {
                cycle = CycleDetector.getUndirectedCyclePath(graph);
            }

            if (cycle.isEmpty()) {
                logMessage("[RESULT] No cycle found in graph ✓");
                logMessage("This is an acyclic graph!");
            } else {
                logMessage("[RESULT] Cycle detected! ✗");
                logMessage("Cycle path: " + nodePathToString(cycle));
                Thread.sleep(500);

                // Highlight the cycle
                logMessage("════════════════");
                logMessage("HIGHLIGHTING CYCLE");
                logMessage("════════════════");
                Thread.sleep(300);

                for (int i = 0; i < cycle.size(); i++) {
                    final Node node = cycle.get(i);
                    final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                    Platform.runLater(() -> {
                        if (nodeGUI != null) {
                            nodeGUI.setColor(CYCLE_NODE_COLOR);
                            nodeGUI.setBorderColor(Color.DARKVIOLET);
                        }
                    });

                    logMessage("[CYCLE] Node " + node.getNodeName());
                    Thread.sleep(animationStepTime / 2);

                    if (i < cycle.size() - 1) {
                        final Node nextNode = cycle.get(i + 1);
                        Edge edge = graph.getEdge(node, nextNode);
                        Edge endEdge = graph.getEdge(nextNode, node);
                        if (edge != null) {
                            Platform.runLater(() -> {
                                if (graphGUI.getEdgeGUI(edge) != null) {
                                    graphGUI.getEdgeGUI(edge).setLineColor(CYCLE_EDGE_COLOR);
                                }
                            });
                            Thread.sleep(animationStepTime / 2);
                        }

                        if (endEdge != null) {
                            Platform.runLater(() -> {
                                if (graphGUI.getEdgeGUI(endEdge) != null) {
                                    graphGUI.getEdgeGUI(endEdge).setLineColor(CYCLE_EDGE_COLOR);
                                }
                            });
                            Thread.sleep(animationStepTime / 2);
                        }
                    }
                }
            }

            Thread.sleep(500);
            logMessage("════════════════");
            logMessage("Cycle Detection Complete!");
            logMessage("════════════════");

        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR: " + e.getMessage());
        }
    }

    // ============= SMALLEST CYCLE (GIRTH) ANIMATION =============
    public static void animateHaveSmallestCycle(GraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null) {
            return;
        }

        try {
            Graph graph = graphGUI.getGraph();
            if (graph == null || graph.isEmpty()) {
                logMessage("Graph is empty!");
                return;
            }

            logMessage("═════════════════");
            logMessage("FINDING SMALLEST CYCLE (GIRTH)");
            logMessage("═════════════════");
            Thread.sleep(500);

            List<Node> shortestCycle = CycleDetector.getGirthPath(graph);

            if (shortestCycle.isEmpty()) {
                logMessage("[RESULT] No cycle found (acyclic graph) ✓");
            } else {
                logMessage("[RESULT] Smallest cycle found!");
                logMessage("Girth (cycle length): " + (shortestCycle.size() - 1));
                logMessage("Cycle path: " + nodePathToString(shortestCycle));
                Thread.sleep(500);

                // Highlight the girth
                logMessage("════════════════");
                logMessage("HIGHLIGHTING GIRTH");
                logMessage("════════════════");
                Thread.sleep(300);

                for (int i = 0; i < shortestCycle.size(); i++) {
                    final Node node = shortestCycle.get(i);
                    final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                    Platform.runLater(() -> {
                        if (nodeGUI != null) {
                            nodeGUI.setColor(Color.web("#fbbf24")); // Kuning cerah
                            nodeGUI.setBorderColor(Color.DARKORANGE);
                        }
                    });

                    logMessage("[GIRTH] Node " + node.getNodeName());
                    Thread.sleep(animationStepTime / 2);

                    if (i < shortestCycle.size() - 1) {
                        final Node nextNode = shortestCycle.get(i + 1);
                        Edge edge = graph.getEdge(node, nextNode);
                        Edge endEdge = graph.getEdge(nextNode, node);
                        if (edge != null) {
                            Platform.runLater(() -> {
                                if (graphGUI.getEdgeGUI(edge) != null) {
                                    graphGUI.getEdgeGUI(edge).setLineColor(Color.web("#fbbf24"));
                                }
                            });
                            Thread.sleep(animationStepTime / 2);
                        }

                        if (endEdge != null) {
                            Platform.runLater(() -> {
                                if (graphGUI.getEdgeGUI(endEdge) != null) {
                                    graphGUI.getEdgeGUI(endEdge).setLineColor(Color.web("#fbbf24"));
                                }
                            });
                            Thread.sleep(animationStepTime / 2);
                        }
                    }
                }
            }

            Thread.sleep(500);
            logMessage("════════════════");
            logMessage("Girth Detection Complete!");
            logMessage("════════════════");

        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR: " + e.getMessage());
        }
    }

    // ============= HELPER METHODS =============
    private static String nodePathToString(List<Node> path) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Node node : path) {
            if (first) {
                result.append(node.getNodeName());
                first = false;
            } else {
                result.append(" -> ").append(node.getNodeName());
            }
        }
        return result.toString();
    }

    private static void logMessage(String message) {
        if (GraphVisualizationController.instance != null) {
            GraphVisualizationController.instance.logMessage(message);
        }
    }
}
