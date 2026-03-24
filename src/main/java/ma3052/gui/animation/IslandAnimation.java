package ma3052.gui.animation;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ma3052.graph.GridGraph;
import ma3052.graph.Node;
import ma3052.gui.GridGraphGUI;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Island Animation - Visualizes islands using DFS with different colors
 * Each island is marked with a unique color as it's discovered
 */
public class IslandAnimation {
    private static volatile long animationStepTime = 300; // in milliseconds
    
    public static void setAnimationStepTime(long animationStepTime) {
        IslandAnimation.animationStepTime = animationStepTime;
    }

    /**
     * Animate island detection using DFS
     * @param gridGraphGUI The grid graph GUI component
     * @param landChar The character representing land ('#')
     * @param onComplete Callback when animation completes
     */
    public static void animate(GridGraphGUI gridGraphGUI, char landChar, Runnable onComplete) {
        if (gridGraphGUI == null || gridGraphGUI.getGridGraph() == null)
            return;

        // Run animation in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                performIslandAnimation(gridGraphGUI, gridGraphGUI.getGridGraph(), landChar);
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Animate largest/biggest island detection using BFS
     * @param gridGraphGUI The grid graph GUI component
     * @param landChar The character representing land ('#')
     * @param onComplete Callback when animation completes
     */
    public static void animateLargestComponent(GridGraphGUI gridGraphGUI, char landChar, Runnable onComplete) {
        if (gridGraphGUI == null || gridGraphGUI.getGridGraph() == null)
            return;

        // Run animation in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                performLargestComponentAnimation(gridGraphGUI, gridGraphGUI.getGridGraph(), landChar);
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Animate largest/biggest island detection using BFS
     * @param gridGraphGUI The grid graph GUI component
     * @param landChar The character representing land ('#')
     */
    public static void animateLargestComponent(GridGraphGUI gridGraphGUI, char landChar) throws InterruptedException {
        if (gridGraphGUI == null || gridGraphGUI.getGridGraph() == null)
            return;

        new Thread(() -> {
            try {
                performLargestComponentAnimation(gridGraphGUI, gridGraphGUI.getGridGraph(), landChar);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Perform the actual island animation using BFS
     */
    private static void performIslandAnimation(GridGraphGUI gridGraphGUI, GridGraph gridGraph, char landChar) throws InterruptedException {
        int rows = gridGraph.getRowSize();
        int cols = gridGraph.getColSize();
        
        // Map to store which island each cell belongs to (-1 = water, 0+ = island number)
        int[][] islandMap = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gridGraph.getNodeType(i, j) != landChar) {
                    islandMap[i][j] = -1; // Water
                } else {
                    islandMap[i][j] = -2; // Unvisited land
                }
            }
        }
        
        // Signal animation start
        Platform.runLater(() -> {
            gridGraphGUI.setAnimating(true);
            gridGraphGUI.setIslandMap(islandMap);
        });
        
        Set<Long> globalVisited = new HashSet<>();
        int islandCount = 0;
        List<Set<Pair<Integer, Integer>>> islands = new ArrayList<>();
        
        // Perform BFS for each unvisited land cell
        for (int i = 0; i < rows; i++) {
            if (Thread.currentThread().isInterrupted())
                break;
                
            for (int j = 0; j < cols; j++) {
                if (Thread.currentThread().isInterrupted())
                    break;
                    
                Node node = gridGraph.getNode(i, j);
                if (gridGraph.getNodeType(node) != landChar || globalVisited.contains(node.getNodeID())) {
                    continue;
                }
                
                // Found a new island - perform BFS
                Set<Pair<Integer, Integer>> currentIsland = new HashSet<>();
                Queue<Pair<Integer, Integer>> bfsQueue = new LinkedList<>();
                bfsQueue.add(new Pair<>(i, j));
                
                int currentIslandNumber = islandCount;
                islands.add(currentIsland);
                
                while (!bfsQueue.isEmpty()) {
                    if (Thread.currentThread().isInterrupted())
                        break;
                        
                    Pair<Integer, Integer> pos = bfsQueue.poll();
                    int row = pos.getKey();
                    int col = pos.getValue();
                    
                    Node currentNode = gridGraph.getNode(row, col);
                    
                    if (globalVisited.contains(currentNode.getNodeID())) {
                        continue;
                    }
                    
                    globalVisited.add(currentNode.getNodeID());
                    islandMap[row][col] = currentIslandNumber;
                    currentIsland.add(new Pair<>(row, col));
                    
                    // Update GridGraphGUI with new island map
                    final int[][] mapCopy = deepCopyIslandMap(islandMap);
                    Platform.runLater(() -> {
                        gridGraphGUI.setIslandMap(mapCopy);
                    });
                    
                    Thread.sleep((long) (animationStepTime));
                    
                    // Add adjacent land cells to queue
                    // Check up, down, left, right (4-directional)
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    
                    for (int[] dir : directions) {
                        int newRow = row + dir[0];
                        int newCol = col + dir[1];
                        
                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            Node adjacentNode = gridGraph.getNode(newRow, newCol);
                            if (gridGraph.getNodeType(adjacentNode) == landChar && 
                                !globalVisited.contains(adjacentNode.getNodeID())) {
                                bfsQueue.add(new Pair<>(newRow, newCol));
                            }
                        }
                    }
                }
                
                islandCount++;
            }
        }
        
        // Final update
        Platform.runLater(() -> {
            gridGraphGUI.setIslandMap(islandMap);
            gridGraphGUI.setAnimating(false);
            // Keep the island colors by storing the final map
            gridGraphGUI.setFinalIslandMap(islandMap);
        });
    }
    
    /**
     * Perform animation to find and highlight the largest island(s) using BFS
     */
    private static void performLargestComponentAnimation(GridGraphGUI gridGraphGUI, GridGraph gridGraph, char landChar) throws InterruptedException {
        int rows = gridGraph.getRowSize();
        int cols = gridGraph.getColSize();
        
        // Map to store which island each cell belongs to
        int[][] islandMap = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gridGraph.getNodeType(i, j) != landChar) {
                    islandMap[i][j] = -1; // Water
                } else {
                    islandMap[i][j] = -2; // Unvisited land
                }
            }
        }
        
