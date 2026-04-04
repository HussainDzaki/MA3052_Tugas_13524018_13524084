package ma3052.core.algorithm;

import java.util.ArrayList;

import ma3052.core.dsu.DisjointSetUnion;
import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class KruskalsAlgorithm {
    public static Graph getMinimumSpanningTree(Graph graph) {
        DisjointSetUnion<Node> dsu = new DisjointSetUnion<>();
        Graph resultGraph = new Graph();
        for (Node node : graph.getNodeList()) {
            resultGraph.addNode(new Node(node.getNodeName(), node.getValue()));
        }
        ArrayList<Edge> sortedEdge = new ArrayList<Edge>(graph.getEdgeList());
        sortedEdge.sort((e1, e2) -> {
            if (e1.getWeight() < e2.getWeight()) return -1;
            if (e1.getWeight() > e2.getWeight()) return 1;
            return 0;
        });
        for (Edge edge :sortedEdge) {
            Node u = edge.getSource();
            Node v = edge.getDestination();
            if (!dsu.isSameSet(u, v)) {
                dsu.unite(u, v);
                resultGraph.addEdge(u.getNodeName(), v.getNodeName(), edge.getWeight());
            }
        }
        return resultGraph;
    }
}