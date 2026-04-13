package ma3052.core.driver;

import java.util.List;
import java.util.Scanner;

import ma3052.core.algorithm.TravellingSalesman;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class TravellingSalesmanDriver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int nodeCount, edgesCount;
        nodeCount = scanner.nextInt();
        edgesCount = scanner.nextInt();

        Graph graph = new Graph();
        for (int i = 0; i < edgesCount; i++) {
            graph.addUndirectedEdge(scanner.next(), scanner.next(), scanner.nextDouble());
        }

        List<Node> result = TravellingSalesman.solve(graph);
        double cost = 0;
        for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i).getNodeName());
            cost += result.get(i).getEdge(result.get((i + 1) % result.size())).getWeight();
        }
        System.out.println("Cost: " + cost);

        scanner.close();
    }
}
