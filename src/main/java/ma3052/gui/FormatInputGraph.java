package ma3052.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import ma3052.graph.Graph;
import ma3052.graph.Node;

// TODO: TEST THIS CLASS AND IMPLEMENT IT TO GUI
public class FormatInputGraph {
    // Configs boolean
    private static boolean inputNodeCount = true; // Input node count on first line
    private static boolean inputEdgeCount = true; // Input edge count on first line

    // Node name options
    public static enum NodeNameOptions {
        CustomNodeName,
        ZeroIndexed,
        OneIndexed
    }

    private static NodeNameOptions currentNameOption = NodeNameOptions.OneIndexed;

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

    public static Graph inputGraphFromFile(File file) throws Exception {
        Graph resultGraph = new Graph();
        HashMap<Integer, Node> indexToNode = new HashMap<>();

        int nodeCount = -1;
        int edgeCount = -1;
        int currentNodeNameCount = 0;
        int currentNodeValueCount = 0;
        int currentEdgeCount = 0;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int lineNumber = 0;

        InputState currentInputState = InputState.Start;
        if (inputNodeCount && inputEdgeCount) {
            currentInputState = InputState.NodeAndEdgeCount;
        } else if (inputNodeCount) {
            currentInputState = InputState.NodeCount;
        } else if (inputEdgeCount) {
            currentInputState = InputState.EdgeCount;
        } else {
            currentInputState = InputState.NodeOrEdge;
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
                        if (currentNameOption == NodeNameOptions.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else {
                            for (int i = 0; i < nodeCount; i++) {
                                Node newNode = new Node(Integer.toString(i));
                                if (currentNameOption == NodeNameOptions.ZeroIndexed) {
                                    newNode = new Node(Integer.toString(i));
                                } else if (currentNameOption == NodeNameOptions.OneIndexed) {
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
                        if (currentNameOption == NodeNameOptions.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else if (inputNodeValue) {
                            currentInputState = InputState.NodeValue;
                        } else {
                            currentInputState = InputState.Edge;
                        }
                    }
                    break;

                case NodeAndEdgeCount:
                    nodeCount = Integer.parseInt(tokens[0]);
                    edgeCount = Integer.parseInt(tokens[1]);
                    if (randomNodeOrEdge) {
                        currentInputState = InputState.NodeOrEdge;
                    } else {
                        if (currentNameOption == NodeNameOptions.CustomNodeName) {
                            currentInputState = InputState.NodeName;
                        } else if (inputNodeValue) {
                            currentInputState = InputState.NodeValue;
                        } else {
                            currentInputState = InputState.Edge;
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
                                        "Line " + lineNumber + "Edge references non-existent node: " +
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
                        if (source == null || destination == null) {
                            throw new IllegalArgumentException(
                                    "Line " + lineNumber + "Edge references non-existent node: " +
                                            (source == null ? tokens[0] : tokens[1]));
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
                            if (currentNodeNameCount < nodeCount) {
                                resultGraph.addNode(node);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + ": Number of node is larger than node count");
                            }
                        }
                    } else if (tokens.length == 2) {
                        // Edge
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        if (source == null) {
                            source = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount) {
                                resultGraph.addNode(source);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + ": Number of node is larger than node count");
                            }
                        }
                        if (destination == null) {
                            destination = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount) {
                                resultGraph.addNode(destination);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + ": Number of node is larger than node count");
                            }
                        }
                        if (currentEdgeCount < edgeCount) {
                            if (isGraphDirected) {
                                resultGraph.addDirectedEdge(source, destination);
                            } else {
                                resultGraph.addUndirectedEdge(source, destination);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Line " + lineNumber + ": Number of edge is larger than edge count");
                        }
                    } else if (tokens.length == 3) {
                        // Weighted edge
                        Node source = resultGraph.getNode(tokens[0]);
                        Node destination = resultGraph.getNode(tokens[1]);
                        double weight = Double.parseDouble(tokens[2]);
                        if (source == null) {
                            source = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount) {
                                resultGraph.addNode(source);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + ": Number of node is larger than node count");
                            }
                        }
                        if (destination == null) {
                            destination = new Node(tokens[0]);
                            if (currentNodeNameCount < nodeCount) {
                                resultGraph.addNode(destination);
                                currentNodeNameCount++;
                            } else {
                                throw new IllegalArgumentException(
                                        "Line " + lineNumber + ": Number of node is larger than node count");
                            }
                        }
                        if (currentEdgeCount < edgeCount) {
                            if (isGraphDirected) {
                                resultGraph.addDirectedEdge(source, destination, weight);
                            } else {
                                resultGraph.addUndirectedEdge(source, destination, weight);
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Line " + lineNumber + ": Number of edge is larger than edge count");
                        }
                    } else {
                        throw new IllegalArgumentException("Line " + lineNumber +
                                ": Expected 1, 2, or 3 space-separated values, got "
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
}
