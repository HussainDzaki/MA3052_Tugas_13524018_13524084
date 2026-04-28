package ma3052.gui.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javafx.geometry.Point2D;
import ma3052.core.graph.Node;
import ma3052.core.graph.PointGraph;

public class FormatPointGraphInput {
    private static enum InputState {
        Node,
        Other
    }

    static private boolean isTspFormat = false;

    public static PointGraph inputGraphFromFile(File input) throws IOException, IllegalArgumentException {
        isTspFormat = input.getName().contains(".tsp");
        BufferedReader reader = new BufferedReader(new FileReader(input));
        return inputGraphFromReader(reader);
    }

    public static PointGraph inputGraphFromString(String input) throws IOException, IllegalArgumentException {
        isTspFormat = false;
        BufferedReader reader = new BufferedReader(new StringReader(input));
        return inputGraphFromReader(reader);
    }

    private static PointGraph inputGraphFromReader(BufferedReader reader) throws IOException, IllegalArgumentException {
        PointGraph graph = new PointGraph();
        InputState state = InputState.Node;
        if (isTspFormat) {
            state = InputState.Other;
        }
        String line;
        while ((line = reader.readLine()) != null) {
            switch (state) {
                case Node:
                    if (line.equals("EOF")) {
                        state = InputState.Other;
                    } else {
                        String[] tokens = line.split("\\s+");
                        if (!graph.hasNode(tokens[0])) {
                            graph.addNode(new Node(tokens[0]),
                                    new Point2D(Double.parseDouble(tokens[2]), -Double.parseDouble(tokens[1])));
                        }
                    }
                    break;
                case Other:
                    if (line.equals("NODE_COORD_SECTION")) {
                        state = InputState.Node;
                    }
                    break;
                default:
                    break;
            }
        }
        return graph;
    }
}
