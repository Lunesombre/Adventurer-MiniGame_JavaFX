package game.adventurer.service;

import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.Adventurer;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.Treasure;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MapGenerator {

  public static final float MIN_WOOD_PERCENTAGE = 0.25f;
  public static final float MAX_WOOD_PERCENTAGE = 0.5f;
  public static final float MIN_DISTANCE_FROM_ADVENTURER_PERCENTAGE = 0.25f;
  public static final Logger LOG = LoggerFactory.getLogger(MapGenerator.class);
  private static final RandomGenerator random = RandomGenerator.getDefault();

  public static GameMap generateMap(int width, int height) throws NoValidRangeException {
    GameMap map;
    Adventurer adventurer;
    Treasure treasure;
    int loopCounter = 0;
    do {
      loopCounter += 1;
      Tile[][] grid = new Tile[width][height];
      // Map generation logic
      // Initialize each Tile
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          grid[y][x] = new Tile(Tile.Type.PATH, x, y); // default, PATH
        }
      }

      // Générer des zones de bois aléatoires
      generateWoodAreas(grid, width, height);

      // Adventurer creation (later will require a name from user)
      // location, will be random on the border of the map later
      String adventurerName = "Michel";
      int adventurerXStart;
      int adventurerYStart;
      do {
        adventurerXStart = random.nextInt(width); // generate an int between 0 and width-1
        if (adventurerXStart == 0 || adventurerXStart == width - 1) {
          adventurerYStart = random.nextInt(height);
        } else {
          boolean isTop = (Math.random() < 0.5f);
          if (isTop) {
            adventurerYStart = 0;
          } else {
            adventurerYStart = height - 1;
          }
        }
      } while (grid[adventurerYStart][adventurerXStart].getType() != Type.PATH);
      LOG.info("Position de l'aventurier : {},{}", adventurerXStart, adventurerYStart);
      int treasureX;
      int treasureY;
      int minXDistance = (int) (MIN_DISTANCE_FROM_ADVENTURER_PERCENTAGE * width);
      int minYDistance = (int) (MIN_DISTANCE_FROM_ADVENTURER_PERCENTAGE * height);
      // Set a random treasureX in allowed ranges
      List<int[]> possibleXRanges = calculatePossibleRanges(adventurerXStart, minXDistance, width);
      // Set a random treasureY in allowed ranges
      List<int[]> possibleYRanges = calculatePossibleRanges(adventurerYStart, minYDistance, height);
      do {
        // randomly set X
        treasureX = chooseRandomPosition(possibleXRanges, 'X');
        // randomly set Y
        treasureY = chooseRandomPosition(possibleYRanges, 'Y');
      } while (grid[treasureY][treasureX].getType() != Type.PATH);
      LOG.info("Treasure location : {},{}", treasureX, treasureY);
      treasure = new Treasure(treasureX, treasureY);

      adventurer = new Adventurer(adventurerName, adventurerXStart, adventurerYStart, 1, 1);

      // Vérification du chemin

      map = new GameMap(grid, width, height, adventurer, treasure);
    } while (!checkPath(map, adventurer, treasure));
    LOG.info("LoopCount = {}", loopCounter);
    LOG.info("The map: {}", map);
    return map;
  }

  private static boolean checkPath(GameMap gameMap, Adventurer adventurer, Treasure treasure) {
    // Implémentation de l'algorithme de pathfinding : BFS (Breadth-First Search) ou A* voire Dijkstra ? => BFS à l'air plus easy
    int width = gameMap.getMapWidth();
    int height = gameMap.getMapHeight();
    Tile[][] grid = gameMap.getGrid();

    boolean[][] visited = new boolean[height][width];
    Queue<int[]> queue = new LinkedList<>();
    // Ajouter la position de départ (l'aventurier) à la file
    queue.offer(new int[]{adventurer.getY(), adventurer.getX()});
    visited[adventurer.getY()][adventurer.getX()] = true;

    // Définir les directions possibles (haut, droite, bas, gauche)
    int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    while (!queue.isEmpty()) {
      int[] current = queue.poll();
      int y = current[0];
      int x = current[1];

      // Si nous avons atteint le trésor, un chemin existe
      if (y == treasure.getY() && x == treasure.getX()) {
        return true;
      }

      // Vérifier toutes les directions possibles
      for (int[] dir : directions) {
        int newY = y + dir[0];
        int newX = x + dir[1];

        // Vérifier si la nouvelle position est valide et non visitée
        if (newY >= 0 && newY < height && newX >= 0 && newX < width
            && !visited[newY][newX] && grid[newY][newX].getType() == Type.PATH) {
          queue.offer(new int[]{newY, newX});
          visited[newY][newX] = true;
        }
      }
    }

    // Si nous arrivons ici, aucun chemin n'a été trouvé
    return false;
  }

  // Fonction pour calculer les plages possibles
  private static List<int[]> calculatePossibleRanges(int adventurerPos, int minDistance, int maxBound) {
    List<int[]> possibleRanges = new ArrayList<>();

    // Plage à gauche/en haut de l'aventurier
    if (adventurerPos - minDistance > 0) {
      possibleRanges.add(new int[]{0, adventurerPos - minDistance});
    }

    // Plage à droite/en bas de l'aventurier
    if (adventurerPos + minDistance < maxBound) {
      possibleRanges.add(new int[]{adventurerPos + minDistance, maxBound - 1});
    }

    return possibleRanges;
  }

  // Fonction pour choisir une position aléatoire dans les plages données
  static int chooseRandomPosition(List<int[]> possibleRanges, char axis) throws NoValidRangeException {
    if (!possibleRanges.isEmpty()) {
      int[] selectedRange = possibleRanges.get(random.nextInt(possibleRanges.size()));
      // Generating a random number inside the selected range
      return random.nextInt(selectedRange[0], selectedRange[1] + 1);
    } else {
      String errorMessage = "No possible " + axis + " location for treasure !";
      LOG.error(errorMessage);
      throw new NoValidRangeException(errorMessage);
    }
  }

  private static void generateWoodAreas(Tile[][] grid, int width, int height) {
    int minNumberOfWoodAreas = (int) (width * height * MIN_WOOD_PERCENTAGE);
    int maxNumberOfWoodAreas = (int) (width * height * MAX_WOOD_PERCENTAGE);
    int numberOfWoodAreas = random.nextInt(minNumberOfWoodAreas, maxNumberOfWoodAreas);
    LOG.warn("numberOfWoodAreas : {}", numberOfWoodAreas);

    for (int i = 0; i <= numberOfWoodAreas; i++) {
      int woodX = random.nextInt(width);
      int woodY = random.nextInt(height);
      /*
      Mécanisme pour faire des bois plus grands
       */
//      int woodSize = random.nextInt(3) + 2; // Taille entre 2 et 4
//
//      for (int y = woodY; y < Math.min(woodY + woodSize, height); y++) {
//        for (int x = woodX; x < Math.min(woodX + woodSize, width); x++) {
//          grid[y][x].setType(Tile.Type.WOOD);
//        }
//      }
      grid[woodY][woodX].setType(Tile.Type.WOOD);
    }
  }

}
