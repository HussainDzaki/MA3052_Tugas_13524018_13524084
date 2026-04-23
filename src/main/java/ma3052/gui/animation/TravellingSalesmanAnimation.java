package ma3052.gui.animation;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.paint.Color;

import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.core.graph.PointGraph;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;
import ma3052.gui.graph.PointGraphGUI;
import ma3052.gui.graph.EdgeGUI;
import ma3052.core.algorithm.TravellingSalesman;

/**
 * TSP Animation (Travelling Salesman Problem)
 * Fase 1: Hamiltonian Cycle DFS dengan greedy edge selection
 * Fase 2: 2-Opt Optimization
 */
public class TravellingSalesmanAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    // Node Colors
    private static final Color HAMILTONIAN_COLOR = Color.web("#4a90e2");
    private static final Color SELECTED_NODE = Color.web("#f59e0b"); 
    private static final Color TWO_OPT_COLOR = Color.web("#fbbf24"); 
    private static final Color RESULT_COLOR = Color.web("#ffd53d"); 

    // Edge Colors

    private static final Color EDGE_NORMAL_COLOR = Color.web("#000"); 
    private static final Color EDGE_HAMILTONIAN_COLOR = Color.web("#4a90e2"); 
    private static final Color EDGE_EXPLORE_COLOR = Color.web("#2f49a9"); 
    private static final Color EDGE_SELECTED_COLOR = Color.web("#da6e09"); 
    private static final Color EDGE_RESULT_COLOR = Color.web("#da6e09"); 

    public static void setAnimationStepTime(long animationStepTime) {
        TravellingSalesmanAnimation.animationStepTime = animationStepTime;
    }

    /**
     * Main animate method - handles both phases
     * Phase 1: Show Hamiltonian Cycle
     * Phase 2: Show 2-Opt optimization with edge swaps
     * Phase 3: Show final optimal path
     */
    public static void animate(GraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null || graphGUI == null) {
            return;
        }

        Graph graph = graphGUI.getGraph();
        List<Node> hamiltonCycle = TravellingSalesman.getHamiltonianCycle(graph);

        // PHASE 1: Show Hamiltonian Cycle
        GraphVisualizationController.instance.logMessage("\nPHASE 1: Hamiltonian Cycle");
        for (int i = 0; i < hamiltonCycle.size(); i++) {
            Node node = hamiltonCycle.get(i);
            Node nextNode = hamiltonCycle.get((i + 1) % (hamiltonCycle.size()));
            final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
            final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(node, nextNode);
            if (edgeGUI != null) {
                Platform.runLater(() -> {
                    edgeGUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                    nodeGUI.setColor(HAMILTONIAN_COLOR);
                });
            }

            try {
                Thread.sleep(animationStepTime);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

        // PHASE 2: 2-Opt Optimization with Visualization
        GraphVisualizationController.instance.logMessage("\nPHASE 2: 2-Opt Optimization");
        ArrayList<Node> cycle = new ArrayList<>(hamiltonCycle);
        if (cycle.isEmpty()) {
            GraphVisualizationController.instance.logMessage("\nGraph Doesn't Have Hamilton Cycle");

        }

        try {
            boolean hasChanges = true;
            int iterationCount = 0;

            do {
                hasChanges = false;
                iterationCount++;
                GraphVisualizationController.instance.logMessage("2-Opt Iteration: " + iterationCount);
                for (int i = 0; i < cycle.size(); i++) {
                    for (int j = i + 2; j < cycle.size(); j++) {
                        Node node1 = cycle.get(i);
                        Node node2 = cycle.get(i + 1);
                        Node node3 = cycle.get(j);
                        Node node4 = cycle.get((j + 1) % cycle.size());

                        final NodeGUI nodeGUI1 = graphGUI.getNodeGUI(node1);
                        final NodeGUI nodeGUI2 = graphGUI.getNodeGUI(node2);
                        final NodeGUI nodeGUI3 = graphGUI.getNodeGUI(node3);
                        final NodeGUI nodeGUI4 = graphGUI.getNodeGUI(node4);

                        // STEP 1: Show Current Edges Being Compared
                        final EdgeGUI edge12GUI = graphGUI.getEdgeGUI(node1, node2);
                        final EdgeGUI edge34GUI = graphGUI.getEdgeGUI(node3, node4);

                        Platform.runLater(() -> {
                            nodeGUI1.setColor(SELECTED_NODE);
                            nodeGUI2.setColor(SELECTED_NODE);
                            nodeGUI3.setColor(SELECTED_NODE);
                            nodeGUI4.setColor(SELECTED_NODE);
                            if (edge12GUI != null)
                                edge12GUI.setLineColor(EDGE_SELECTED_COLOR);
                            if (edge34GUI != null)
                                edge34GUI.setLineColor(EDGE_SELECTED_COLOR);
                        });
                        Thread.sleep(animationStepTime);

                        // Get edges for comparison
                        Edge edge1 = node1.getEdge(node2);
                        Edge edge2 = node3.getEdge(node4);
                        Edge edge3 = node1.getEdge(node3);
                        Edge edge4 = node2.getEdge(node4);

                        // If any edge is missing, skip this pair
                        if (edge1 == null || edge2 == null || edge3 == null || edge4 == null) {
                            // Reset colors
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                if (edge12GUI != null)
                                    edge12GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                                if (edge34GUI != null)
                                    edge34GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                            });
                            continue;
                        }

                        double currentDist = edge1.getWeight() + edge2.getWeight();
                        double newDist = edge3.getWeight() + edge4.getWeight();

                        // STEP 2: Show New Potential Edges (if better)
                        if (currentDist > newDist) {
                            GraphVisualizationController.instance
                                    .logMessage("Found improvement: " + currentDist + " > " + newDist);
                            final EdgeGUI edge13GUI = graphGUI.getEdgeGUI(node1, node3);
                            final EdgeGUI edge24GUI = graphGUI.getEdgeGUI(node2, node4);

                            // Show new edges in EXPLORE color
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(TWO_OPT_COLOR);
                                nodeGUI2.setColor(TWO_OPT_COLOR);
                                nodeGUI3.setColor(TWO_OPT_COLOR);
                                nodeGUI4.setColor(TWO_OPT_COLOR);
                                if (edge13GUI != null)
                                    edge13GUI.setLineColor(EDGE_EXPLORE_COLOR);
                                if (edge24GUI != null)
                                    edge24GUI.setLineColor(EDGE_EXPLORE_COLOR);
                            });
                            Thread.sleep(animationStepTime);

                            // Perform the swap
                            for (int k = 1; k <= (j - i) / 2; k++) {
                                Node temp = cycle.get(i + k);
                                cycle.set(i + k, cycle.get(j - k + 1));
                                cycle.set(j - k + 1, temp);
                            }
                            hasChanges = true;

                            // STEP 3: Reset Colors After Comparison
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                if (edge12GUI != null)
                                    edge12GUI.setLineColor(EDGE_NORMAL_COLOR);
                                if (edge34GUI != null)
                                    edge34GUI.setLineColor(EDGE_NORMAL_COLOR);
                            });
                        }
                        else {
                            // STEP 3: Reset Colors After Comparison
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                if (edge12GUI != null)
                                    edge12GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                                if (edge34GUI != null)
                                    edge34GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                            });
                        }
                        Thread.sleep(animationStepTime / 2);
                    }
                }
            } while (hasChanges);

            // PHASE 3: Show Final Optimal Path
            GraphVisualizationController.instance.logMessage("PHASE 3: Final Optimal Path");
            Thread.sleep(animationStepTime * 2);

            // Highlight final optimal cycle with distinct colors
            double totalWeight = 0;
            for (int i = 0; i < cycle.size(); i++) {
                Node node = cycle.get(i);
                Node nextNode = cycle.get((i + 1) % cycle.size());
                final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(node, nextNode);
                Edge edge = node.getEdge(nextNode);
                totalWeight += edge.getWeight();
                if (edgeGUI != null) {
                    Platform.runLater(() -> {
                        edgeGUI.setLineColor(EDGE_RESULT_COLOR);
                        nodeGUI.setColor(RESULT_COLOR);
                    });
                }

                try {
                    Thread.sleep(animationStepTime / 2);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
            GraphVisualizationController.instance
                        .logMessage("With Total Weight: " + totalWeight);
            GraphVisualizationController.instance.logMessage("Initial Hamilton Cycle : " + graphGUI.pathToString(hamiltonCycle));
            GraphVisualizationController.instance.logMessage("Result Cycle : " + graphGUI.pathToString(cycle));


        } catch (Exception e) {
            GraphVisualizationController.instance.logMessage("Error during 2-opt animation: " + e.getMessage());
            e.printStackTrace();
        }

    }
    
    public static void animate(PointGraphGUI graphGUI) {
        if (GraphVisualizationController.instance == null || graphGUI == null) {
            return;
        }

        PointGraph graph = graphGUI.getGraph();
        List<Node> hamiltonCycle = TravellingSalesman.getHamiltonianCycle(graph);

        // PHASE 1: Show Hamiltonian Cycle
        GraphVisualizationController.instance.logMessage("\nPHASE 1: Hamiltonian Cycle");
        for (int i = 0; i < hamiltonCycle.size(); i++) {
            Node node = hamiltonCycle.get(i);
            Node nextNode = hamiltonCycle.get((i + 1) % (hamiltonCycle.size()));
            final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
            final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(node, nextNode);
            graphGUI.addEdge(node, nextNode);
            if (edgeGUI != null) {
                Platform.runLater(() -> {
                    edgeGUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                    nodeGUI.setColor(HAMILTONIAN_COLOR);
                });
            }

            try {
                Thread.sleep(animationStepTime);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

        // PHASE 2: 2-Opt Optimization with Visualization
        GraphVisualizationController.instance.logMessage("\nPHASE 2: 2-Opt Optimization");
        ArrayList<Node> cycle = new ArrayList<>(hamiltonCycle);
        if (cycle.isEmpty()) {
            GraphVisualizationController.instance.logMessage("\nGraph Doesn't Have Hamilton Cycle");

        }

        try {
            boolean hasChanges = true;
            int iterationCount = 0;

            do {
                hasChanges = false;
                iterationCount++;
                GraphVisualizationController.instance.logMessage("2-Opt Iteration: " + iterationCount);
                for (int i = 0; i < cycle.size(); i++) {
                    for (int j = i + 2; j < cycle.size(); j++) {
                        Node node1 = cycle.get(i);
                        Node node2 = cycle.get(i + 1);
                        Node node3 = cycle.get(j);
                        Node node4 = cycle.get((j + 1) % cycle.size());

                        final NodeGUI nodeGUI1 = graphGUI.getNodeGUI(node1);
                        final NodeGUI nodeGUI2 = graphGUI.getNodeGUI(node2);
                        final NodeGUI nodeGUI3 = graphGUI.getNodeGUI(node3);
                        final NodeGUI nodeGUI4 = graphGUI.getNodeGUI(node4);

                        // STEP 1: Show Current Edges Being Compared
                        final EdgeGUI edge12GUI = graphGUI.getEdgeGUI(node1, node2);
                        final EdgeGUI edge34GUI = graphGUI.getEdgeGUI(node3, node4);

                        Platform.runLater(() -> {
                            nodeGUI1.setColor(SELECTED_NODE);
                            nodeGUI2.setColor(SELECTED_NODE);
                            nodeGUI3.setColor(SELECTED_NODE);
                            nodeGUI4.setColor(SELECTED_NODE);
                            if (edge12GUI != null)
                                edge12GUI.setLineColor(EDGE_SELECTED_COLOR);
                            if (edge34GUI != null)
                                edge34GUI.setLineColor(EDGE_SELECTED_COLOR);
                        });
                        Thread.sleep(animationStepTime);

                        // Get edges for comparison
                        Edge edge1 = node1.getEdge(node2);
                        Edge edge2 = node3.getEdge(node4);
                        Edge edge3 = node1.getEdge(node3);
                        Edge edge4 = node2.getEdge(node4);

                        // If any edge is missing, skip this pair
                        if (edge1 == null || edge2 == null || edge3 == null || edge4 == null) {
                            // Reset colors
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                if (edge12GUI != null)
                                    edge12GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                                if (edge34GUI != null)
                                    edge34GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                            });
                            continue;
                        }

                        double currentDist = edge1.getWeight() + edge2.getWeight();
                        double newDist = edge3.getWeight() + edge4.getWeight();

                        // STEP 2: Show New Potential Edges (if better)
                        if (currentDist > newDist) {
                            GraphVisualizationController.instance
                                    .logMessage("Found improvement: " + currentDist + " > " + newDist);
                            final EdgeGUI edge13GUI = graphGUI.getEdgeGUI(node1, node3);
                            final EdgeGUI edge24GUI = graphGUI.getEdgeGUI(node2, node4);

                            // Show new edges in EXPLORE color
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(TWO_OPT_COLOR);
                                nodeGUI2.setColor(TWO_OPT_COLOR);
                                nodeGUI3.setColor(TWO_OPT_COLOR);
                                nodeGUI4.setColor(TWO_OPT_COLOR);
                                if (edge13GUI != null)
                                    edge13GUI.setLineColor(EDGE_EXPLORE_COLOR);
                                if (edge24GUI != null)
                                    edge24GUI.setLineColor(EDGE_EXPLORE_COLOR);
                            });
                            Thread.sleep(animationStepTime);

                            // Perform the swap
                            for (int k = 1; k <= (j - i) / 2; k++) {
                                Node temp = cycle.get(i + k);
                                cycle.set(i + k, cycle.get(j - k + 1));
                                cycle.set(j - k + 1, temp);
                            }
                            hasChanges = true;
                            
                            // STEP 3: Reset Colors After Comparison
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                graphGUI.removeEdge(edge12GUI.getEdge());
                                graphGUI.removeEdge(edge24GUI.getEdge());
                            });
                        }
                        else {
                            // STEP 3: Reset Colors After Comparison
                            Platform.runLater(() -> {
                                nodeGUI1.setColor(HAMILTONIAN_COLOR);
                                nodeGUI2.setColor(HAMILTONIAN_COLOR);
                                nodeGUI3.setColor(HAMILTONIAN_COLOR);
                                nodeGUI4.setColor(HAMILTONIAN_COLOR);
                                if (edge12GUI != null)
                                    edge12GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                                if (edge34GUI != null)
                                    edge34GUI.setLineColor(EDGE_HAMILTONIAN_COLOR);
                            });
                        }
                        Thread.sleep(animationStepTime / 2);
                    }
                }
            } while (hasChanges);

            // PHASE 3: Show Final Optimal Path
            GraphVisualizationController.instance.logMessage("PHASE 3: Final Optimal Path");
            Thread.sleep(animationStepTime * 2);

            // Highlight final optimal cycle with distinct colors
            double totalWeight = 0;
            for (int i = 0; i < cycle.size(); i++) {
                Node node = cycle.get(i);
                Node nextNode = cycle.get((i + 1) % cycle.size());
                final NodeGUI nodeGUI = graphGUI.getNodeGUI(node);
                final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(node, nextNode);
                Edge edge = node.getEdge(nextNode);
                totalWeight += edge.getWeight();
                if (edgeGUI != null) {
                    Platform.runLater(() -> {
                        edgeGUI.setLineColor(EDGE_RESULT_COLOR);
                        nodeGUI.setColor(RESULT_COLOR);
                    });
                }

                try {
                    Thread.sleep(animationStepTime / 2);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
            GraphVisualizationController.instance
                        .logMessage("With Total Weight: " + totalWeight);
            GraphVisualizationController.instance.logMessage("Initial Hamilton Cycle : " + graphGUI.pathToString(hamiltonCycle));
            GraphVisualizationController.instance.logMessage("Result Cycle : " + graphGUI.pathToString(cycle));


        } catch (Exception e) {
            GraphVisualizationController.instance.logMessage("Error during 2-opt animation: " + e.getMessage());
            e.printStackTrace();
        }

    }
}