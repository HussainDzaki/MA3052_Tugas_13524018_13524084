package ma3052.driver;

import java.util.Scanner;

import ma3052.graph.GridGraph;
import ma3052.graph.Island;

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

        System.out.print("Total pulau: " + Island.getTotalIsland(g, '#'));

        scanner.close();
    }
}
