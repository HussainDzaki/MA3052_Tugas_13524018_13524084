package ma3052.gui.controller;

import java.io.InputStream;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import ma3052.App;
import ma3052.core.graph.GraphFactory;
import ma3052.core.graph.GraphFactory.BipartiteNameOption;
import ma3052.core.graph.GraphFactory.NodeNameOption;
import ma3052.gui.graph.GraphGUI;

public class GraphGeneratorController {
    private GraphVisualizationController mainController;

    private final String[] graphTypes = {
            "Complete Graph",
            "Complete Binary Graph",
            "Complete Bipartite Graph",
            "Path Graph",
            "Cycle Graph",
            "Star Graph",
            "Wheel Graph",
            "Prism Graph",
            "Hypercube Graph",
            "Ladder Graph",
            "Circular Ladder Graph",
            "Mobius Ladder Graph",
            "Petersen Graph",
            "Generalized Petersen Graph",
            "Circulant Graph",
    };
    private String currentGraphType = graphTypes[0];

    @FXML
    ScrollPane scrollPane;
    @FXML
    private GridPane graphGrid;

    @FXML
    private Label graphLabel;
    @FXML
    private VBox nodeNamingVBox;
    @FXML
    private ComboBox<String> nodeNamingComboBox;
    @FXML
    private VBox graphNamingVBox;
    @FXML
    private ComboBox<String> graphNamingComboBox;
    @FXML
    private VBox firstInputVBox;
    @FXML
    private Label firstInputLabel;
    @FXML
    private TextField firstInputTextField;
    @FXML
    private VBox secondInputVBox;
    @FXML
    private Label secondInputLabel;
    @FXML
    private TextField secondInputTextField;
    @FXML
    private Button generateButton;

    // Graph
    private GraphGUI graphGUI;

    public void setMainController(GraphVisualizationController mainController) {
        this.mainController = mainController;
        graphGUI = mainController.getGraphGUI();
    }

