package ma3052.core.driver;

import java.util.List;
import java.util.Scanner;

import ma3052.core.algorithm.GraphComponent;
import ma3052.core.algorithm.GraphTraversal;
import ma3052.core.algorithm.PathSearch;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class MainDriver {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Program Graf");
        System.out.println("Pilihan:");
        System.out.println("[1] Cari urutan traversal dari node tertentu");
        System.out.println("[2] Apakah ada lintasan dari suatu node ke node lain dalam graf");
        System.out.println("[3] Apakah graf merupakan graf terhubung");
        System.out.println("");
        System.out.print("Masukkan angka pada pilihan: ");
        int pilihan = SCANNER.nextInt();
        boolean ulangi = true;
        while (ulangi) {
            ulangi = false;
            switch (pilihan) {
                case 1:
                    traversalDriver();
                    break;

                case 2:
                    pathDriver();
                    break;

                case 3:
                    connectivityDriver();
                    break;

                default:
                    ulangi = true;
                    break;
            }
        }
    }

    private static Graph inputUndirectedGraph() {
        Scanner scanner = MainDriver.SCANNER;

        int nodeCount, edgesCount;
        System.out.print("Masukkan banyak simpul: ");
        nodeCount = scanner.nextInt();
        System.out.print("Masukkan banyak sisi: ");
        edgesCount = scanner.nextInt();

        Graph graph = new Graph();
        for (int i = 0; i < nodeCount; i++) {
            System.out.print(String.format("Masukkan nama node ke-%d: ", i + 1));
            Node n = new Node(scanner.next());
            graph.addNode(n);
        }

        for (int i = 0; i < edgesCount; i++) {
            System.out.print(String.format("Masukkan sisi ke-%d: ", i + 1));
            String startNodeName = scanner.next();
            String endNodeName = scanner.next();
            Node startNode = graph.getNode(startNodeName);
            Node endNode = graph.getNode(endNodeName);
            graph.addUndirectedEdge(startNode, endNode);
        }

        return graph;
    }

    private static Graph inputDirectedGraph() {
        Scanner scanner = MainDriver.SCANNER;

        int nodeCount, edgesCount;
        System.out.print("Masukkan banyak simpul: ");
        nodeCount = scanner.nextInt();
        System.out.print("Masukkan banyak sisi: ");
        edgesCount = scanner.nextInt();

        Graph graph = new Graph();
        for (int i = 0; i < nodeCount; i++) {
            System.out.print(String.format("Masukkan nama node ke-%d: ", i + 1));
            Node n = new Node(scanner.next());
            graph.addNode(n);
        }

        for (int i = 0; i < edgesCount; i++) {
            System.out.print(String.format("Masukkan sisi ke-%d: ", i + 1));
            String startNodeName = scanner.next();
            String endNodeName = scanner.next();
            Node startNode = graph.getNode(startNodeName);
            Node endNode = graph.getNode(endNodeName);
            graph.addDirectedEdge(startNode, endNode);
        }

        return graph;
    }

    private static void traversalDriver() {
        System.out.println("Pilihan:");
        System.out.println("[1] Graf tak berarah");
        System.out.println("[2] Graf berarah");
        System.out.println("");
        Graph graf = null;
        boolean ulangi = true;
        while (ulangi) {
            System.out.print("Masukkan tipe graf: ");
            int pilihan = SCANNER.nextInt();
            ulangi = false;
            switch (pilihan) {
                case 1:
                    graf = inputUndirectedGraph();
                    break;

                case 2:
                    graf = inputDirectedGraph();
                    break;

                default:
                    ulangi = true;
                    break;
            }
        }
        System.out.println();

        Node startNode = null;
        while (startNode == null) {
            System.out.print("Masukkan nama node mulai traversal: ");
            String startNodeName = SCANNER.next();
            startNode = graf.getNode(startNodeName);
        }
        System.out.println();

        System.out.println("Pilihan:");
        System.out.println("[1] Depth First Search");
        System.out.println("[2] Breath First Search");
        System.out.println("");

        List<Node> hasil = null;
        ulangi = true;
        while (ulangi) {
            System.out.print("Masukkan jenis algoritma yang dipakai: ");
            int pilihan = SCANNER.nextInt();
            ulangi = false;
            switch (pilihan) {
                case 1:
                    hasil = GraphTraversal.traversalOrderDFS(graf, startNode);
                    break;

                case 2:
                    hasil = GraphTraversal.traversalOrderBFS(graf, startNode);
                    break;

                default:
                    ulangi = true;
                    break;
            }
        }
        System.out.println();

        System.out.println("Hasil urutan traversal: ");
        boolean pertama = true;
        for (Node node : hasil) {
            if (!pertama) {
                System.out.print(" -> ");
            } else {
                pertama = false;
            }
            System.out.print(node.getNodeName());
        }
        System.out.println();
    }

    private static void pathDriver() {
        System.out.println("Pilihan:");
        System.out.println("[1] Graf tak berarah");
        System.out.println("[2] Graf berarah");
        System.out.println("");
        Graph graf = null;
        boolean ulangi = true;
        while (ulangi) {
            System.out.print("Masukkan tipe graf: ");
            int pilihan = SCANNER.nextInt();
            ulangi = false;
            switch (pilihan) {
                case 1:
                    graf = inputUndirectedGraph();
                    break;

                case 2:
                    graf = inputDirectedGraph();
                    break;

                default:
                    ulangi = true;
                    break;
            }
        }
        System.out.println();

        Node startNode = null;
        while (startNode == null) {
            System.out.print("Masukkan nama node awal: ");
            String startNodeName = SCANNER.next();
            startNode = graf.getNode(startNodeName);
        }
        System.out.println();

        Node endNode = null;
        while (endNode == null) {
            System.out.print("Masukkan nama node akhir: ");
            String endNodeName = SCANNER.next();
            endNode = graf.getNode(endNodeName);
        }
        System.out.println();

        System.out.println("Pilihan:");
        System.out.println("[1] Depth First Search");
        System.out.println("[2] Breath First Search");
        System.out.println("");

        List<Node> hasil = null;
        ulangi = true;
        while (ulangi) {
            System.out.print("Masukkan jenis algoritma yang dipakai: ");
            int pilihan = SCANNER.nextInt();
            ulangi = false;
            switch (pilihan) {
                case 1:
                    hasil = PathSearch.searchPathDFS(graf, startNode, endNode);
                    break;

                case 2:
                    hasil = PathSearch.searchPathBFS(graf, startNode, endNode);
                    break;

                default:
                    ulangi = true;
                    break;
            }
        }
        System.out.println();

        if (hasil == null) {
            System.out.println("Tidak ada lintasan dari " + startNode.getNodeName() + " ke " + endNode.getNodeName());
        } else {
            System.out.println("Lintasan dari " + startNode.getNodeName() + " ke " + endNode.getNodeName() + " : ");
            boolean pertama = true;
            for (Node node : hasil) {
                if (!pertama) {
                    System.out.print(" -> ");
                } else {
                    pertama = false;
                }
                System.out.print(node.getNodeName());
            }
            System.out.println();
        }
    }

    private static void connectivityDriver() {
        Graph graf = inputUndirectedGraph();
        if (GraphComponent.isOneComponent(graf)) {
            System.out.println("Graf terhubung");
        } else {
            System.out.println("Bukan graf terhubung");
        }
    }
}
