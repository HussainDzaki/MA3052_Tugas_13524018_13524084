package ma3052.gui.animation;

import java.util.ArrayList;
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
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.EdgeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class TraversalAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    public static void setAnimationStepTime(long animationStepTime) {
        TraversalAnimation.animationStepTime = animationStepTime;
    }

    public static void animateDFS(GraphGUI graphGUI, String startNode) {
        if (GraphVisualizationController.instance == null)
            return;

        try {
            Set<Node> visited = new HashSet<>();
            Deque<Node> stack = new LinkedList<>();
            Deque<Node> prevStack = new LinkedList<>();
            List<String> traversalOrder = new ArrayList<>();
            Graph graph = graphGUI.getGraph();

            // Start
            int stepCount = 0;
            stack.push(graph.getNode(startNode));

            while (!stack.isEmpty()) {
                if (Thread.currentThread().isInterrupted())
                    break;

                // Get current node (and previous.. for edge drawing)
                Node currentNode = stack.pop();
                Node prevNode = prevStack.poll();
                if (visited.contains(currentNode))
                    continue;
                visited.add(currentNode);

                // Records the traversal
                traversalOrder.add(currentNode.getNodeName());
                stepCount++;
                // Next nodes
                for (Edge edge : currentNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (!visited.contains(neighbor)) {
                        stack.push(neighbor);
                        prevStack.push(currentNode);
                    }
                }

                // Draw node and edge with new color
                int step = stepCount;
                final NodeGUI nodeToDraw = graphGUI.getNodeGUI(currentNode);
                final EdgeGUI edgeGUI1 = prevNode != null
                        ? graphGUI.getEdgeGUI(graph.getEdge(prevNode, currentNode))
                        : null;
                final EdgeGUI edgeGUI2 = prevNode != null
                        ? graphGUI.getEdgeGUI(graph.getEdge(currentNode, prevNode))
                        : null;
                Platform.runLater(() -> {
                    nodeToDraw.setColor(Color.YELLOW);
                    nodeToDraw.setBorderColor(Color.ORANGE);
                    if (edgeGUI1 != null) {
                        edgeGUI1.setLineColor(Color.ORANGE);
                    }
                    if (edgeGUI2 != null) {
                        edgeGUI2.setLineColor(Color.ORANGE);
                    }
                    GraphVisualizationController.instance
                            .logMessage("[Step " + step + "] Visiting: " + nodeToDraw.getNode().getNodeName());
                });

                // Waiting... so you can actually see what's happening
                Thread.sleep((long) (animationStepTime));
            }
            GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            GraphVisualizationController.instance.logMessage("DFS Complete! Visited " + visited.size() + " nodes");
            GraphVisualizationController.instance.logMessage("Order: " + traversalOrder);
            GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void animateBFS(GraphGUI graphGUI, String startNode) {
        if (GraphVisualizationController.instance == null)
            return;

        try {
            Set<Node> visited = new HashSet<>();
            Deque<Node> queue = new LinkedList<>();
            Deque<Node> prevQueue = new LinkedList<>();
            List<String> traversalOrder = new ArrayList<>();
            Graph graph = graphGUI.getGraph();

            // Start
            int stepCount = 0;
            queue.add(graph.getNode(startNode));

            while (!queue.isEmpty()) {
                if (Thread.currentThread().isInterrupted())
                    break;

                // Get current node (and previous.. for edge drawing)
                Node currentNode = queue.remove();
                Node prevNode = prevQueue.poll();
                if (visited.contains(currentNode))
                    continue;
                visited.add(currentNode);

                // Records the traversal
                traversalOrder.add(currentNode.getNodeName());
                stepCount++;

                // Next nodes
                for (Edge edge : currentNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        prevQueue.add(currentNode);
                    }
                }

                // Draw node and edge with new color
                int step = stepCount;
                final NodeGUI nodeToDraw = graphGUI.getNodeGUI(currentNode);
                final EdgeGUI edgeGUI1 = prevNode != null
                        ? graphGUI.getEdgeGUI(graph.getEdge(prevNode, currentNode))
                        : null;
                final EdgeGUI edgeGUI2 = prevNode != null
                        ? graphGUI.getEdgeGUI(graph.getEdge(currentNode, prevNode))
                        : null;
                Platform.runLater(() -> {
                    nodeToDraw.setColor(Color.YELLOW);
                    nodeToDraw.setBorderColor(Color.ORANGE);
                    if (edgeGUI1 != null) {
                        edgeGUI1.setLineColor(Color.ORANGE);
                    }
                    if (edgeGUI2 != null) {
                        edgeGUI2.setLineColor(Color.ORANGE);
                    }
                    GraphVisualizationController.instance
                            .logMessage("[Step " + step + "] Visiting: " + nodeToDraw.getNode().getNodeName());
                });

                // Waiting... so you can actually see what's happening
                Thread.sleep((long) (animationStepTime));
            }
            GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            GraphVisualizationController.instance.logMessage("BFS Complete! Visited " + visited.size() + " nodes");
            GraphVisualizationController.instance.logMessage("Order: " + traversalOrder);
            GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
