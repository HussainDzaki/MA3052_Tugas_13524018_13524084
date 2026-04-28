package ma3052.gui.graph;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ma3052.core.graph.Edge;

public class EdgeGUI {
    // Edge
    private Edge edge;
    private NodeGUI sourceGUI;
    private NodeGUI destinationGUI;

    // Visual
    private double lineWidth;
    private Color lineColor;
    private Color textColor;
    private double arrowHeadSize;
    private boolean drawWeight;

    // Physics config
    private static final double SPRING_CONSTANT = 200;
    private static final double MIN_SPRING_LENGTH = 120;
    private static final double MAX_SPRING_LENGTH = 120;

    public EdgeGUI(Edge edge, NodeGUI sourceGUI, NodeGUI destinationGUI) {
        this.edge = edge;
        this.sourceGUI = sourceGUI;
        this.destinationGUI = destinationGUI;
        this.lineWidth = 2;
        this.lineColor = Color.BLACK;
        this.textColor = Color.BLACK;
        this.arrowHeadSize = 10;
    }

    public Edge getEdge() {
        return edge;
    }

    public NodeGUI getSourceGUI() {
        return sourceGUI;
    }

    public NodeGUI getDestinationGUI() {
        return destinationGUI;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWeight) {
        this.lineWidth = lineWeight;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public double getArrowHeadSize() {
        return arrowHeadSize;
    }

    public void setArrowHeadSize(double arrowHeadSize) {
        this.arrowHeadSize = arrowHeadSize;
    }

    public boolean isDrawWeight() {
        return drawWeight;
    }

    public void setDrawWeight(boolean drawWeight) {
        this.drawWeight = drawWeight;
    }

    public void draw(GraphicsContext context, boolean isDirected) {
        Point2D angleVector = sourceGUI.getPosition().subtract(destinationGUI.getPosition());
        double angleRad = Math.atan2(angleVector.getY(), angleVector.getX());

        // Draw line between two nodes
        context.setStroke(lineColor);
        context.setLineWidth(lineWidth);
        context.strokeLine(
                sourceGUI.getPosition().getX(), sourceGUI.getPosition().getY(),
                destinationGUI.getPosition().getX(), destinationGUI.getPosition().getY());

        if (isDirected) {
            // Draw arrow head
            Point2D offset1 = new Point2D(Math.cos(angleRad), Math.sin(angleRad))
                    .multiply(destinationGUI.getRadius());
            Point2D point1 = destinationGUI.getPosition().add(offset1);

            Point2D offset2 = new Point2D(Math.cos(angleRad + Math.toRadians(30)),
                    Math.sin(angleRad + Math.toRadians(30))).multiply(arrowHeadSize);
            Point2D point2 = point1.add(offset2);

            Point2D offset3 = new Point2D(Math.cos(angleRad - Math.toRadians(30)),
                    Math.sin(angleRad - Math.toRadians(30))).multiply(arrowHeadSize);
            Point2D point3 = point1.add(offset3);

            context.setFill(lineColor);
            context.fillPolygon(
                    new double[] { point1.getX(), point2.getX(), point3.getX() },
                    new double[] { point1.getY(), point2.getY(), point3.getY() },
                    3);
        }

        if (edge == null)
            return;

        if (drawWeight) {
            // Draw weight text in the middle
            Point2D middlePosition = sourceGUI.getPosition().add(destinationGUI.getPosition()).multiply(0.5);
            Point2D offset = new Point2D(Math.sin(angleRad), -Math.cos(angleRad)).multiply(15 + lineWidth);

            String weightString = Double.toString(edge.getWeight());
            weightString = weightString.replace("Infinity", "∞");
            double textX = middlePosition.getX() + offset.getX();
            double textY = middlePosition.getY() + offset.getY();

            context.setFill(textColor);
            context.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            context.fillText(weightString, textX, textY);

            // TODO: bikin tulisan ga overlap dengan edge kalo mau
        }
    }

    public void update() {
        // Add spring force to both node
        // Hooke's Law: F = -kx
        Point2D offset = destinationGUI.getPosition().subtract(sourceGUI.getPosition());
        Point2D springForce = new Point2D(0, 0);
        if (offset.magnitude() < MIN_SPRING_LENGTH) {
            springForce = offset.normalize().multiply(SPRING_CONSTANT * (offset.magnitude() - MIN_SPRING_LENGTH));
        } else if (offset.magnitude() > MAX_SPRING_LENGTH) {
            springForce = offset.normalize().multiply(SPRING_CONSTANT * (offset.magnitude() - MAX_SPRING_LENGTH));
        }
        sourceGUI.addForce(springForce);
        destinationGUI.addForce(springForce.multiply(-1));
    }

}
