package ma3052.gui.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import ma3052.core.graph.Edge;
import ma3052.core.graph.Graph;
import ma3052.core.graph.Node;

public class FormatGraphInput {
    // Node name options
    public static enum NodeNameOption {
        CustomNodeName,
        ZeroIndexed,
        OneIndexed
    }

    private static NodeNameOption currentNameOption = NodeNameOption.OneIndexed;

    // Node/Edge count option
    public static enum InputCountOption {
        NodeAndEdgeCount,
        OnlyNodeCount,
        OnlyEdgeCount,
        NoExplicitCount
    }

    private static InputCountOption currentCountOptions = InputCountOption.NodeAndEdgeCount;

    // Graph type
    private static boolean isGraphDirected = false;

    // Node/edge value/weight
    private static boolean inputNodeValue = false; // Input node value after inputting list of name (if chosen)
    private static boolean inputEdgeWeight = false; // Input edge weight after the two pair of node

    // Error prevention
    private static boolean randomNodeOrEdge = false; // Can input node and edge at the same time
    private static boolean newNodeFromEdge = true; // Automatically add new node with the name if does not exist when
                                                   // inputting edges

    private static enum InputState {
        Start,
        NodeCount,
        EdgeCount,
        NodeAndEdgeCount,
        NodeName,
        NodeValue,
        Edge,
        NodeOrEdge,
        End
    }

    public static NodeNameOption getCurrentNameOption() {
        return currentNameOption;
    }

    public static void setCurrentNameOption(NodeNameOption option) {
        currentNameOption = option;
    }

    public static InputCountOption getCurrentCountOption() {
        return currentCountOptions;
    }

    public static void setCurrentCountOption(InputCountOption option) {
        currentCountOptions = option;
    }

    public static boolean isGraphDirected() {
        return isGraphDirected;
    }

    public static void setIsGraphDirected(boolean directed) {
        isGraphDirected = directed;
    }

    public static boolean isInputNodeValue() {
        return inputNodeValue;
    }

    public static void setInputNodeValue(boolean value) {
        inputNodeValue = value;
    }

    public static boolean isInputEdgeWeight() {
        return inputEdgeWeight;
    }

    public static void setInputEdgeWeight(boolean value) {
        inputEdgeWeight = value;
    }

    public static boolean isRandomNodeOrEdge() {
        return randomNodeOrEdge;
    }

    public static void setRandomNodeOrEdge(boolean value) {
        randomNodeOrEdge = value;
    }

    public static boolean isNewNodeFromEdge() {
        return newNodeFromEdge;
    }

    public static void setNewNodeFromEdge(boolean value) {
        newNodeFromEdge = value;
    }

