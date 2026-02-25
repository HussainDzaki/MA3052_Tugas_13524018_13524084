package ma3052.graph;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;

public class GridGraph {
    public static final char defaultNodeType = '.';
    private Graph graph;
    private ArrayList<ArrayList<Character>> grid;
    private HashMap<Pair<Integer, Integer>, Node> positionToNode;
    private int rowSize;
    private int colSize;

    public GridGraph(int rows, int cols) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException("Illegal row or col size: rows = " + rows + "; cols = " + cols);
        }

        graph = new Graph();
        grid = new ArrayList<>(rows);
        positionToNode = new HashMap<>();
        rowSize = rows;
        colSize = cols;

        for (int i = 0; i < rows; i++) {
            grid.add(new ArrayList<>(cols));
            for (int j = 0; j < cols; j++) {
                Node node = new Node(Integer.toString(i * rows + j));
                graph.addNode(node);
                grid.get(i).set(j, defaultNodeType);
                positionToNode.put(new Pair<Integer, Integer>(i, j), node);
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i < rows - 1) {
                    graph.addUndirectedEdge(getNode(i, j), getNode(i + 1, j));
                }
                if (j < cols - 1) {
                    graph.addUndirectedEdge(getNode(i, j), getNode(i, j + 1));
                }
            }
        }
    }

    public int getRowSize() {
        return rowSize;
    }

    public int getColSize() {
        return colSize;
    }

    public Node getNode(int row, int col) {
        if (row < 0 || row >= rowSize || col < 0 || col >= colSize) {
            throw new IndexOutOfBoundsException();
        }

        return positionToNode.get(new Pair<Integer, Integer>(row, col));
    }

    public Character getNodeType(int row, int col) {
        if (row < 0 || row >= rowSize || col < 0 || col >= colSize) {
            throw new IndexOutOfBoundsException();
        }

        return grid.get(row).get(col);
    }

    public void setNodeType(int row, int col, char type) {
        if (row < 0 || row >= rowSize || col < 0 || col >= colSize) {
            throw new IndexOutOfBoundsException();
        }

        grid.get(row).set(col, type);
    }
}
