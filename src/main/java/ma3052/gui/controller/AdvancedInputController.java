package ma3052.gui.controller;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AdvancedInputController {
    private GraphVisualGUIController mainController;

    @FXML
    private ComboBox<String> nodeLabel;

    @FXML
    private ComboBox<String> inputCount;

    @FXML
    private CheckBox directed;

    @FXML
    private CheckBox nodeValue;

    @FXML
    private CheckBox edgeWeight;

    @FXML
    private CheckBox addNodeFromEdge;

    @FXML
    private CheckBox randomNodeEgde;

    @FXML
    private Button addFromFileButton;

    @FXML
    private Button getCurrentGraphButton;

    @FXML
    private Button applyInputButton;

    @FXML
    private TextArea formatTextArea;

    @FXML
    private TextArea inputTextArea;

    @FXML
    public void initialize() {
        initOptions();
        initButtons();
        initTextAreas();
    }

    private void initOptions() {
        nodeLabel.getItems().addAll(
                "Zero Indexed",
                "One Indexed",
                "Custom Label");
        nodeLabel.setValue("One Indexed");

        switch (FormatGraphInput.getCurrentNameOption()) {
            case ZeroIndexed:
                nodeLabel.setValue("Zero Indexed");
                break;
            case OneIndexed:
                nodeLabel.setValue("One Indexed");
                break;
            case CustomNodeName:
                nodeLabel.setValue("Custom Label");
                break;
        }
        nodeLabel.setOnAction(e -> {
            String str = nodeLabel.getValue();
            if (str.equals("Zero Indexed")) {
                FormatGraphInput.setCurrentNameOption(FormatGraphInput.NodeNameOption.ZeroIndexed);
            } else if (str.equals("One Indexed")) {
                FormatGraphInput.setCurrentNameOption(FormatGraphInput.NodeNameOption.OneIndexed);
            } else if (str.equals("Custom Label")) {
                FormatGraphInput.setCurrentNameOption(FormatGraphInput.NodeNameOption.CustomNodeName);
            }
            updateFormat();
        });

        inputCount.getItems().addAll(
                "Node and Edge Count",
                "Only Node Count",
                "Only Edge Count",
                "No Explicit Count");

        switch (FormatGraphInput.getCurrentCountOption()) {
            case NodeAndEdgeCount:
                inputCount.setValue("Node and Edge Count");
                break;
            case OnlyNodeCount:
                inputCount.setValue("Only Node Count");
                break;
            case OnlyEdgeCount:
                inputCount.setValue("Only Edge Count");
                break;
            case NoExplicitCount:
                inputCount.setValue("No Explicit Count");
                break;
        }

        inputCount.setOnAction(e -> {
            String str = inputCount.getValue();
            if (str.equals("Node and Edge Count")) {
                FormatGraphInput.setCurrentCountOption(FormatGraphInput.InputCountOption.NodeAndEdgeCount);
                addNodeFromEdge.setDisable(false);
            } else if (str.equals("Only Node Count")) {
                FormatGraphInput.setCurrentCountOption(FormatGraphInput.InputCountOption.OnlyNodeCount);
                addNodeFromEdge.setDisable(false);
            } else if (str.equals("Only Edge Count")) {
                FormatGraphInput.setCurrentCountOption(FormatGraphInput.InputCountOption.OnlyEdgeCount);
                addNodeFromEdge.setSelected(true);
                addNodeFromEdge.setDisable(true);
            } else if (str.equals("No Explicit Count")) {
                addNodeFromEdge.setSelected(true);
                addNodeFromEdge.setDisable(true);
                FormatGraphInput.setCurrentCountOption(FormatGraphInput.InputCountOption.NoExplicitCount);
            }
            updateFormat();
        });

        directed.setSelected(FormatGraphInput.isGraphDirected());
        directed.setOnAction(e -> {
            FormatGraphInput.setIsGraphDirected(directed.isSelected());
            updateFormat();
        });

        nodeValue.setSelected(FormatGraphInput.isInputNodeValue());
        nodeValue.setOnAction(e -> {
            FormatGraphInput.setInputNodeValue(nodeValue.isSelected());
            updateFormat();
        });

        edgeWeight.setSelected(FormatGraphInput.isInputEdgeWeight());
        edgeWeight.setOnAction(e -> {
            FormatGraphInput.setInputEdgeWeight(edgeWeight.isSelected());
            updateFormat();
        });

        addNodeFromEdge.setSelected(FormatGraphInput.isNewNodeFromEdge());
        addNodeFromEdge.setOnAction(e -> {
            FormatGraphInput.setNewNodeFromEdge(addNodeFromEdge.isSelected());
            updateFormat();
        });

        randomNodeEgde.setSelected(FormatGraphInput.isRandomNodeOrEdge());
        randomNodeEgde.setOnAction(e -> {
            FormatGraphInput.setRandomNodeOrEdge(randomNodeEgde.isSelected());
            updateFormat();
        });
    }

    private void initButtons() {
        addFromFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Graph from File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));

            File selectedFile = fileChooser.showOpenDialog(addFromFileButton.getScene().getWindow());
            if (selectedFile != null) {
                Platform.runLater(() -> {
                    try {
                        if (mainController != null) {
                            mainController.setGraph(FormatGraphInput.inputGraphFromFile(selectedFile));
                            mainController.getGraphGUI().setDrawEdgeWeight(edgeWeight.isSelected());
                            mainController.getGraphGUI().updateGraph();
                            ((Stage) (applyInputButton.getScene().getWindow())).close();
                        }
                    } catch (IOException e) {
                        showError("Error reading file: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        showError("Invalid file format: " + e.getMessage());
                    }
                });
            }
        });

        getCurrentGraphButton.setOnAction(event -> {
            try {
                if (mainController != null) {
                    inputTextArea
                            .setText(FormatGraphInput.graphToInputString(mainController.getGraph()));
                }
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });

        applyInputButton.setOnAction(event -> {
            Platform.runLater(() -> {
                try {
                    if (mainController != null) {
                        mainController
                                .setGraph(FormatGraphInput.inputGraphFromString(inputTextArea.getText()));
                        mainController.getGraphGUI().setDrawEdgeWeight(edgeWeight.isSelected());
                        mainController.getGraphGUI().updateGraph();
                        ((Stage) (applyInputButton.getScene().getWindow())).close();
                    }
                } catch (IOException e) {
                    showError("Error reading file: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    showError("Invalid file format: " + e.getMessage());
                }
            });
        });
    }

    private void initTextAreas() {
        formatTextArea.setEditable(false);
        formatTextArea.setText("" +
                "<node-count(n)> <edge-count(m)>\n" +
                "<edge-1> <edge-1>\n" +
                "<edge-2> <edge-2>\n" +
                "<edge-3> <edge-3>\n" +
                "...\n" +
                "<edge-m> <edge-m>");
        inputTextArea.setEditable(true);
        inputTextArea.setText("" +
                "4 5\n" +
                "1 4\n" +
                "4 3\n" +
                "2 3\n" +
                "3 5\n" +
                "2 5");

        Platform.runLater(() -> {
            updateFormat();
            updateInputFromGraph();
        });
    }

    public void setMainController(GraphVisualGUIController mainController) {
        this.mainController = mainController;
    }

    private void updateFormat() {
        formatTextArea.setText(FormatGraphInput.getFormatString());
    }

    private void updateInputFromGraph() {
        if (mainController != null) {
            inputTextArea
                    .setText(FormatGraphInput.graphToInputString(mainController.getGraph()));
        }
    }

    /**
     * Show an error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
