package game.adventurer.service;

import static game.adventurer.util.PathfindingUtil.hasPath;

import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.CreatureMovementHandler;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.Treasure;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Sniffer;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MapGenerator {

  private MapGenerator() {
  }

  public static final float MIN_WOOD_PERCENTAGE = 0.3f;
  public static final float MAX_WOOD_PERCENTAGE = 0.5f;
  public static final float MIN_DISTANCE_FROM_ADVENTURER_PERCENTAGE = 0.25f;
  public static final Logger LOG = LoggerFactory.getLogger(MapGenerator.class);
  private static final RandomGenerator random = RandomGenerator.getDefault();

  public static GameMap generateMap(int width, int height, String adventurerName) throws NoValidRangeException {
    GameMap map;
    Adventurer adventurer;
    Treasure treasure;
    CreatureMovementHandler movementHandler;
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

      // Generate randomly placed woods
      generateWoodAreas(grid, width, height);

      // Adventurer creation
      // location is random on the border of the map

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
      LOG.info("Position de l'aventurier : tileX={}, tileY={}", adventurerXStart, adventurerYStart);
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
      LOG.info("Treasure location : tileX={}, tileY={}", treasureX, treasureY);
      treasure = new Treasure(treasureX, treasureY);

      adventurer = new Adventurer(adventurerName, adventurerXStart, adventurerYStart);

      // Path verification
      map = new GameMap(grid, width, height, adventurer, treasure);
    } while (!checkPath(map, adventurer, treasure));
    LOG.info("LoopCount = {}", loopCounter);
    LOG.debug("The map: {}", map);

    // CreatureMovementHandler creation
    movementHandler = new CreatureMovementHandler(map);
    for (int i = 0; i < width; i++) {
      if (Tile.Type.PATH == map.getTileTypeAt(i, 0)) {
        // Adds a Monster on first tile that is a PATH tile, and has a path to the adventurer, then breaks
        GameMap finalMap = map;
        if (hasPath(adventurer.getTileX(), adventurer.getTileY(), i, 0, map.getMapWidth(), map.getMapHeight(),
            (x, y) -> finalMap.getTileTypeAt(x, y) == Type.PATH)) {
//          map.addMonster(new Mugger("Test Mugger " + (i + 1), i, 0, movementHandler));
          map.addMonster(new Sniffer("Test Sniffer " + (i + 1), i, 0, movementHandler));
          break;
        }

      }
    }

    return map;
  }

  private static boolean checkPath(GameMap gameMap, Adventurer adventurer, Treasure treasure) {
    // Pathfinding algorithm: BFS (Breadth-First Search)
    return hasPath(
        adventurer.getTileX(), adventurer.getTileY(),
        treasure.getTileX(), treasure.getTileY(),
        gameMap.getMapWidth(), gameMap.getMapHeight(),
        (x, y) -> gameMap.getTileTypeAt(x, y) == Type.PATH
    );
  }

  private static List<int[]> calculatePossibleRanges(int adventurerPos, int minDistance, int maxBound) {
    List<int[]> possibleRanges = new ArrayList<>();

    // Range to the left/top of the adventurer
    if (adventurerPos - minDistance > 0) {
      possibleRanges.add(new int[]{0, adventurerPos - minDistance});
    }

    // Range to the right/bottom of the adventurer
    if (adventurerPos + minDistance < maxBound) {
      possibleRanges.add(new int[]{adventurerPos + minDistance, maxBound - 1});
    }

    return possibleRanges;
  }

  // Function for selecting a random position within a given range
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
      grid[woodY][woodX].setType(Tile.Type.WOOD);
    }
  }

}
