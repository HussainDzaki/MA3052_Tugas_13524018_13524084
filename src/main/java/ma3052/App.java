package ma3052;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ma3052.graph.Graph;
import ma3052.graph.Node;

import java.io.IOException;
import java.util.Scanner;

/**
 * JavaFX App
 */
public class App {

    public static void main(String[] args) {
        int nodeCount, edgesCount;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan <Banyak node>, Banyak sisi\n");
        nodeCount = scanner.nextInt();
        edgesCount = scanner.nextInt();

        Graph g = new Graph();
        for (int i = 0; i < nodeCount; i++) {
            System.out.print(String.format("Masukkan nama node ke-%d: ", i + 1));
            Node n = new Node(scanner.next());
            g.addNode(n);
        }

        System.out.print("Memasukan sisi masing-masing node\n");
        for (int i = 0; i < edgesCount; i++) {
            System.out.print(String.format("Masukkan relasi sisi ke-%d: ", i + 1));
            String startNodeName, endNodeName;
            startNodeName = scanner.next();
            endNodeName = scanner.next();
            Node startNode = g.getNode(startNodeName);
            Node endNode = g.getNode(endNodeName);
            g.addUndirectedEdge(startNode, endNode);
        }
        
        scanner.close();
    }

}