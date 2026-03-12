package ma3052.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ma3052.graph.Edge;
import ma3052.graph.Graph;
import ma3052.graph.Node;

public class CycleDetector {
    public static boolean isBipartite(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            return true;
        }

        // Algoritma Pewarnaan 0 / 1 untuk menentukan bipartite
        HashMap<Long, Integer> nodeColors = new HashMap<>();

        for (Node startNode : graph.getNodeList()) {
            // Jika belum diwarnai melakukan DFS
            if (!nodeColors.containsKey(startNode.getNodeID())) {
                ArrayDeque<Node> queue = new ArrayDeque<>();
                queue.add(startNode);
                nodeColors.put(startNode.getNodeID(), 0); // set awal warna 0

                while (!queue.isEmpty()) {
                    Node currNode = queue.poll();
                    int currColor = nodeColors.get(currNode.getNodeID());
                    int neighborColor = 1 - currColor; // Jadi warna lawan
                    for (Edge edge : currNode.getAdjacencyList()) {
                        Node nextNode = edge.getDestination();
                        long nextNodeID = nextNode.getNodeID();

                        if (!nodeColors.containsKey(nextNodeID)) {
                            nodeColors.put(nextNodeID, neighborColor); // set tetangga jadi warna lawan
                            queue.add(nextNode);
                        } else if (nodeColors.get(nextNodeID) == currColor) {
                            return false; 
                        }
                    }
                }
            }
        }
        return true;
    }

    public static List<Node> getDiameterPath(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            return new ArrayList<Node>();
        }

        List<Node> longestPathFound = new ArrayList<>();

        for (Node startMode : graph.getNodeList()) {
            List<Node> currPath = getFarthestPathDFS(startMode);
            if (currPath.size() > longestPathFound.size()) {
                longestPathFound = currPath;
            }
        }
        return longestPathFound;
    }

    public static List<Node> getFarthestPathDFS(Node startNode) {
        // simpul -> simpul sebelum
        HashMap<Long, Node> pred = new HashMap<>();
        HashMap<Long, Integer> distances = new HashMap<>();

        ArrayDeque<Node> queue = new ArrayDeque<>();
        distances.put(startNode.getNodeID(), 0);
        pred.put(startNode.getNodeID(), null);
        queue.add(startNode);

        Node farhestNode = startNode;

        while (!queue.isEmpty()) {
            Node currNode = queue.poll();
            int currDist = distances.get(currNode.getNodeID());
            if (currDist > distances.get(farhestNode.getNodeID())) {
                farhestNode = currNode;
            }

            for (Edge edge : currNode.getAdjacencyList()) {
                Node neighbor = edge.getDestination();
                Long neighborID = neighbor.getNodeID();

                if (!distances.containsKey(neighborID)) {
                    distances.put(neighborID, currDist + 1);
                    pred.put(neighborID, currNode);
                    queue.add(neighbor);
                }
            }
        }

        return reconstructPath(farhestNode, pred);
    }

    private static List<Node> reconstructPath(Node target, HashMap<Long, Node> pred) {
        ArrayList<Node> path = new ArrayList<>();
        Node step = target;
        while (step != null) {
            path.addFirst(step);
            step = pred.get(step.getNodeID());
        }
        return path;
    }

    public static List<Node> getUndirectedCyclePath(Graph graph) {
        if (graph == null || graph.isEmpty())
            return new ArrayList<Node>();

        HashSet<Long> visited = new HashSet<>();
        HashMap<Long, Long> parentMap = new HashMap<>();
        // Kita simpan referensi Node berdasarkan ID untuk memudahkan rekonstruksi
        HashMap<Long, Node> nodeMap = new HashMap<>();

        for (Node startNode : graph.getNodeList()) {
            if (!visited.contains(startNode.getNodeID())) {
                List<Node> cycle = findUndirectedCycleBFS(startNode, visited, parentMap, nodeMap);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
        }
        return new ArrayList<>(); // Tidak ada siklus
    }

    private static List<Node> findUndirectedCycleBFS(Node startNode, HashSet<Long> visited,
            HashMap<Long, Long> parentMap, HashMap<Long, Node> nodeMap) {
        ArrayDeque<Node> queue = new ArrayDeque<>();

        visited.add(startNode.getNodeID());
        parentMap.put(startNode.getNodeID(), null); // node pertama tidak punya parent
        nodeMap.put(startNode.getNodeID(), startNode);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            Long currID = curr.getNodeID();
            for (Edge edge : curr.getAdjacencyList()) {
                Node neighbor = edge.getDestination();
                Long neighborID = neighbor.getNodeID();
                nodeMap.put(neighborID, neighbor);

                // Jika tetangga sudah dikunjungi dan bukan parent dari node saat ini
                if (visited.contains(neighborID)) {
                    if (parentMap.get(currID) == null || neighborID != parentMap.get(currID)) {
                        return reconstructUndirectedCycle(curr, neighbor, parentMap, nodeMap);
                    }
                } else {
                    visited.add(neighborID);
                    parentMap.put(neighborID, currID);
                    queue.add(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    private static List<Node> reconstructUndirectedCycle(Node nodeA, Node nodeB,
            HashMap<Long, Long> parentMap, HashMap<Long, Node> nodeMap) {
        ArrayList<Node> pathA = new ArrayList<>();
        Node currA = nodeA;
        while (currA != null) {
            pathA.add(currA);
            Long parentID = parentMap.get(currA.getNodeID());
            currA = (parentID == null || parentID == -1) ? null : nodeMap.get(parentID);
        }
        ArrayList<Node> pathB = new ArrayList<>();
        Node currB = nodeB;
        while (currB != null) {
            pathB.add(currB);
            Long parentID = parentMap.get(currB.getNodeID());
            currB = (parentID == null || parentID == -1) ? null : nodeMap.get(parentID);
        }

        // cari titik pertemuan terakhir
        int i = pathA.size() - 1;
        int j = pathB.size() - 1;
        Node intersection = null;

        while (i >= 0 && j >= 0 && pathA.get(i) == pathB.get(j)) {
            intersection = pathA.get(i);
            i--;
            j--;
        }

        List<Node> result = new ArrayList<>();
        for (int k = 0; k <= i + 1; k++) {
            result.add(pathA.get(k));
        }
        for (int k = j; k >= 0; k--) {
            result.add(pathB.get(k));
        }

        result.add(nodeA);
        return result;
    }

    public static List<Node> getDirectedCyclePath(Graph graph) {
        if (graph == null || graph.isEmpty())
            return new ArrayList<>();

        // Map untuk status: 0 = Unvisited, 1 = Visiting, 2 = Visited
        HashMap<Long, Integer> status = new HashMap<>();
        HashMap<Long, Node> parentMap = new HashMap<>();
        HashMap<Long, Node> nodeMap = new HashMap<>();

        for (Node startNode : graph.getNodeList()) {
            if (!status.containsKey(startNode.getNodeID())) {
                List<Node> cycle = findCycleDFS(startNode, status, parentMap, nodeMap);
                if (!cycle.isEmpty())
                    return cycle;
            }
        }
        return new ArrayList<>();
    }

    private static List<Node> findCycleDFS(Node current,
            HashMap<Long, Integer> status,
            HashMap<Long, Node> parentMap,
            HashMap<Long, Node> nodeMap) {
        long currID = current.getNodeID();
        nodeMap.put(currID, current);
        status.put(currID, 1); // set status ke visiting

        for (Edge edge : current.getAdjacencyList()) {
            Node neighbor = edge.getDestination();
            long neighborID = neighbor.getNodeID();

            // jika menemukan status proses = 1, ada siklus
            if (status.getOrDefault(neighborID, 0) == 1) {
                return reconstructDirectedCycle(current, neighbor, parentMap, nodeMap);
            }

            if (status.getOrDefault(neighborID, 0) == 0) {
                parentMap.put(neighborID, current);
                List<Node> cycle = findCycleDFS(neighbor, status, parentMap, nodeMap);
                if (!cycle.isEmpty())
                    return cycle;
            }
        }

        status.put(currID, 2);
        return new ArrayList<>();
    }

    private static List<Node> reconstructDirectedCycle(Node endNode, Node startNode,
            HashMap<Long, Node> parentMap,
            HashMap<Long, Node> nodeMap) {
        LinkedList<Node> cycle = new LinkedList<>();
        cycle.addFirst(startNode);

        Node curr = endNode;
        // Runut balik dari endNode sampai ketemu startNode lagi
        while (curr != null && curr != startNode) {
            cycle.addFirst(curr);
            curr = parentMap.get(curr.getNodeID());
        }

        cycle.addLast(startNode); // Tutup siklus (A-B-C-A)
        return cycle;
    }

    public static List<Node> getGirthPath(Graph graph) {
        if (graph == null || graph.isEmpty())
            return new ArrayList<>();

        List<Node> shortestCycleFound = new ArrayList<>();
        int minSize = Integer.MAX_VALUE;

        for (Node startNode : graph.getNodeList()) {
            List<Node> currentCycle = findShortestCycleFromNode(startNode, minSize);

            if (!currentCycle.isEmpty() && currentCycle.size() < minSize) {
                shortestCycleFound = currentCycle;
                minSize = currentCycle.size();
            }

            if (minSize == 3)
                break;
        }

        return shortestCycleFound;
    }

    private static List<Node> findShortestCycleFromNode(Node startNode, int globalMin) {
        HashMap<Long, Integer> distances = new HashMap<>();
        HashMap<Long, Node> parentMap = new HashMap<>();
        ArrayDeque<Node> queue = new ArrayDeque<>();

        distances.put(startNode.getNodeID(), 0);
        parentMap.put(startNode.getNodeID(), null);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            int currDist = distances.get(curr.getNodeID());

            if (currDist * 2 >= globalMin)
                break;

            for (Edge edge : curr.getAdjacencyList()) {
                Node neighbor = edge.getDestination();
                long neighborID = neighbor.getNodeID();

                if (!distances.containsKey(neighborID)) {
                    distances.put(neighborID, currDist + 1);
                    parentMap.put(neighborID, curr);
                    queue.add(neighbor);
                } else if (parentMap.get(curr.getNodeID()) == null
                        || neighborID != parentMap.get(curr.getNodeID()).getNodeID()) {
                    return reconstructGirthPath(curr, neighbor, startNode, parentMap);
                }
            }
        }
        return new ArrayList<>();
    }

    private static List<Node> reconstructGirthPath(Node u, Node v, Node root, HashMap<Long, Node> parentMap) {
        LinkedList<Node> path = new LinkedList<>();

        ArrayList<Node> pathA = new ArrayList<>();
        Node step = u;
        while (step != null) {
            pathA.add(step);
            step = parentMap.get(step.getNodeID());
        }

        ArrayList<Node> pathB = new ArrayList<>();
        step = v;
        while (step != null) {
            pathB.add(step);
            step = parentMap.get(step.getNodeID());
        }

        // cari titik pertemuan terakhir
        int i = pathA.size() - 1;
        int j = pathB.size() - 1;
        Node lca = null;

        while (i >= 0 && j >= 0 && pathA.get(i) == pathB.get(j)) {
            lca = pathA.get(i);
            i--;
            j--;
        }

        for (int k = 0; k <= i + 1; k++) {
            path.add(pathA.get(k));
        }
        for (int k = j; k >= 0; k--) {
            path.add(pathB.get(k));
        }
        path.add(u); // Close the cycle

        return new ArrayList<>(path);
    }

    public static String getResultPathString(List<Node> path) {
        String result = "";
        boolean first = true;
        for (Node node : path) {
            if (first) {
                result += node.getNodeName();
                first = false;
            } else {
                result += " -> ";
                result += node.getNodeName();
            }
        }
        return result;
    }
}
