package ma3052.gui.animation;

import java.util.HashSet;
import java.util.PriorityQueue;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import ma3052.controller.GraphVisualGUIController;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.gui.EdgeGUI;
import ma3052.gui.GraphGUI;
import ma3052.gui.NodeGUI;

public class PrimsAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    public static void setAnimationStepTime(long animationStepTime) {
        PrimsAnimation.animationStepTime = animationStepTime;
    }

    public static void animate(GraphGUI graphGUI) {
        if (GraphVisualGUIController.instance == null)
            return;

        try {
            Graph graph = graphGUI.getGraph();

            PriorityQueue<Edge> primsQueue = new PriorityQueue<>((e1, e2) -> {
                if (e1.getWeight() < e2.getWeight())
                    return -1;
                if (e1.getWeight() > e2.getWeight())
                    return 1;
                return 0;
            });
            int stepCount = 0;
            double totalWeight = 0;
            HashSet<Node> visitedNode = new HashSet<>();
            Node firstNode = graph.getNodeList().getFirst();
            primsQueue.add(new Edge(null, firstNode, 0));
            while (!primsQueue.isEmpty()) {
                stepCount++;
                Edge currentEdge = primsQueue.poll();
                Node currentNode = currentEdge.getDestination();
                int step = stepCount;
                if (visitedNode.contains(currentNode)) {
                    final EdgeGUI edgeGUI1 = graphGUI.getEdgeGUI(currentEdge);
                    Edge otherEdge = graph.getEdge(currentNode, currentEdge.getSource());
                    final EdgeGUI edgeGUI2 = otherEdge == null ? null : graphGUI.getEdgeGUI(otherEdge);
                    Platform.runLater(() -> {
                        if (edgeGUI1 != null) {
                            edgeGUI1.setLineColor(Color.GRAY);
                        }
                        if (edgeGUI2 != null) {
                            edgeGUI2.setLineColor(Color.GRAY);
                        }
                    });
                    if (currentEdge.getSource() != null) {
                        Platform.runLater(() -> {
                            GraphVisualGUIController.instance
                                    .logMessage("[Step " + step + "] Checking edge ("
                                            + currentEdge.getSource().getNodeName() + ", "
                                            + currentEdge.getDestination().getNodeName() + ")"
                                            + " -> NO");
                        });
                    }
                    Thread.sleep((long) (animationStepTime));
                    continue;
                } else {
                    if (currentEdge.getSource() != null) {
                        Platform.runLater(() -> {
                            GraphVisualGUIController.instance
                                    .logMessage("[Step " + step + "] Checking edge ("
                                            + currentEdge.getSource().getNodeName() + ", "
                                            + currentEdge.getDestination().getNodeName() + ")"
                                            + " -> NO");
                        });
                    }
                }
                visitedNode.add(currentNode);
                if (currentEdge.getSource() != null) { // Is not the first node
                    totalWeight += currentEdge.getWeight();
                    final NodeGUI nodeGUI1 = graphGUI.getNodeGUI(currentEdge.getSource());
                    final NodeGUI nodeGUI2 = graphGUI.getNodeGUI(currentEdge.getDestination());
                    final EdgeGUI edgeGUI1 = graphGUI.getEdgeGUI(currentEdge);
                    Edge otherEdge = graph.getEdge(currentNode, currentEdge.getSource());
                    final EdgeGUI edgeGUI2 = otherEdge == null ? null : graphGUI.getEdgeGUI(otherEdge);
                    Platform.runLater(() -> {
                        if (nodeGUI1 != null) {
                            nodeGUI1.setColor(Color.YELLOW);
                            nodeGUI1.setBorderColor(Color.ORANGE);
                        }
                        if (nodeGUI2 != null) {
                            nodeGUI2.setColor(Color.YELLOW);
                            nodeGUI2.setBorderColor(Color.ORANGE);
                        }
                        if (edgeGUI1 != null) {
                            edgeGUI1.setLineColor(Color.ORANGE);
                        }
                        if (edgeGUI2 != null) {
                            edgeGUI2.setLineColor(Color.ORANGE);
                        }
                    });
                    Thread.sleep((long) (animationStepTime));
                }
                for (Edge edge : currentNode.getAdjacencyList()) {
                    Node nextNode = edge.getDestination();
                    if (visitedNode.contains(nextNode))
                        continue;
                    primsQueue.add(edge);
                    final EdgeGUI edgeGUI1 = graphGUI.getEdgeGUI(edge);
                    Edge otherEdge = graph.getEdge(nextNode, currentNode);
                    final EdgeGUI edgeGUI2 = otherEdge == null ? null : graphGUI.getEdgeGUI(otherEdge);
                    Platform.runLater(() -> {
                        if (edgeGUI1 != null) {
                            edgeGUI1.setLineColor(Color.GREEN);
                        }
                        if (edgeGUI2 != null) {
                            edgeGUI2.setLineColor(Color.GREEN);
                        }
                    });
                }
                stepCount++;
                int step2 = stepCount;
                Platform.runLater(() -> {
                    GraphVisualGUIController.instance
                            .logMessage("[Step " + step2 + "] Adding edges to neighbour of " + currentNode.getNodeName() + " to queue");
                });
                Thread.sleep((long) (animationStepTime));
                // Waiting... so you can actually see what's happening
            }

            GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
            GraphVisualGUIController.instance.logMessage("Prims's Algorithm Completed!");
            GraphVisualGUIController.instance.logMessage("Total Weight of Spanning Tree: " + totalWeight);
            GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