        // Signal animation start
        Platform.runLater(() -> {
            gridGraphGUI.setAnimating(true);
            gridGraphGUI.setIslandMap(islandMap);
        });
        
        Set<Long> globalVisited = new HashSet<>();
        int islandCount = 0;
        int largestSize = 0;
        List<Integer> largestIslandNumbers = new ArrayList<>(); // List to store all islands with max size
        List<Integer> islandSizes = new ArrayList<>(); // Track size of each island
        
        // Find all islands and track the largest using BFS
        for (int i = 0; i < rows; i++) {
            if (Thread.currentThread().isInterrupted())
                break;
                
            for (int j = 0; j < cols; j++) {
                if (Thread.currentThread().isInterrupted())
                    break;
                    
                Node node = gridGraph.getNode(i, j);
                if (gridGraph.getNodeType(node) != landChar || globalVisited.contains(node.getNodeID())) {
                    continue;
                }
                
                // Found a new island - perform BFS
                Queue<Pair<Integer, Integer>> bfsQueue = new LinkedList<>();
                bfsQueue.add(new Pair<>(i, j));
                
                int currentIslandNumber = islandCount;
                int currentSize = 0;
                
                while (!bfsQueue.isEmpty()) {
                    if (Thread.currentThread().isInterrupted())
                        break;
                        
                    Pair<Integer, Integer> pos = bfsQueue.poll();
                    int row = pos.getKey();
                    int col = pos.getValue();
                    
                    Node currentNode = gridGraph.getNode(row, col);
                    
                    if (globalVisited.contains(currentNode.getNodeID())) {
                        continue;
                    }
                    
                    globalVisited.add(currentNode.getNodeID());
                    islandMap[row][col] = currentIslandNumber;
                    currentSize++;
                    
                    // Update GridGraphGUI with new island map
                    final int[][] mapCopy = deepCopyIslandMap(islandMap);
                    Platform.runLater(() -> {
                        gridGraphGUI.setIslandMap(mapCopy);
                    });
                    
                    Thread.sleep((long) (animationStepTime));
                    
                    // Add adjacent land cells to queue
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    
                    for (int[] dir : directions) {
                        int newRow = row + dir[0];
                        int newCol = col + dir[1];
                        
                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                            Node adjacentNode = gridGraph.getNode(newRow, newCol);
                            if (gridGraph.getNodeType(adjacentNode) == landChar && 
                                !globalVisited.contains(adjacentNode.getNodeID())) {
                                bfsQueue.add(new Pair<>(newRow, newCol));
                            }
                        }
                    }
                }
                
                // Track island size
                islandSizes.add(currentSize);
                
                // Track all islands with the largest size
                if (currentSize > largestSize) {
                    largestSize = currentSize;
                    largestIslandNumbers.clear(); // Clear previous islands
                    largestIslandNumbers.add(currentIslandNumber);
                } else if (currentSize == largestSize) {
                    largestIslandNumbers.add(currentIslandNumber); // Add to list if same size
                }
                
                islandCount++;
            }
        }
        
        // Highlight only the largest island(s) - color them distinctly
        // Color all other islands as default green (land color = -3)
        int[][] finalMap = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (largestIslandNumbers.contains(islandMap[i][j])) {
                    // This cell belongs to a largest island - keep with color
                    finalMap[i][j] = islandMap[i][j];
                } else if (islandMap[i][j] >= 0) {
                    // This cell is land but not a largest island - color it green
                    finalMap[i][j] = -3; // Special code for small islands (will render as land green)
                } else {
                    // Water - keep water color
                    finalMap[i][j] = islandMap[i][j];
                }
            }
        }
        
        // Final update - show largest island(s) in color, all other islands in green
        Platform.runLater(() -> {
            gridGraphGUI.setIslandMap(finalMap);
            gridGraphGUI.setAnimating(false);
            gridGraphGUI.setFinalIslandMap(finalMap);
        });
    }
    
    /**
     * Helper method to deep copy island map
     */
    private static int[][] deepCopyIslandMap(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}