    /*
     * TUJUAN HASIL:
     * <VBox alignment="CENTER" spacing="10" GridPane.columnIndex="0"
     * GridPane.rowIndex="0" styleClass="button-secondary-2,rounded">
     * <padding>
     * <Insets bottom="10" left="15" right="15" top="10" />
     * </padding>
     * <HBox styleClass="border-solid,border-thick" maxWidth="150">
     * <ImageView fitWidth="150" preserveRatio="true">
     * <Image url="@../image/graph-generator/Complete Graph.png"/>
     * </ImageView>
     * </HBox>
     * <Label text="Complete Graph" styleClass="h2,lexend-bold"/>
     * </VBox>
     */
    @FXML
    void initialize() {
        for (int i = 0; i < graphTypes.length; i++) {
            int row = i / 3;
            int col = i % 3;
            VBox vBox = addCell(row, col);

            InputStream inputStream = App.class
                    .getResourceAsStream("image/graph-generator/" + graphTypes[i] + ".png");

            if (inputStream != null) {
                Image image = new Image(inputStream);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(150);
                imageView.setPreserveRatio(true);
                HBox imageHBox = new HBox(imageView);
                imageHBox.getStyleClass().addAll("border-solid", "border-thick");
                imageHBox.setMaxWidth(150);
                vBox.getChildren().add(imageHBox);
            }

            Label label = new Label(graphTypes[i]);
            label.setTextAlignment(TextAlignment.CENTER);
            label.getStyleClass().addAll("h2", "lexend-bold");

            vBox.getChildren().add(label);

            final String labelText = graphTypes[i];
            vBox.setOnMouseClicked((e) -> {
                graphLabel.setText("Selected: " + labelText);
                currentGraphType = labelText;
                scrollPane.setVvalue(0.0);
                switch (currentGraphType) {
                    case "Complete Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Complete Binary Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Depth");
                        firstInputTextField.setPromptText("D");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Complete Bipartite Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Graph 1 Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(true);
                        secondInputVBox.setVisible(true);
                        secondInputLabel.setText("Graph 2 Node Count");
                        secondInputTextField.setPromptText("M");
                        break;
                    case "Path Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Cycle Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Star Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Outer Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Wheel Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Outer Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(true);
                        secondInputVBox.setVisible(true);
                        secondInputLabel.setText("Layer Count (>= 1)");
                        secondInputTextField.setPromptText("K");
                        break;
                    case "Prism Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("N-gon");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(true);
                        secondInputVBox.setVisible(true);
                        secondInputLabel.setText("Layer Count (>= 2)");
                        secondInputTextField.setPromptText("K");
                        break;
                    case "Hypercube Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Dimension");
                        firstInputTextField.setPromptText("D");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Ladder Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Step Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Circular Ladder Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Step Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Mobius Ladder Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Step Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Petersen Graph":
                        firstInputVBox.setManaged(false);
                        firstInputVBox.setVisible(false);

                        secondInputVBox.setManaged(false);
                        secondInputVBox.setVisible(false);
                        break;
                    case "Generalized Petersen Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Outer Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(true);
                        secondInputVBox.setVisible(true);
                        secondInputLabel.setText("Edge Jump");
                        secondInputTextField.setPromptText("K");
                        break;
                    case "Circulant Graph":
                        firstInputVBox.setManaged(true);
                        firstInputVBox.setVisible(true);
                        firstInputLabel.setText("Node Count");
                        firstInputTextField.setPromptText("N");

                        secondInputVBox.setManaged(true);
                        secondInputVBox.setVisible(true);
                        secondInputLabel.setText("Edge Jumps");
                        secondInputTextField.setPromptText("S1 S2 S3 ... Sk");
                        break;
                }
                nodeNamingVBox.setManaged(!currentGraphType.equals("Hypercube Graph"));
                nodeNamingVBox.setVisible(!currentGraphType.equals("Hypercube Graph"));
                graphNamingVBox.setManaged(currentGraphType.equals("Complete Bipartite Graph"));
                graphNamingVBox.setVisible(currentGraphType.equals("Complete Bipartite Graph"));
            });
        }
        nodeNamingComboBox.getItems().addAll("One Indexed", "Zero Indexed", "Alphabetic");
        nodeNamingComboBox.setValue("One Indexed");
        graphNamingComboBox.getItems().addAll("UV", "XY", "AB", "NONE");
        graphNamingComboBox.setValue("UV");
    }

    private VBox addCell(int row, int col) {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        vBox.setSpacing(10);
        vBox.getStyleClass().addAll("button-secondary-2", "rounded", "cursor-pointer");
        vBox.setPadding(new Insets(10, 15, 15, 10));
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setPrefWidth(200);
        graphGrid.add(vBox, col, row);
        return vBox;
    }

    private NodeNameOption getNoneNameOption() {
        switch (nodeNamingComboBox.getValue()) {
            case "Zero Indexed":
                return NodeNameOption.ZeroIndexed;

            case "One Indexed":
                return NodeNameOption.OneIndexed;

            case "Alphabetic":
                return NodeNameOption.Alphabetic;

            default:
                return NodeNameOption.ZeroIndexed;
        }
    }

    private BipartiteNameOption getGraphNameOption() {
        switch (graphNamingComboBox.getValue()) {
            case "UV":
                return BipartiteNameOption.UV;

            case "XY":
                return BipartiteNameOption.XY;

            case "AB":
                return BipartiteNameOption.AB;

            default:
                return BipartiteNameOption.NONE;
        }
    }

    @FXML
    private void handleGenerateGraph() {
        try {
            switch (currentGraphType) {
                case "Complete Graph":
                    graphGUI.setGraph(GraphFactory.createCompleteGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Complete Binary Graph":
                    graphGUI.setGraph(GraphFactory.createCompleteBinaryGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Complete Bipartite Graph":
                    graphGUI.setGraph(GraphFactory.createCompleteBipartiteGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            Integer.parseInt(secondInputTextField.getText()),
                            getNoneNameOption(), getGraphNameOption()));
                    break;
                case "Path Graph":
                    graphGUI.setGraph(GraphFactory.createPathGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Cycle Graph":
                    graphGUI.setGraph(GraphFactory.createCycleGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Star Graph":
                    graphGUI.setGraph(GraphFactory.createStarGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Wheel Graph":
                    graphGUI.setGraph(GraphFactory.createWheelGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            Integer.parseInt(secondInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Prism Graph":
                    graphGUI.setGraph(GraphFactory.createPrismGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            Integer.parseInt(secondInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Hypercube Graph":
                    graphGUI.setGraph(GraphFactory.createHypercubeGraph(
                            Integer.parseInt(firstInputTextField.getText())));
                    break;
                case "Ladder Graph":
                    graphGUI.setGraph(GraphFactory.createLadderGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Circular Ladder Graph":
                    graphGUI.setGraph(GraphFactory.createCircularLadderGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Mobius Ladder Graph":
                    graphGUI.setGraph(GraphFactory.createMobiusLadderGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Petersen Graph":
                    graphGUI.setGraph(GraphFactory.createPetersenGraph());
                    break;
                case "Generalized Petersen Graph":
                    graphGUI.setGraph(GraphFactory.createGeneralizedPetersenGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            Integer.parseInt(secondInputTextField.getText()),
                            getNoneNameOption()));
                    break;
                case "Circulant Graph":
                    String[] tokens = secondInputTextField.getText().split("[ \t+]");
                    int[] s = new int[tokens.length];
                    for (int i = 0; i < s.length; i++) {
                        s[i] = Integer.parseInt(tokens[i]);
                    }
                    graphGUI.setGraph(GraphFactory.createCirculantGraph(
                            Integer.parseInt(firstInputTextField.getText()),
                            getNoneNameOption(), s));
                    break;
            }
            mainController.getGraphInputMenuController().updateListFromGraph();
            ((Stage) (graphGrid.getScene().getWindow())).close();
        } catch (Exception e) {
            mainController.showError(e.getMessage());
        }
    }
}
