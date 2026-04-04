package ma3052.core.driver;

import java.util.Scanner;

import ma3052.core.algorithm.IslandCounter;
import ma3052.core.graph.GridGraph;

public class IslandDriver {
    public static void main(String[] args) {
        int rowSize, colSize;
        Scanner scanner = new Scanner(System.in);
        rowSize = scanner.nextInt();
        colSize = scanner.nextInt();
        scanner.nextLine();

        GridGraph g = new GridGraph(rowSize, colSize);
        for (int i = 0; i < rowSize; i++) {
            String currentRow = scanner.nextLine();
            for (int j = 0; j < colSize; j++) {
                g.setNodeType(i, j, currentRow.charAt(j));
            }
        }

        System.out.print("Total pulau: " + IslandCounter.getTotalIsland(g, '#'));

        scanner.close();
    }
}
