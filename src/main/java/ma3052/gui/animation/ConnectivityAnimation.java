package ma3052.gui.animation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import ma3052.controller.GraphVisualGUIController;
import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;
import ma3052.gui.EdgeGUI;
import ma3052.gui.GraphGUI;
import ma3052.gui.NodeGUI;

public class ConnectivityAnimation {
     private static volatile long animationStepTime = 500; // in milliseconds

     public static void setAnimationStepTime(long animationStepTime) {
          ConnectivityAnimation.animationStepTime = animationStepTime;
     }

     public static void animate(GraphGUI graphGUI) {
          if (GraphVisualGUIController.instance == null)
               return;

          try {
               Set<Node> visited = new HashSet<>();
               Deque<Node> queue = new LinkedList<>();
               Deque<Node> prevQueue = new LinkedList<>();
               List<String> traversalOrder = new ArrayList<>();
               Graph graph = graphGUI.getGraph();

               // Start
               int stepCount = 0;
               queue.add(graph.getNodeList().get(0));

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
                         GraphVisualGUIController.instance
                                   .logMessage("[Step " + step + "] Visiting: " + nodeToDraw.getNode().getNodeName());
                    });

                    // Waiting... so you can actually see what's happening
                    Thread.sleep((long) (animationStepTime));
               }

               boolean isConnected = true;
               for (Node node : graph.getNodeList()) {
                    if (visited.contains(node))
                         continue;
                    isConnected = false;
                    final NodeGUI nodeToDraw = graphGUI.getNodeGUI(node);
                    Platform.runLater(() -> {
                         nodeToDraw.setColor(Color.ORANGE);
                         nodeToDraw.setBorderColor(Color.RED);
                    });
               }

               GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
               if (isConnected) {
                    GraphVisualGUIController.instance.logMessage("The graph is connected!");
               }
               else {
                    GraphVisualGUIController.instance.logMessage("The graph is not connected!");
               }
               GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
          } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
          }
     }
}
