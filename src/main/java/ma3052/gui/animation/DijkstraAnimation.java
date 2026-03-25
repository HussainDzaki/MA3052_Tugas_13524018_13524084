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

import ma3052.controller.GraphVisualGUIController;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.gui.GraphGUI;
import ma3052.gui.NodeGUI;

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

    private static final Color PROCESSED_COLOR = Color.web("#f2ec2c");
    private static final Color VISITED_COLOR = Color.web("#28a13a");

    public static void setAnimationStepTime(long animationStepTime) {
        DijkstraAnimation.animationStepTime = animationStepTime;
    }

    public static void animate(GraphGUI graphGUI, String startNode, String endNode) {
        if (GraphVisualGUIController.instance == null) { // tidak ada graph
            return;
        }
        try {
            Graph graph = graphGUI.getGraph();
            Node sourceNode = graph.getNode(startNode);
            Node destinationNode = graph.getNode(endNode);
            Map<Node, Node> visited = new HashMap<>();
            Map<Node, Double> tabelVertex = new HashMap<>();
            PriorityQueue<NodeEntry> nodeQueue = new PriorityQueue<>();
            Set<Node> settledNodes = new HashSet<>();

            // Initialization
            for (Node node : graph.getNodeList()) {
                if (node == sourceNode) {
                    tabelVertex.put(node, 0.0);
                } else {
                    tabelVertex.put(node, Double.MAX_VALUE);
                }
            }

            nodeQueue.add(new NodeEntry(sourceNode, 0.0));
            while (!nodeQueue.isEmpty()) {
                NodeEntry entry = nodeQueue.poll();
                Node currNode = entry.node;
                if (settledNodes.contains(currNode)) {
                    continue;
                }
                settledNodes.add(currNode);
                final NodeGUI nodeToDraw = graphGUI.getNodeGUI(currNode);
                Platform.runLater(() -> {
                    nodeToDraw.setColor(PROCESSED_COLOR);
                });

                for (Edge edge : currNode.getAdjacencyList()) {
                    Node neighbor = edge.getDestination();
                    if (settledNodes.contains(neighbor))
                        continue;
                    Double distToNeighbor = tabelVertex.get(currNode) + edge.getWeight();
                    if (distToNeighbor < tabelVertex.get(neighbor)) {
                        tabelVertex.put(neighbor, distToNeighbor);
                        visited.put(neighbor, currNode);
                        nodeQueue.add(new NodeEntry(neighbor, distToNeighbor));
                    }

                }
                Thread.sleep((long) (animationStepTime));
            }

            Node currBack = destinationNode;
            if (tabelVertex.get(destinationNode) == Double.MAX_VALUE) {
                GraphVisualGUIController.instance.logMessage("═════════════════");
                GraphVisualGUIController.instance.logMessage("Dijkstra Complete!");
                GraphVisualGUIController.instance.logMessage("NO PATH FOUND!");
                GraphVisualGUIController.instance.logMessage("═════════════════");
            }

            List<Node> path = new ArrayList<>();

            while (currBack != null) {
                path.add(currBack);
                currBack = visited.get(currBack);
            }

            Collections.reverse(path);

            for (int i = 0; i < path.size(); i++) {
                Node nodeNow = path.get(i);
                Platform.runLater(() -> {
                    graphGUI.getNodeGUI(nodeNow).setColor(VISITED_COLOR);
                });
                Thread.sleep(animationStepTime);
                if (i < path.size() - 1) {
                    Node nextNode = path.get(i + 1);
                    Edge edgeToColor = graph.getEdge(nodeNow, nextNode);

                    if (edgeToColor != null) {
                        Platform.runLater(() -> {
                            graphGUI.getEdgeGUI(edgeToColor).setLineColor(VISITED_COLOR);
                        });
                        Thread.sleep(animationStepTime);
                    }

                }

            }

            GraphVisualGUIController.instance.logMessage("═══════════════════");
            GraphVisualGUIController.instance.logMessage("Dijkstra Complete!");
            GraphVisualGUIController.instance.logMessage("Path FOUND " + pathToString(path));
            GraphVisualGUIController.instance.logMessage("════════════════════");

        } catch (Exception e) {
            // TODO: handle exception
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
                res += " -> " + path.get(i).getNodeName().toString();
            }
        }
        return res;
    }

}
