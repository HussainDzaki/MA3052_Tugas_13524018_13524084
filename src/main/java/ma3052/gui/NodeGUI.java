package ma3052.gui;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ma3052.graph.Node;

public class NodeGUI {
    // Node
    private Node node;

    // Visual
    private double radius;
    private double borderWidth;
    private boolean drawLabel;
    private boolean drawValue;

    // Colors
    private Color color;
    private Color borderColor;
    private Color textColor;

    // Physics
    private Point2D position;
    private Point2D velocity;
    private Point2D force;
    private boolean lockPosition;

    // Physics config
    private static final double DEFAULT_RADIUS = 20; // Default node radius
    private static final double DAMPENING = 0.7; // Friction/dampening
    private static final double MAX_VELOCITY = 500.0; // Maximum node velocity

    public NodeGUI(Node node) {
        this.node = node;
        this.position = new Point2D(0, 0);
        this.velocity = new Point2D(0, 0);
        this.force = new Point2D(0, 0);
        this.radius = DEFAULT_RADIUS;
        this.drawLabel = true;
        this.drawValue = false;
        this.borderWidth = 3;
        this.color = Color.WHITE;
        this.borderColor = Color.BLACK;
        this.textColor = Color.BLACK;
        this.lockPosition = false;
    }

    public NodeGUI(Node node, Point2D position) {
        this.node = node;
        this.position = position;
        this.velocity = new Point2D(0, 0);
        this.force = new Point2D(0, 0);
        this.radius = DEFAULT_RADIUS;
        this.drawLabel = true;
        this.drawValue = false;
        this.borderWidth = 3;
        this.color = Color.WHITE;
        this.borderColor = Color.BLACK;
        this.textColor = Color.BLACK;
        this.lockPosition = false;
    }

    public Node getNode() {
        return node;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Point2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Point2D velocity) {
        this.velocity = velocity;
    }

    public Point2D getForce() {
        return force;
    }

    public void setForce(Point2D force) {
        this.force = force;
    }

    public void addForce(Point2D force) {
        this.force = this.force.add(force);
    }

    public boolean isLockPosition() {
        return lockPosition;
    }

    public void setLockPosition(boolean lockPosition) {
        this.lockPosition = lockPosition;
    }

    public boolean isDrawLabel() {
        return drawLabel;
    }

    public void setDrawLabel(boolean drawLabel) {
        this.drawLabel = drawLabel;
    }

    public boolean isDrawValue() {
        return drawValue;
    }

    public void setDrawValue(boolean drawValue) {
        this.drawValue = drawValue;
    }

    public void draw(GraphicsContext context) {
        // Draw node circle with color
        context.setFill(color);
        context.fillOval(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);

        // Draw border
        context.setStroke(borderColor);
        context.setLineWidth(borderWidth);
        context.strokeOval(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);

        if (node == null)
            return;

        if (drawLabel && !drawValue) {
            // Draw label centered in node
            context.setFill(textColor);
            context.setFont(Font.font("Cascadia Code Regular", FontWeight.NORMAL, 12));

            // Center the text
            double textWidth = node.getNodeName().length() * 7;
            double textX = position.getX() - textWidth / 2;
            double textY = position.getY() + 5;

            context.fillText(node.getNodeName(), textX, textY);
        } else if (!drawLabel && drawValue) {
            // Draw value centered in node
            context.setFill(textColor);
            context.setFont(Font.font("Cascadia Code Regular", FontWeight.NORMAL, 12));

            // Center the text
            String valueString = Double.toString(node.getValue());
            valueString = valueString.replace("Infinity", "∞");
            double valueWidth = valueString.length() * 7;
            double valueX = position.getX() - valueWidth / 2;
            double valueY = position.getY() + 5;

            context.fillText(valueString, valueX, valueY);
        } else if (drawLabel && drawValue) {
            // Draw label centered in node
            context.setFill(textColor);
            context.setFont(Font.font("Cascadia Code Regular", FontWeight.NORMAL, 12));

            // Draw value in the center
            String valueString = Double.toString(node.getValue());
            valueString = valueString.replace("Infinity", "∞");
            double valueWidth = valueString.length() * 7;
            double valueX = position.getX() - valueWidth / 2;
            double valueY = position.getY() + 5;

            context.fillText(valueString, valueX, valueY);

            // Draw name in the top center outside the node
            double nameWidth = node.getNodeName().length() * 7;
            double nameX = position.getX() - nameWidth / 2;
            double nameY = position.getY() + 10 + radius + borderWidth;

            context.fillText(node.getNodeName(), nameX, nameY);
        }

    }

    public void update(double deltaTime) {
        velocity = velocity.add(force.multiply(deltaTime)).multiply(DAMPENING);

        if (velocity.magnitude() > MAX_VELOCITY) {
            velocity = velocity.normalize().multiply(MAX_VELOCITY);
        }

        if (!lockPosition) {
            position = position.add(velocity.multiply(deltaTime));
        }
    }

    public void clampPosition(double minx, double miny, double maxx, double maxy) {
        position = new Point2D(Math.clamp(position.getX(), minx, maxx), Math.clamp(position.getY(), miny, maxy));
    }
}
