package game.adventurer.util;

import static game.adventurer.util.MiscUtil.getDistance;
import static game.adventurer.util.MiscUtil.isOutOfMapBounds;

import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.enums.Move;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
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

  public static boolean hasPath(Position startPosition, Position targetPosition, int width, int height, TileValidator validator) {
    return hasPath(startPosition.x(), startPosition.y(), targetPosition.x(), targetPosition.y(), width, height, validator);
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

  // A* style
  public static Set<Position> shortestPath(Creature creature, Position start, Position goal, GameMap gameMap) {
    Set<Type> allowedTileTypes = creature.getAllowedTileTypes();
    PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::fCost)); // nodes to be explored, sorted by their smallest cost
    Map<Position, Node> allNodes = new HashMap<>();
    Set<Position> closedSet = new HashSet<>(); // already explored Nodes

    // Initial node
    Node startNode = new Node(start, null, 0, getDistance(start, goal));
    openSet.add(startNode);
    allNodes.put(start, startNode);

    while (!openSet.isEmpty()) {
      Node current = openSet.poll();

      // If goal is reached
      if (current.position.equals(goal)) {
        return reconstructPath(current);
      }

      closedSet.add(current.position);

      // Explore neighbors
      for (Position neighbor : getValidNeighbors(current.position, gameMap, allowedTileTypes, false)) {
        if (closedSet.contains(neighbor)) {
          continue;
        }

        if (gameMap.isTileOccupied(neighbor.x(), neighbor.y())) {
          continue; // Avoiding occupied Tiles
        }

        int tentativeGCost = current.gCost + 1; // Uniform cost (1 per tile) for now, might change that later depending on Creature type
        // or terrain elevation if it comes to be a thing
        Node neighborNode = allNodes.getOrDefault(neighbor, new Node(neighbor));

        if (tentativeGCost < neighborNode.gCost) {
          neighborNode.gCost = tentativeGCost;
          neighborNode.hCost = getDistance(neighbor, goal);
          neighborNode.parent = current;

          allNodes.put(neighbor, neighborNode);

          if (!openSet.contains(neighborNode)) {
            openSet.add(neighborNode);
          } else {
            openSet.remove(neighborNode);
            openSet.add(neighborNode); // forces the openSet to re-sort when a Node min cost is lowered
          }
        }
      }
    }

    return new LinkedHashSet<>(); // No path found

  }

  private static LinkedHashSet<Position> reconstructPath(Node goalNode) {
    Deque<Position> path = new ArrayDeque<>();
    Node current = goalNode;

    while (current != null) {
      path.addFirst(current.position); // adds the position on the top of the queue
      current = current.parent;
    }
    path.removeFirst(); // removes the current position of the Creature from the path
    return new LinkedHashSet<>(path); // the path will be in correct order, from the target to the goal
  }

  /**
   * Retrieves the valid neighboring positions for a given position on the game map.
   *
   * <p>This method checks all possible moves from the given position and returns the positions
   * that are within the map bounds, of an allowed tile type, and optionally not occupied.
   *
   * @param position                     The current position.
   * @param gameMap                      The game map containing the grid and tile information.
   * @param allowedTileTypes             The set of tile types that are considered valid.
   * @param takeIntoAccountOccupiedTiles If true, only unoccupied tiles are considered valid.
   * @return A set of valid neighboring positions.
   */
  public static Set<Position> getValidNeighbors(Position position, GameMap gameMap, Set<Type> allowedTileTypes,
      boolean takeIntoAccountOccupiedTiles) {
    Set<Position> neighbors = new HashSet<>();

    for (Move move : Move.values()) {
      int newX = position.x() + move.getDx();
      int newY = position.y() + move.getDy();
      boolean validTile = !isOutOfMapBounds(gameMap, newX, newY)
          && allowedTileTypes.contains(gameMap.getTileTypeAt(newX, newY))
          && (!takeIntoAccountOccupiedTiles || !gameMap.isTileOccupied(newX, newY));
      if (validTile) {
        neighbors.add(new Position(newX, newY));
      }
    }

    return neighbors;
  }

  /**
   * Retrieves a valid neighboring position for the monster to move to, considering the game map constraints and whether the monster is in search
   * mode.
   *
   * @param monster  The monster for which to find a valid neighboring position.
   * @param gameMap  The game map containing tile information and boundaries.
   * @param inSearch A boolean flag indicating whether the monster is in search mode. If true, the method ensures the neighboring position is within
   *                 the monster's search area.
   * @return A valid neighboring {@code Position} that the monster can move to, or {@code null} if no valid neighboring position is found.
   */
  public static Position getValidNeighbor(Monster monster, GameMap gameMap, boolean inSearch) {

    // Convert the array of Move values to a List
    List<Move> moves = new ArrayList<>(List.of(Move.values()));

    // Shuffle the list to randomize the order of moves
    Collections.shuffle(moves);

    for (Move move : moves) {
      int newX = monster.getTileX() + move.getDx();
      int newY = monster.getTileY() + move.getDy();
      Position position = new Position(newX, newY);
      boolean searchAreaFail = false;
      if (inSearch) {
        searchAreaFail = !monster.getSearchArea().contains(position);
      }
      boolean validTile = !searchAreaFail
          && !isOutOfMapBounds(gameMap, newX, newY)
          && monster.getAllowedTileTypes().contains(gameMap.getTileTypeAt(newX, newY))
          && !gameMap.isTileOccupied(newX, newY);
      if (validTile) {
        return position;
      }
    }

    return null;
  }


  // Internal class for A* nodes
  private static class Node {

    Position position;
    Node parent;
    int gCost; // Real cost from start to this node
    int hCost; // Heuristic cost from this node to goal (using Manhattan distance here)

    Node(Position position) {
      this(position, null, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    Node(Position position, Node parent, int gCost, int hCost) {
      this.position = position;
      this.parent = parent;
      this.gCost = gCost;
      this.hCost = hCost;
    }

    /**
     * The method approximates the cost to reach goal by adding the real cost from start to a given node (gCost) and the estimated cost from a given
     * node to the goal (hCost)
     *
     * @return an int that is the total estimated cost to reach goal
     */
    int fCost() {
      return gCost + hCost;
    }

  }

  /**
   * Calculates the shortest distance between two points on a grid using the A* algorithm.
   *
   * @param creature The creature for which the path is being calculated.
   * @param start    The starting position.
   * @param goal     The goal position.
   * @param gameMap  The game map containing the grid and tile information.
   * @return The length of the shortest path, or -1 if no path exists.
   */
  public static int shortestDistance(Creature creature, Position start, Position goal, GameMap gameMap) {
    Set<Type> allowedTileTypes = creature.getAllowedTileTypes();
    PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::fCost));
    Map<Position, Integer> gCosts = new HashMap<>();
    Set<Position> closedSet = new HashSet<>();

    // Initial node
    Node startNode = new Node(start, null, 0, getDistance(start, goal));
    openSet.add(startNode);
    gCosts.put(start, 0);

    while (!openSet.isEmpty()) {
      Node current = openSet.poll();

      if (current.position.equals(goal)) {
        return current.gCost; // Return the distance as soon as the goal is reached
      }

      closedSet.add(current.position);

      for (Position neighbor : getValidNeighbors(current.position, gameMap, allowedTileTypes, false)) {
        if (closedSet.contains(neighbor)) {
          continue;
        }

        int tentativeGCost = current.gCost + 1;

        if (tentativeGCost < gCosts.getOrDefault(neighbor, Integer.MAX_VALUE)) {
          gCosts.put(neighbor, tentativeGCost);
          Node neighborNode = new Node(neighbor, null, tentativeGCost, getDistance(neighbor, goal));

          if (!openSet.contains(neighborNode)) {
            openSet.add(neighborNode);
          } else {
            openSet.remove(neighborNode);
            openSet.add(neighborNode);
          }
        }
      }
    }

    return -1; // No path found
  }

  public static Set<Position> calculateSearchArea(Monster monster, Position lastSeenPosition, GameMap gameMap) {
    Set<Position> searchArea = new HashSet<>();
    Queue<Position> queue = new LinkedList<>();
    Set<Position> visited = new HashSet<>();

    queue.add(lastSeenPosition);
    visited.add(lastSeenPosition);

    TileValidator validator = (x, y) -> {
      Position pos = new Position(x, y);
      // Check if the tile is within the allowed types and not in the monster's FOV when it lost sight of the Adventurer,
      // or on the path to the last known Adventurer position
      return monster.getAllowedTileTypes().contains(gameMap.getTileTypeAt(x, y))
          && monster.getStoredFOV().contains(pos);
    };

    while (!queue.isEmpty()) {
      Position current = queue.poll();
      searchArea.add(current);

      for (Position neighbor : getValidNeighbors(current, gameMap, monster.getAllowedTileTypes(), false)) {
        if (!visited.contains(neighbor)
            && getDistance(lastSeenPosition, neighbor) <= 6
            && hasPath(lastSeenPosition, neighbor, gameMap.getMapWidth(), gameMap.getMapHeight(), validator)) {
          queue.add(neighbor);
          visited.add(neighbor);
        }
      }
    }
    log.info("CalculatedSearchArea size : {}", searchArea.size());
    return searchArea;
  }

  /**
   * Finds the nearest tile of a given type from a starting position using a breadth-first search (BFS). This ensures the shortest path in terms of
   * tile traversal is found.
   *
   * @param startPosition The starting position from which the search begins.
   * @param gameMap       The game map containing the tiles.
   * @param type          The tile type to search for.
   * @return The position of the nearest tile of the specified type, or {@code null} if no such tile is found.
   */
  public static Position findNearestTileOfType(Position startPosition, GameMap gameMap, Tile.Type type) {
    Set<Position> visited = new HashSet<>();
    Queue<Position> queue = new LinkedList<>();
    queue.add(startPosition);
    visited.add(startPosition);

    while (!queue.isEmpty()) {
      Position current = queue.poll();

      if (gameMap.getTileTypeAt(current.x(), current.y()) == type) {
        return current;
      }

      // Use getValidNeighbors to get valid neighboring positions
      Set<Position> neighbors = getValidNeighbors(current, gameMap, Set.of(Type.values()), false);

      for (Position neighbor : neighbors) {
        if (!visited.contains(neighbor)) {
          queue.add(neighbor);
          visited.add(neighbor);
        }
      }
    }

    // Return null if no tile of said type is found
    return null;
  }

}
