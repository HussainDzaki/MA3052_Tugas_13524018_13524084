package ma3052.core.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphFactory {
    private static final int MAX_POWER_OF_2 = 24;

    public enum NodeNameOption {
        ZeroIndexed, // 0 1 2 3 4 ...
        OneIndexed, // 1 2 3 4 5 ...
        Alphabetic // A B C D E .... Z AA AB ...
    };

    private static String getNodeName(int index, NodeNameOption option) {
        switch (option) {
            case ZeroIndexed:
                return Integer.toString(index);

            case OneIndexed:
                return Integer.toString(index + 1);

            case Alphabetic:
                String result = new String();
                do {
                    result += (char) ('A' + index % 26);
                    index /= 26;
                } while (index > 0);
                return new StringBuilder(result).reverse().toString();

            default:
                return "";
        }
    }

    public enum BipartiteNameOption {
        LR,
        UV,
        AB,
        XY,
        NONE
    };

    private static String getBipartiteName(boolean left, BipartiteNameOption option) {
        switch (option) {
            case LR:
                return left ? "L" : "R";

            case UV:
                return left ? "U" : "V";

            case AB:
                return left ? "A" : "B";

            case XY:
                return left ? "X" : "Y";

            default:
                return "";
        }
    }

    public static Graph createCompleteGraph(int n) {
        return createCompleteGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createCompleteGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                graph.addEdge(getNodeName(i, option), getNodeName(j, option));
            }
        }
        return graph;
    }

    public static Graph createCompleteBipartiteGraph(int n, int M) {
        return createCompleteBipartiteGraph(n, M, BipartiteNameOption.UV, NodeNameOption.OneIndexed);
    }

    public static Graph createCompleteBipartiteGraph(int n, int M,
            BipartiteNameOption bipartiteNameOption,
            NodeNameOption nodeNameOption) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getBipartiteName(true, bipartiteNameOption) + getNodeName(i, nodeNameOption)));
        }
        for (int j = 0; j < M; j++) {
            graph.addNode(new Node(getBipartiteName(false, bipartiteNameOption)
                    + getNodeName(j + (bipartiteNameOption == BipartiteNameOption.NONE ? n : 0), nodeNameOption)));
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < M; j++) {
                graph.addEdge(
                        getBipartiteName(true, bipartiteNameOption) + getNodeName(i, nodeNameOption),
                        getBipartiteName(false, bipartiteNameOption) + getNodeName(
                                (bipartiteNameOption == BipartiteNameOption.NONE ? n : 0), nodeNameOption));
            }
        }
        return graph;
    }

    public static Graph createPathGraph(int n) {
        return createPathGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createPathGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 1; i < n; i++) {
            graph.addEdge(getNodeName(i - 1, option), getNodeName(i, option));
        }
        return graph;
    }

    public static Graph createCycleGraph(int n) {
        return createCycleGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createCycleGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            graph.addEdge(getNodeName(i, option), getNodeName((i + 1) % n, option));
        }
        return graph;
    }

    public static Graph createCompleteBinaryGraph(int depth) {
        return createCompleteBinaryGraph(depth, NodeNameOption.OneIndexed);
    }

    public static Graph createCompleteBinaryGraph(int depth, NodeNameOption option) {
        if (depth > MAX_POWER_OF_2) {
            throw new IllegalArgumentException("Too much depth for complete binary tree (> " + MAX_POWER_OF_2 + ")");
        }
        Graph graph = new Graph();
        for (int d = 0; d < depth; d++) {
            for (int i = 0; i < (1 << d); i++) {
                graph.addEdge(getNodeName(i, option), getNodeName(2 * i, option));
                graph.addEdge(getNodeName(i, option), getNodeName(2 * i + 1, option));

            }
        }
        return graph;
    }

    public static Graph createStarGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 1; i < n; i++) {
            graph.addEdge(getNodeName(0, option), getNodeName(i, option));
        }
        return graph;
    }

    public static Graph createWheelGraph(int n) {
        return createWheelGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createWheelGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 1; i < n; i++) {
            graph.addEdge(getNodeName(0, option), getNodeName(i, option));
            graph.addEdge(getNodeName(i, option), getNodeName(i % (n - 1) + 1, option));
        }
        return graph;
    }

    public static Graph createPrismGraph(int n) {
        return createPrismGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createPrismGraph(int n, NodeNameOption option) {
        return createPrismGraph(n, 2, option);
    }

    public static Graph createPrismGraph(int n, int c, NodeNameOption option) {
        if (c < 0) {
            throw new IllegalArgumentException("c must be a positive number. Got: c = " + c);
        }
        Graph graph = new Graph();
        for (int i = 0; i < c * n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < c; j++) {
                graph.addEdge(getNodeName(i + j * n, option), getNodeName((i + 1) % n + j * n, option));
                if (j > 0) {
                    graph.addEdge(getNodeName(i + (j - 1) * n, option), getNodeName(i + j * n, option));
                }
            }
        }
        return graph;
    }

    public static Graph createLadderGraph(int n) {
        return createLadderGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createLadderGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < 2 * n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            graph.addEdge(getNodeName(i, option), getNodeName((i + 1) % n, option));
            graph.addEdge(getNodeName(i + n, option), getNodeName((i + 1 % n) + n, option));
            graph.addEdge(getNodeName(i, option), getNodeName(i + n, option));
        }
        return graph;
    }

    public static Graph createMobiusLadderGraph(int n) {
        return createMobiusLadderGraph(n, NodeNameOption.OneIndexed);
    }

    public static Graph createMobiusLadderGraph(int n, NodeNameOption option) {
        Graph graph = new Graph();
        for (int i = 0; i < 2 * n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            if (i < n - 1) {
                graph.addEdge(getNodeName(i, option), getNodeName(i + 1, option));
                graph.addEdge(getNodeName(i + n, option), getNodeName(i + 1 + n, option));
            } else {
                graph.addEdge(getNodeName(i, option), getNodeName(n, option));
                graph.addEdge(getNodeName(i + n, option), getNodeName(0, option));

            }
            graph.addEdge(getNodeName(i, option), getNodeName(i + n, option));
        }
        return graph;
    }

    public static Graph createPetersenGraph(int n, int k) {
        return createPetersenGraph(n, k, NodeNameOption.OneIndexed);
    }

    public static Graph createPetersenGraph(int n, int k, NodeNameOption option) {
        if (k > n / 2) {
            throw new IllegalArgumentException("k must be less or equal than n / 2. Got: n = " + n + ", k = " + k);
        }
        Graph graph = new Graph();
        for (int i = 0; i < 2 * n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int i = 0; i < n; i++) {
            graph.addEdge(getNodeName(i, option), getNodeName((i + k) % n, option));
            graph.addEdge(getNodeName(i + n, option), getNodeName((i + 1) % n + n, option));
            graph.addEdge(getNodeName(i, option), getNodeName(i + n, option));
        }
        return graph;
    }

    public static Graph createCirculantGraph(int n, int... s) {
        return createCirculantGraph(n, NodeNameOption.OneIndexed, s);
    }

    public static Graph createCirculantGraph(int n, NodeNameOption option, int... s) {
        for (int si : s) {
            if (si <= 0 || si >= n) {
                throw new IllegalArgumentException("All value of s must be between 1 and n. Got s_i = " + si);
            }
        }
        Graph graph = new Graph();
        for (int i = 0; i < n; i++) {
            graph.addNode(new Node(getNodeName(i, option)));
        }
        for (int si : s) {
            for (int i = 0; i < n; i++) {
                graph.addEdge(getNodeName(0, option), getNodeName((i + si) % n, option));
            }
        }
        return graph;
    }

    public static Graph createHypercubeGraph(int dimension) {
        if (dimension < 0) {
            throw new IllegalArgumentException("Dimension of hypercube must be a positive number.");
        }
        if (dimension > MAX_POWER_OF_2) {
            throw new IllegalArgumentException("Dimension of hypercube is too big (> " + MAX_POWER_OF_2 + ")");
        }
        Graph graph = new Graph(new Node(""));
        for (int d = 1; d <= dimension; d++) {
            List<Node> nodes = new ArrayList<>(graph.getNodeList());
            List<Edge> edges = new ArrayList<>(graph.getEdgeList());
            for (Edge edge : edges) {
                graph.addEdge("1" + edge.getSource().getNodeName(), "1" + edge.getDestination().getNodeName());
            }
            for (Node node : nodes) {
                Node pairNode = graph.hasNode("1" + node.getNodeName())
                        ? graph.getNode("1" + node.getNodeName())
                        : new Node("1" + node.getNodeName());
                graph.addNode(pairNode);
                graph.addEdge(node, pairNode);
                node.setNodeName("0" + node.getNodeName());
            }
        }
        return graph;
    }
}
