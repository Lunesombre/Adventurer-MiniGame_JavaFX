package game.adventurer.service;

import static game.adventurer.model.enums.DifficultyLevel.EASY;
import static game.adventurer.model.enums.DifficultyLevel.HARD;
import static game.adventurer.model.enums.DifficultyLevel.NORMAL;
import static game.adventurer.model.enums.MapSize.LARGE;
import static game.adventurer.model.enums.MapSize.MEDIUM;
import static game.adventurer.model.enums.MapSize.SMALL;
import static game.adventurer.util.PathfindingUtil.hasPath;

import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.Treasure;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MapSize;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public static GameMap generateMap(String adventurerName, MapSize mapSize, DifficultyLevel difficulty) throws NoValidRangeException {
    int width = mapSize.getSize();
    int height = mapSize.getSize();
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

    addMonsters(map, mapSize, difficulty);

    return map;
  }

  private static void addMonsters(GameMap map, MapSize size, DifficultyLevel difficultyLevel) {
    record ChosenSettings(MapSize size, DifficultyLevel difficultyLevel) {

    }
    int lurkersCount = 0;
    int muggersCount = 0;
    int sniffersCount = 0;
    ChosenSettings chosenSettings = new ChosenSettings(size, difficultyLevel);

    // no record destructuration on enums in a switch in java 21, yet.
    switch (chosenSettings) {
      case ChosenSettings s when s.equals(new ChosenSettings(SMALL, EASY)) -> muggersCount = 2;
      case ChosenSettings s when s.equals(new ChosenSettings(SMALL, NORMAL)) -> {
        muggersCount = 1;
        sniffersCount = 1;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(SMALL, HARD)) -> {
        muggersCount = 1;
        sniffersCount = 1;
        lurkersCount = 1;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(MEDIUM, EASY)) -> {
        muggersCount = 3;
        sniffersCount = 1;
        lurkersCount = 1;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(MEDIUM, NORMAL)) -> {
        muggersCount = 3;
        sniffersCount = 2;
        lurkersCount = 1;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(MEDIUM, HARD)) -> {
        muggersCount = 4;
        sniffersCount = 2;
        lurkersCount = 2;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(LARGE, EASY)) -> {
        muggersCount = 6;
        sniffersCount = 3;
        lurkersCount = 3;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(LARGE, NORMAL)) -> {
        muggersCount = 8;
        sniffersCount = 5;
        lurkersCount = 3;
      }
      case ChosenSettings s when s.equals(new ChosenSettings(LARGE, HARD)) -> {
        muggersCount = 10;
        sniffersCount = 6;
        lurkersCount = 4;
      }
      case null, default -> throw new IllegalStateException("Unexpected settings: " + chosenSettings);
    }
    Map<Class<? extends Monster>, Integer> monsterQuotaMap = new HashMap<>();
    if (muggersCount > 0) {
      monsterQuotaMap.put(Mugger.class, muggersCount);
    }
    if (sniffersCount > 0) {
      monsterQuotaMap.put(Sniffer.class, sniffersCount);
    }
    if (lurkersCount > 0) {
      monsterQuotaMap.put(Lurker.class, lurkersCount);
    }

    // adding monsters to the map.
    // the monsters should be placed on tiles that they have the right to be on,
    // have a path to the Adventurer (except Lurkers ?) and far enough from the Adventurer
    MonsterPlacerService placer = new MonsterPlacerService(map, size);
    placer.placeMonsters(monsterQuotaMap);
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
