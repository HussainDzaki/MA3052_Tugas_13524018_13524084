package ma3052.gui;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ma3052.graph.GridGraph;
import ma3052.graph.IslandCounter;

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
    private static final Color LAND_COLOR = Color.web("#268510"); // Black for land (#)
    private static final Color GRID_LINE_COLOR = Color.web("#CCCCCC"); // Light gray for grid lines
    private static final Color BORDER_COLOR = Color.web("#333333"); // Dark gray for borders

    private volatile boolean isDrawing = true;
    private double cellSize = 20; // Size of each cell in pixels
    
    // Animation state
    private volatile int[][] islandMap = null;
    private volatile int[][] finalIslandMap = null;
    private volatile boolean isAnimating = false;

    public GridGraphGUI(Canvas canvas) {
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        threadPoolExecutor.scheduleWithFixedDelay(() -> {
            if (isDrawing && gridGraph != null) {
                updateCanvas();
            }
        }, 0, 90000, TimeUnit.NANOSECONDS);

        Platform.runLater(() -> {
            canvas.getScene().getWindow().setOnCloseRequest(event -> {
                threadPoolExecutor.shutdown();
            });
        });
    }

    /**
     * Set the grid graph to visualize
     */
    public void setGridGraph(GridGraph grid) {
        this.gridGraph = grid;
    }

    /**
     * Get the current grid graph
     */
    public GridGraph getGridGraph() {
        return this.gridGraph;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    /**
     * Update canvas with current grid state
     */
    private void updateCanvas() {
        Platform.runLater(() -> {
            clearCanvas();
            drawGrid();
        });
    }

    private void clearCanvas() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Draw the grid on canvas
     */
    private void drawGrid() {
        if (gridGraph == null)
            return;

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

        // Color palette for islands during animation
        Color[] ISLAND_COLORS = {
                Color.web("#FF6B6B"), Color.web("#4ECDC4"), Color.web("#FFE66D"),
                Color.web("#95E1D3"), Color.web("#F38181"), Color.web("#AA96DA"),
                Color.web("#FCBAD3"), Color.web("#A8D8EA"), Color.web("#FFD3B6"),
                Color.web("#FF9F1C")
        };

        // Draw cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = startX + j * cellSize;
                double y = startY + i * cellSize;
                
                Color cellColor;
                
                // Determine which map to use
                int[][] mapToUse = null;
                if (isAnimating && islandMap != null) {
                    mapToUse = islandMap;
                } else if (finalIslandMap != null) {
                    mapToUse = finalIslandMap;
                }
                
                // If using animation map, apply colors
                if (mapToUse != null) {
                    if (mapToUse[i][j] == -1) {
                        // Water
                        cellColor = WATER_COLOR;
                    } else if (mapToUse[i][j] == -2) {
                        // Unvisited land
                        cellColor = LAND_COLOR;
                    } else if (mapToUse[i][j] == -3) {
                        // Small island (not the largest) - render as default green
                        cellColor = LAND_COLOR;
                    } else {
                        // Island - assign color based on island number
                        cellColor = ISLAND_COLORS[mapToUse[i][j] % ISLAND_COLORS.length];
                    }
                } else {
                    // Default rendering
                    char nodeType = gridGraph.getNodeType(i, j);
                    if (nodeType == '#') {
                        cellColor = LAND_COLOR;
                    } else {
                        cellColor = WATER_COLOR;
                    }
                }
                
                graphicsContext.setFill(cellColor);
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
     * Get total island count in the grid
     */
    public int getTotalIsland() {
        if (gridGraph == null) {
            return 0;
        }
        return IslandCounter.getTotalIsland(gridGraph, '#');
    }

    /**
     * Stop the rendering thread
     */
    public void stop() {
        isDrawing = false;
        threadPoolExecutor.shutdown();
    }


    /**
     * Set island map for animation visualization
     */
    public void setIslandMap(int[][] map) {
        this.islandMap = map;
    }
    
    /**
     * Get current island map
     */
    public int[][] getIslandMap() {
        return this.islandMap;
    }
    
    /**
     * Set final island map to persist after animation
     */
    public void setFinalIslandMap(int[][] map) {
        this.finalIslandMap = map;
    }
    
    /**
     * Get final island map
     */
    public int[][] getFinalIslandMap() {
        return this.finalIslandMap;
    }
    
    /**
     * Set animation state
     */
    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }
    
    /**
     * Check if currently animating
     */
    public boolean isAnimating() {
        return this.isAnimating;
    }

    /**
     * Get Canvas
     */
    public Canvas getCanvas(){
        return this.canvas;
    }
}
