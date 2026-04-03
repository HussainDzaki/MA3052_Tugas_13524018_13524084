package ma3052.gui.animation;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import ma3052.core.dsu.DisjointSetUnion;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualGUIController;
import ma3052.gui.graph.EdgeGUI;
import ma3052.gui.graph.GraphGUI;
import ma3052.gui.graph.NodeGUI;

public class KruskalAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    public static void setAnimationStepTime(long animationStepTime) {
        KruskalAnimation.animationStepTime = animationStepTime;
    }

    public static void animate(GraphGUI graphGUI) {
        if (GraphVisualGUIController.instance == null)
            return;

        try {
            DisjointSetUnion<Node> dsu = new DisjointSetUnion<>();
            Graph graph = graphGUI.getGraph();
            ArrayList<Edge> sortedEdge = new ArrayList<Edge>(graph.getEdgeList());
            sortedEdge.sort((e1, e2) -> {
                if (e1.getWeight() < e2.getWeight())
                    return -1;
                if (e1.getWeight() > e2.getWeight())
                    return 1;
                return 0;
            });
            int stepCount = 0;
            double totalWeight = 0;
            for (Edge edge : sortedEdge) {
                stepCount++;
                Node u = edge.getSource();
                Node v = edge.getDestination();
                if (!dsu.isSameSet(u, v)) {
                    dsu.unite(u, v);
                    totalWeight += edge.getWeight();
                    int step = stepCount;
                    final NodeGUI uGUI = graphGUI.getNodeGUI(u);
                    final NodeGUI vGUI = graphGUI.getNodeGUI(v);
                    final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(edge);
                    Platform.runLater(() -> {
                        if (uGUI != null) {
                            uGUI.setColor(Color.YELLOW);
                            uGUI.setBorderColor(Color.ORANGE);
                        }
                        if (vGUI != null) {
                            vGUI.setColor(Color.YELLOW);
                            vGUI.setBorderColor(Color.ORANGE);
                        }
                        if (edgeGUI != null) {
                            edgeGUI.setLineColor(Color.ORANGE);
                        }
                        GraphVisualGUIController.instance
                                .logMessage("[Step " + step + "] Checking edge ("
                                        + u.getNodeName() + ", " + v.getNodeName() + ")"
                                        + " -> YES");
                    });
                } else {
                    int step = stepCount;
                    final EdgeGUI edgeGUI = graphGUI.getEdgeGUI(edge);
                    Platform.runLater(() -> {
                        if (edgeGUI != null) {
                            edgeGUI.setLineColor(Color.GRAY);
                        }
                        GraphVisualGUIController.instance
                                .logMessage("[Step " + step + "] Checking edge ("
                                        + u.getNodeName() + ", " + v.getNodeName() + ")"
                                        + " -> NO");
                    });
                }
                Thread.sleep((long) (animationStepTime));
                // Waiting... so you can actually see what's happening
            }

            GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
            GraphVisualGUIController.instance.logMessage("Kruskal's Algorithm Completed!");
            GraphVisualGUIController.instance.logMessage("Total Weight of Spanning Tree: " + totalWeight);
            GraphVisualGUIController.instance.logMessage("═══════════════════════════════════");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
