package ma3052.gui.animation;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.EdgeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class PathAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    public static void setAnimationStepTime(long animationStepTime) {
        PathAnimation.animationStepTime = animationStepTime;
    }

    public static void animateDFS(GraphGUI graphGUI, String startNode, String endNode) {
        if (GraphVisualizationController.instance == null)
            return;

        try {
            Map<Node, Node> visited = new HashMap<>();
            Deque<Node> stack = new LinkedList<>();
            Deque<Node> prevStack = new LinkedList<>();
            Graph graph = graphGUI.getGraph();
            Node sourceNode = graph.getNode(startNode);
            Node destinationNode = graph.getNode(endNode);

            // Start
            int stepCount = 0;
            stack.push(sourceNode);

            while (!stack.isEmpty()) {
                if (Thread.currentThread().isInterrupted())
                    break;

                // Get current node (and previous.. for edge drawing)
                Node currentNode = stack.pop();
                Node prevNode = prevStack.poll();
                if (visited.containsKey(currentNode))
                    continue;
                visited.put(currentNode, prevNode);

                // Records the step
                stepCount++;

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

                // Has reached destination
                if (currentNode == destinationNode)
                    break;

                // Next nodes
                for (Edge edge : currentNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (!visited.containsKey(neighbor)) {
                        stack.push(neighbor);
                        prevStack.push(currentNode);
                    }
                }
            }

            if (visited.containsKey(destinationNode)) {
                // Get the path by backtracking and draw the path
                List<String> path = new LinkedList<>();
                Node backtrackNode = destinationNode;
                while (backtrackNode != sourceNode) {
                    path.addFirst(backtrackNode.getNodeName());
                    Node prevNode = visited.get(backtrackNode);

                    final NodeGUI nodeToDraw = graphGUI.getNodeGUI(backtrackNode);
                    final EdgeGUI edgeGUI1 = prevNode != null
                            ? graphGUI.getEdgeGUI(graph.getEdge(prevNode, backtrackNode))
                            : null;
                    final EdgeGUI edgeGUI2 = prevNode != null
                            ? graphGUI.getEdgeGUI(graph.getEdge(backtrackNode, prevNode))
                            : null;
                    Platform.runLater(() -> {
                        nodeToDraw.setColor(Color.GREENYELLOW);
                        nodeToDraw.setBorderColor(Color.GREEN);
                        if (edgeGUI1 != null) {
                            edgeGUI1.setLineColor(Color.GREEN);
                        }
                        if (edgeGUI2 != null) {
                            edgeGUI2.setLineColor(Color.GREEN);
                        }
                    });

                    backtrackNode = prevNode;
                }
                // Last node
                path.addFirst(sourceNode.getNodeName());
                final NodeGUI nodeToDraw = graphGUI.getNodeGUI(sourceNode);
                Platform.runLater(() -> {
                    nodeToDraw.setColor(Color.GREENYELLOW);
                    nodeToDraw.setBorderColor(Color.GREEN);
                });

                // Convert path to string
                // Ex: 1 -> 4 -> 3 -> 2
                String pathString = new String();
                boolean first = true;
                for (String node : path) {
                    if (first) {
                        pathString += node;
                        first = false;
                    } else {
                        pathString += " -> " + node;
                    }
                }

                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
                GraphVisualizationController.instance.logMessage("DFS Complete! Visited " + visited.size() + " nodes");
                GraphVisualizationController.instance.logMessage("Path: " + pathString);
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            } else {
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
                GraphVisualizationController.instance.logMessage("DFS Complete! Visited " + visited.size() + " nodes");
                GraphVisualizationController.instance
                        .logMessage("There is no path from " + startNode + " to " + endNode);
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void animateBFS(GraphGUI graphGUI, String startNode, String endNode) {
        if (GraphVisualizationController.instance == null)
            return;

        try {
            Map<Node, Node> visited = new HashMap<>();
            Deque<Node> queue = new LinkedList<>();
            Deque<Node> prevQueue = new LinkedList<>();
            Graph graph = graphGUI.getGraph();
            Node sourceNode = graph.getNode(startNode);
            Node destinationNode = graph.getNode(endNode);

            // Start
            int stepCount = 0;
            queue.add(sourceNode);

            while (!queue.isEmpty()) {
                if (Thread.currentThread().isInterrupted())
                    break;

                // Get current node (and previous.. for edge drawing)
                Node currentNode = queue.remove();
                Node prevNode = prevQueue.poll();
                if (visited.containsKey(currentNode))
                    continue;
                visited.put(currentNode, prevNode);

                // Records the step
                stepCount++;

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

                // Has reached destination
                if (currentNode == destinationNode)
                    break;

                // Next nodes
                for (Edge edge : currentNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (!visited.containsKey(neighbor)) {
                        queue.add(neighbor);
                        prevQueue.add(currentNode);
                    }
                }
            }

            if (visited.containsKey(destinationNode)) {
                // Get the path by backtracking
                List<String> path = new LinkedList<>();
                Node backtrackNode = destinationNode;
                while (backtrackNode != sourceNode) {
                    path.addFirst(backtrackNode.getNodeName());
                    Node prevNode = visited.get(backtrackNode);

                    final NodeGUI nodeToDraw = graphGUI.getNodeGUI(backtrackNode);
                    final EdgeGUI edgeGUI1 = prevNode != null
                            ? graphGUI.getEdgeGUI(graph.getEdge(prevNode, backtrackNode))
                            : null;
                    final EdgeGUI edgeGUI2 = prevNode != null
                            ? graphGUI.getEdgeGUI(graph.getEdge(backtrackNode, prevNode))
                            : null;
                    Platform.runLater(() -> {
                        nodeToDraw.setColor(Color.GREENYELLOW);
                        nodeToDraw.setBorderColor(Color.GREEN);
                        if (edgeGUI1 != null) {
                            edgeGUI1.setLineColor(Color.GREEN);
                        }
                        if (edgeGUI2 != null) {
                            edgeGUI2.setLineColor(Color.GREEN);
                        }
                    });

                    backtrackNode = prevNode;
                }
                // Last node
                path.addFirst(sourceNode.getNodeName());
                final NodeGUI nodeToDraw = graphGUI.getNodeGUI(sourceNode);
                Platform.runLater(() -> {
                    nodeToDraw.setColor(Color.GREENYELLOW);
                    nodeToDraw.setBorderColor(Color.GREEN);
                });

                // Convert path to string
                // Ex: 1 -> 4 -> 3 -> 2
                String pathString = new String();
                boolean first = true;
                for (String node : path) {
                    if (first) {
                        pathString += node;
                        first = false;
                    } else {
                        pathString += " -> " + node;
                    }
                }

                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
                GraphVisualizationController.instance.logMessage("BFS Complete! Visited " + visited.size() + " nodes");
                GraphVisualizationController.instance.logMessage("Path: " + pathString);
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            } else {
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
                GraphVisualizationController.instance.logMessage("BFS Complete! Visited " + visited.size() + " nodes");
                GraphVisualizationController.instance
                        .logMessage("There is no path from " + startNode + " to " + endNode);
                GraphVisualizationController.instance.logMessage("═══════════════════════════════════");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
