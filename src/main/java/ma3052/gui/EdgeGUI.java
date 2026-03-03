package ma3052.gui;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ma3052.graph.Edge;

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

    // Physics config
    private static final double SPRING_CONSTANT = 400;
    private static final double SPRING_LENGTH = 100;

    public EdgeGUI(Edge edge, NodeGUI sourceGUI, NodeGUI destinationGUI) {
        this.edge = edge;
        this.sourceGUI = sourceGUI;
        this.destinationGUI = destinationGUI;
        this.lineWidth = 2;
        this.lineColor = Color.BLACK;
        this.textColor = Color.BLACK;
        this.arrowHeadSize = 7;
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

    public void draw(GraphicsContext context, boolean drawWeight, boolean isDirected) {
        double angleRad = sourceGUI.getPosition().angle(destinationGUI.getPosition());

        // Draw line between two nodes
        context.setStroke(lineColor);
        context.setLineWidth(lineWidth);
        context.strokeLine(
                sourceGUI.getPosition().getX(), sourceGUI.getPosition().getY(),
                destinationGUI.getPosition().getX(), destinationGUI.getPosition().getY());

        if (drawWeight) {
            // Draw weight text in the middle
            Point2D middlePosition = sourceGUI.getPosition().add(destinationGUI.getPosition()).multiply(0.5);
            Point2D offset = new Point2D(Math.sin(angleRad), -Math.cos(angleRad)).multiply(15 + lineWidth);

            String weightString = Double.toString(edge.getWeight());
            double textX = middlePosition.getX() + offset.getX();
            double textY = middlePosition.getY() + offset.getY();

            context.setFill(textColor);
            context.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
            context.fillText(weightString, textX, textY);

            // TODO: bikin tulisan ga overlap dengan edge kalo mau
        }

        if (isDirected) {
            // Draw arrow head
            Point2D offset1 = new Point2D(Math.cos(angleRad), Math.sin(angleRad))
                    .multiply(-1 * destinationGUI.getRadius());
            Point2D point1 = destinationGUI.getPosition().add(offset1);

            Point2D offset2 = new Point2D(Math.cos(angleRad + 30), Math.sin(angleRad + 30)).multiply(arrowHeadSize);
            Point2D point2 = point1.add(offset2);

            Point2D offset3 = new Point2D(Math.cos(angleRad - 30), Math.sin(angleRad - 30)).multiply(arrowHeadSize);
            Point2D point3 = point1.add(offset3);

            context.setFill(lineColor);
            context.fillPolygon(
                    new double[] { point1.getX(), point2.getX(), point3.getX() },
                    new double[] { point1.getY(), point2.getY(), point3.getY() },
                    3);
        }

    }

    public void update() {
        // Add spring force to both node
        // Hooke's Law: F = -kx
        Point2D offset = destinationGUI.getPosition().subtract(sourceGUI.getPosition());
        Point2D springForce = offset.normalize().multiply(SPRING_CONSTANT * (offset.magnitude() - SPRING_LENGTH));
        sourceGUI.addForce(springForce);
        destinationGUI.addForce(springForce.multiply(-1));
    }
}
