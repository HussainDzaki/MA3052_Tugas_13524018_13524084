package ma3052.gui;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ma3052.graph.GridGraph;


import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Grid Graph visualization component
 * Displays grid cells with water (.) colored blue and land (#) colored black
 */
public class GridGraphGUI {
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private GridGraph gridGraph;
    private ScheduledThreadPoolExecutor threadPoolExecutor;

    // Visualization parameters
    private static final Color WATER_COLOR = Color.web("#4DA6FF"); // Blue for water (.)
    private static final Color LAND_COLOR = Color.web("#000000");   // Black for land (#)
    private static final Color GRID_LINE_COLOR = Color.web("#CCCCCC"); // Light gray for grid lines
    private static final Color BORDER_COLOR = Color.web("#333333");    // Dark gray for borders

    private volatile boolean isDrawing = true;
    private double cellSize = 20; // Size of each cell in pixels


    public GridGraphGUI(Canvas canvas){
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        
        // Start rendering thread with proper frame rate (60 FPS = 16ms per frame)
        threadPoolExecutor.scheduleWithFixedDelay(() -> {
            if (isDrawing && gridGraph != null) {
                updateCanvas();
            }
        }, 0, 90000, TimeUnit.NANOSECONDS); // 60 FPS
    }

    /**
     * Set the grid graph to visualize
     */
    public void setGridGraph(GridGraph grid){
        this.gridGraph = grid;
    }

    /**
     * Get the current grid graph
     */
    public GridGraph getGridGraph(){
        return this.gridGraph;
    }

    /**
     * Update canvas with current grid state
     */
    private void updateCanvas(){
        Platform.runLater(this::drawGrid);
    }

    /**
     * Draw the grid on canvas
     */
    private void drawGrid(){
        if (gridGraph == null) return;

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Calculate cell size to fit grid in canvas
        int rows = gridGraph.getRowSize();
        int cols = gridGraph.getColSize();
        double cellWidth = (width - 10) / cols;
        double cellHeight = (height - 10) / rows;
        double cellSize = Math.min(cellWidth, cellHeight);
        double startX = (width - cols * cellSize) / 2;
        double startY = (height - rows * cellSize) / 2;

        // Draw cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char nodeType = gridGraph.getNodeType(i, j);
                double x = startX + j * cellSize;
                double y = startY + i * cellSize;

                // Draw cell background based on type
                if (nodeType == '#') {
                    graphicsContext.setFill(LAND_COLOR);
                } else {
                    graphicsContext.setFill(WATER_COLOR);
                }
                graphicsContext.fillRect(x, y, cellSize, cellSize);

                // Draw cell border
                graphicsContext.setStroke(GRID_LINE_COLOR);
                graphicsContext.setLineWidth(1);
                graphicsContext.strokeRect(x, y, cellSize, cellSize);
            }
        }

        // Draw outer border
        double gridWidth = cols * cellSize;
        double gridHeight = rows * cellSize;
        graphicsContext.setStroke(BORDER_COLOR);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(startX, startY, gridWidth, gridHeight);
    }


    /**
     * Stop the rendering thread
     */
    public void stop(){
        isDrawing = false;
        threadPoolExecutor.shutdown();
    }

}