    public static Graph inputGraphFromFile(File input) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        return inputGraphFromReader(reader);
    }

    public static Graph inputGraphFromString(String input) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new StringReader(input));
        return inputGraphFromReader(reader);
    }

    private static Graph inputGraphFromReader(BufferedReader reader) throws IOException, IllegalArgumentException {
        Graph resultGraph = new Graph(isGraphDirected());
        HashMap<Integer, Node> indexToNode = new HashMap<>();

        int nodeCount = -1;
        int edgeCount = -1;
        int currentNodeNameCount = 0;
        int currentNodeValueCount = 0;
        int currentEdgeCount = 0;

        String line;
        int lineNumber = 0;

        InputState currentInputState = InputState.Start;
        switch (currentCountOptions) {
            case NodeAndEdgeCount:
                currentInputState = InputState.NodeAndEdgeCount;
                break;
            case OnlyNodeCount:
                currentInputState = InputState.NodeCount;
                break;
            case OnlyEdgeCount:
                currentInputState = InputState.EdgeCount;
                break;
            case NoExplicitCount:
                currentInputState = InputState.NodeOrEdge;
                break;
        }

        // Parse file
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) {
                continue;
            }

            // Already finished inputting
            if (currentInputState == InputState.End) {
                break;
            }

            String[] tokens = line.split("\\s+");

            switch (currentInputState) {
                case NodeCount:
                    nodeCount = Integer.parseInt(tokens[0]);
                    if (randomNodeOrEdge) {
                        currentInputState = InputState.NodeOrEdge;
                    } else {
                        if (currentNameOption == NodeNameOption.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else {
                            for (int i = 0; i < nodeCount; i++) {
                                Node newNode = new Node(Integer.toString(i));
                                if (currentNameOption == NodeNameOption.ZeroIndexed) {
                                    newNode = new Node(Integer.toString(i));
                                } else if (currentNameOption == NodeNameOption.OneIndexed) {
                                    newNode = new Node(Integer.toString(i + 1));
                                }
                                resultGraph.addNode(newNode);
                                indexToNode.put(i, newNode);
                            }
                            if (inputNodeValue) {
                                currentInputState = InputState.NodeValue;
                            } else {
                                currentInputState = InputState.Edge;
                            }
                        }
                    }
                    break;

                case EdgeCount:
                    edgeCount = Integer.parseInt(tokens[0]);
                    if (randomNodeOrEdge) {
                        currentInputState = InputState.NodeOrEdge;
                    } else {
                        if (currentNameOption == NodeNameOption.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else if (inputNodeValue) {
                            currentInputState = InputState.NodeValue;
                        } else {
                            currentInputState = InputState.Edge;
                        }
                    }
                    break;

                case NodeAndEdgeCount:
                    if (tokens.length != 2) {
                        throw new IllegalArgumentException(
                                "[Line " + lineNumber + "] Expected node count and edge count.");
                    }
                    nodeCount = Integer.parseInt(tokens[0]);
                    edgeCount = Integer.parseInt(tokens[1]);
                    if (randomNodeOrEdge) {
                        currentInputState = InputState.NodeOrEdge;
                    } else {
                        if (currentNameOption == NodeNameOption.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else {
                            for (int i = 0; i < nodeCount; i++) {
                                Node newNode = new Node(Integer.toString(i));
                                if (currentNameOption == NodeNameOption.ZeroIndexed) {
                                    newNode = new Node(Integer.toString(i));
                                } else if (currentNameOption == NodeNameOption.OneIndexed) {
                                    newNode = new Node(Integer.toString(i + 1));
                                }
                                resultGraph.addNode(newNode);
                                indexToNode.put(i, newNode);
                            }
                            if (inputNodeValue) {
                                currentInputState = InputState.NodeValue;
                            } else {
                                currentInputState = InputState.Edge;
                            }
                        }
                    }
                    break;

                case NodeName:
                    for (int i = 0; i < tokens.length && currentNodeNameCount < nodeCount; i++) {
                        Node newNode = new Node(tokens[i]);
                        indexToNode.put(currentNodeNameCount, newNode);
                        resultGraph.addNode(newNode);
                        currentNodeNameCount++;
                    }
                    if (currentNodeNameCount == nodeCount) {
                        if (inputNodeValue) {
                            currentInputState = InputState.NodeValue;
                        } else {
                            currentInputState = InputState.Edge;
                        }
                    }
                    break;

                case NodeValue:
                    for (int i = 0; i < tokens.length && currentNodeValueCount < nodeCount; i++) {
                        Node node = indexToNode.get(currentNodeValueCount);
                        node.setValue(Integer.parseInt(tokens[i]));
                        currentNodeValueCount++;
                    }
                    if (currentNodeValueCount == nodeCount) {
                        if (inputNodeValue) {
                            currentInputState = InputState.NodeValue;
                        } else {
                            currentInputState = InputState.Edge;
                        }
                    }
                    break;

                case Edge:
                    if (currentEdgeCount == edgeCount) {
                        currentInputState = InputState.End;
                        break;
                    }
                    if (tokens.length == 2) {
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        if (newNodeFromEdge) {
                            if (source == null) {
                                source = new Node(tokens[0]);
                            }
                            if (destination == null) {
                                destination = new Node(tokens[1]);
                            }
                        } else {
                            if (source == null || destination == null) {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Edge references non-existent node: " +
                                                (source == null ? tokens[0] : tokens[1]));
                            }
                        }
                        if (isGraphDirected) {
                            resultGraph.addDirectedEdge(source, destination);
                        } else {
                            resultGraph.addUndirectedEdge(source, destination);
                        }
                        currentEdgeCount++;
                    } else if (tokens.length == 3) {
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        double weight = Double.parseDouble(tokens[2]);
                        if (!newNodeFromEdge) {
                            if (source == null || destination == null) {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + "Edge references non-existent node: " +
                                                (source == null ? tokens[0] : tokens[1]));
                            }
                        }
                        else {
                            if (source == null) {
                                source = new Node(tokens[0]);
                            }
                            if (destination == null) {
                                destination = new Node(tokens[1]);
                            }
                        }
                        if (isGraphDirected) {
                            resultGraph.addDirectedEdge(source, destination, weight);
                        } else {
                            resultGraph.addUndirectedEdge(source, destination, weight);
                        }
                        currentEdgeCount++;
                    } else {
                        throw new IllegalArgumentException("Line " + lineNumber +
                                ": Expected 2 or 3 space-separated values, got " + tokens.length);
                    }
                    break;

                case NodeOrEdge:
                    if (currentNodeNameCount == nodeCount && currentEdgeCount == edgeCount) {
                        currentInputState = InputState.End;
                        break;
                    }
                    if (tokens.length == 1) {
                        // Node
                        Node node = resultGraph.getNode(tokens[0]);
                        if (node == null) {
                            node = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount || nodeCount == -1) {
                                resultGraph.addNode(node);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Number of node is larger than node count");
                            }
                        }
                    } else if (tokens.length == 2) {
                        // Edge
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        if (source == null) {
                            source = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount || nodeCount == -1) {
                                resultGraph.addNode(source);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Number of node is larger than node count");
                            }
                        }
                        if (destination == null) {
                            destination = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount || nodeCount == -1) {
                                resultGraph.addNode(destination);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Number of node is larger than node count");
                            }
                        }
                        if (currentEdgeCount < edgeCount || edgeCount == -1) {
                            if (isGraphDirected) {
                                resultGraph.addDirectedEdge(source, destination);
                            } else {
                                resultGraph.addUndirectedEdge(source, destination);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "[Line " + lineNumber + "] Number of edge is larger than edge count");
                        }
                    } else if (tokens.length == 3) {
                        // Weighted edge
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        double weight = Double.parseDouble(tokens[2]);
                        if (source == null) {
                            source = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount || nodeCount == -1) {
                                resultGraph.addNode(source);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Number of node is larger than node count");
                            }
                        }
                        if (destination == null) {
                            destination = new Node(tokens[1]);
                            if (currentNodeNameCount < nodeCount || nodeCount == -1) {
                                resultGraph.addNode(destination);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "[Line " + lineNumber + "] Number of node is larger than node count");
                            }
                        }
                        if (currentEdgeCount < edgeCount || edgeCount == -1) {
                            if (isGraphDirected) {
                                resultGraph.addDirectedEdge(source, destination, weight);
                            } else {
                                resultGraph.addUndirectedEdge(source, destination, weight);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "[Line " + lineNumber + "] Number of edge is larger than edge count");
                        }
                    } else {
                        throw new IllegalArgumentException(
                                "[Line " + lineNumber + "] Expected 1, 2, or 3 space-separated values, got "
                                        + tokens.length);
                    }
                    break;

                case End:
                    break;

                default:
                    break;
            }
        }
        reader.close();

        return resultGraph;
    }

    public static String getFormatString() {
        String resultString = new String();

        // Node and edge count
        switch (currentCountOptions) {
            case NodeAndEdgeCount:
                resultString = resultString.concat("<node-count> <edge-count>\n");
                break;
            case OnlyNodeCount:
                resultString = resultString.concat("<node-count>\n");
                break;
            case OnlyEdgeCount:
                resultString = resultString.concat("<edge-count>\n");
                break;
            case NoExplicitCount:
                break;
            default:
                break;
        }

        // Node name
        if (currentNameOption == NodeNameOption.CustomNodeName) {
            resultString = resultString.concat("<node-name-1> <node-name-2> ... <node-name-n>\n");
        }

        // Node value
        if (inputNodeValue) {
            resultString = resultString.concat("<node-value-1> <node-value-2> ... <node-value-n>\n");
        }

        // Edges
        if (inputEdgeWeight) {
            resultString = resultString.concat("<edge-source-1> <edge-destination-1> <edge-weight-1>\n");
            resultString = resultString.concat("<edge-source-2> <edge-destination-2> <edge-weight-2>\n");
            resultString = resultString.concat("...\n");
            resultString = resultString.concat("<edge-source-m> <edge-destination-m> <edge-weight-m>\n");
        } else {
            resultString = resultString.concat("<edge-source-1> <edge-destination-1>\n");
            resultString = resultString.concat("<edge-source-2> <edge-destination-2>\n");
            resultString = resultString.concat("...\n");
            resultString = resultString.concat("<edge-source-m> <edge-destination-m>\n");
        }

        return resultString;
    }

    public static String graphToInputString(Graph graph) {
        String resultString = new String();

        // Node and edge count
        switch (currentCountOptions) {
            case NodeAndEdgeCount:
                resultString = resultString.concat(graph.getNodeList().size() + " "
                        + graph.getEdgeList().size() + "\n");
                break;
            case OnlyNodeCount:
                resultString = resultString.concat(graph.getNodeList().size() + "\n");
                break;
            case OnlyEdgeCount:
                resultString = resultString.concat(graph.getEdgeList().size() + "\n");
                break;
            case NoExplicitCount:
                break;
            default:
                break;
        }

        // Node name
        if (currentNameOption == NodeNameOption.CustomNodeName) {
            for (Node node : graph.getNodeList()) {
                resultString = resultString.concat(node.getNodeName() + "\n");
            }
        }

        // Node value
        if (inputNodeValue) {
            for (Node node : graph.getNodeList()) {
                resultString = resultString.concat(node.getNodeName() + " ");
            }
            resultString = resultString.concat("\n");
        }

        // Edges
        for (Edge edge : graph.getEdgeList()) {
            if (inputEdgeWeight) {
                resultString = resultString.concat(edge.getSource().getNodeName() + " "
                        + edge.getDestination().getNodeName() + " "
                        + edge.getWeight() + "\n");
            } else {
                resultString = resultString.concat(edge.getSource().getNodeName() + " "
                        + edge.getDestination().getNodeName() + "\n");
            }
        }

        return resultString;
    }
}
