package ma3052.gui.animation;

import java.util.List;

import ma3052.core.algorithm.GraphTraversal;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;
import ma3052.gui.controller.GraphVisualizationController;
import ma3052.gui.graph.GraphGUI;

public class BandwidthAnimation {
    private static volatile long animationStepTime = 500; // in milliseconds

    public static void setAnimationStepTime(long animationStepTime) {
        BandwidthAnimation.animationStepTime = Math.max(1, animationStepTime);
    }

    public static void animate(GraphGUI graphGUI) {
        Graph graph = graphGUI.getGraph();
        List<Node> bestOrder = null;
        long bestBandwidth = 0;
        for (Node startNode : graph.getNodeList()) {
            List<Node> order = GraphTraversal.traversalOrderBFS(graph, startNode);
            int i = 1;
            for (Node node : order) {
                node.setValue(i);
                node.setNodeName(Integer.toString(i));
                i++;
            }
            long currentBandwidth = 0;
            for (Node node : graph.getNodeList()) {
                for (Edge edge : node.getAdjacencyList()) {
                    currentBandwidth = (long) Math.max((double) currentBandwidth, Math.abs(edge.getSource().getValue()
                            - edge.getDestination().getValue()));
                }
            }
            if (bestOrder == null || currentBandwidth < bestBandwidth) {
                bestOrder = order;
                bestBandwidth = currentBandwidth;
            }
            try {
                Thread.sleep(animationStepTime);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
        int i = 1;
        for (Node node : bestOrder) {
            node.setValue(i);
            node.setNodeName(Integer.toString(i));
            i++;
        }

        GraphVisualizationController.instance.logMessage("Possible Minimum Bandwidth ≤ " + bestBandwidth);
    }
}
