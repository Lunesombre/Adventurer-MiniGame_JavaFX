package game.adventurer.util;

import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.LoggerFactory;

public class PathfindingUtil {

  public interface TileValidator {

    boolean isValidTile(int x, int y);
  }

  /**
   * Checks if there is a valid path between two points on a grid using Breadth-First Search (BFS).
   *
   * @param startX    The starting X coordinate.
   * @param startY    The starting Y coordinate.
   * @param endX      The ending X coordinate.
   * @param endY      The ending Y coordinate.
   * @param width     The width of the grid.
   * @param height    The height of the grid.
   * @param validator A TileValidator to determine if a tile is valid for the path.
   * @return true if a path exists, false otherwise.
   */
  public static boolean hasPath(int startX, int startY, int endX, int endY, int width, int height, TileValidator validator) {
    // Keeping track of visited tiles with a bi-dimensional array
    boolean[][] visited = new boolean[height][width];
    // Create a queue for BFS. Each element is an array: [y, x]
    Queue<int[]> queue = new LinkedList<>();
    // Add starting position (adventurer) to queue
    queue.offer(new int[]{startY, startX});
    visited[startY][startX] = true;

    // Define possible directions (up, right, down, left)
    int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    while (!queue.isEmpty()) {
      int[] current = queue.poll();
      int y = current[0];
      int x = current[1];

      // If the treasure is reached, a path exists
      if (y == endY && x == endX) {
        return true;
      }

      // Verifies all directions
      for (int[] dir : directions) {
        int newY = y + dir[0];
        int newX = x + dir[1];

        // Check that the new position is valid and not visited
        if (newY >= 0 && newY < height && newX >= 0 && newX < width
            && !visited[newY][newX] && validator.isValidTile(newX, newY)) {
          queue.offer(new int[]{newY, newX});
          visited[newY][newX] = true;
        }
      }
    }

    // If we get here, no path was found.
    return false;
  }

  /**
   * Finds the shortest path between two points on a grid using Breadth-First Search (BFS).
   *
   * @param startX    The starting X coordinate.
   * @param startY    The starting Y coordinate.
   * @param endX      The ending X coordinate.
   * @param endY      The ending Y coordinate.
   * @param width     The width of the grid.
   * @param height    The height of the grid.
   * @param validator A TileValidator to determine if a tile is valid for the path.
   * @return The length of the shortest path, or -1 if no path exists.
   */
  public static int shortestPath(int startX, int startY, int endX, int endY, int width, int height, TileValidator validator) {
    LoggerFactory.getLogger(PathfindingUtil.class).debug("treasure: ({}, {})", endX, endY);
    // Keeping track of visited tiles with a bi-dimensional array
    boolean[][] visited = new boolean[height][width];
    // Create a queue for BFS. Each element is an array: [y, x, distance]
    Queue<int[]> queue = new LinkedList<>();

    // Add the starting point to the queue and mark it as visited
    queue.offer(new int[]{startX, startY, 0}); // x, y, distance
    visited[startY][startX] = true;

    int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // Up, Right, Down, Left

    while (!queue.isEmpty()) {
      int[] current = queue.poll();
      int x = current[0];
      int y = current[1];
      int distance = current[2];

      // If we've reached the end point, return the distance
      if (x == endX && y == endY) {
        LoggerFactory.getLogger(PathfindingUtil.class).debug("Shortest path found: {}", distance);
        return distance;
      }

      for (int[] dir : directions) {
        int newX = x + dir[0];
        int newY = y + dir[1];

        // Check if the new position is within bounds, not visited, and valid
        if (newX >= 0 && newX < width && newY >= 0 && newY < height
            && !visited[newY][newX] && validator.isValidTile(newX, newY)) {
          queue.offer(new int[]{newX, newY, distance + 1});
          visited[newY][newX] = true;
        }
      }
    }

    return -1; // No path found
  }

}

