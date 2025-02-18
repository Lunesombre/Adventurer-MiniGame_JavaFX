package game.adventurer.service;

import game.adventurer.model.CreatureMovementHandler;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.MovementHandler;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.MapSize;
import game.adventurer.util.PathfindingUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for placing monsters on the game map.
 */
@Slf4j
public class MonsterPlacerService {

  private final GameMap map;
  private final int mapWidth;
  private final int mapHeight;
  private final int quadrantsCount;
  private final Random random = new Random();
  /**
   * By design, quadrants contain 25 Tile (or Position).
   * <p>
   * This allows more control and works perfectly fine on a square map where sides' length is a multiple of 5. It might need some rework if this were
   * to change.
   */
  private static final int POSITIONS_PER_QUADRANT = 25;

  public MonsterPlacerService(GameMap map, MapSize mapSize) {
    this.map = map;
    this.mapHeight = map.getMapHeight();
    this.mapWidth = map.getMapWidth();
    this.quadrantsCount = (int) (Math.pow(mapSize.getSize(), 2) / POSITIONS_PER_QUADRANT); // Dynamic quadrant count based on map size
  }

  /**
   * Places monsters on the game map according to the specified quota for each monster type.
   * <p>
   * Monsters cannot be placed on an already occupied tile, <br/>they will be placed semi-randomly <br/>(randomly amongst the least populated
   * quadrants of the map, <br/> and according to rules depending on the Monster's subclass).
   *
   * @param monsterQuotaMap A map containing the monster class and the number of monsters to place.
   */
  public void placeMonsters(Map<Class<? extends Monster>, Integer> monsterQuotaMap) {

    List<Integer> availableQuadrants = getAvailableQuadrants();

    // Map to track occupied positions per quadrant
    Map<Integer, Set<Position>> occupiedPositions = new HashMap<>();

    for (int quadrant : availableQuadrants) {
      occupiedPositions.put(quadrant, new HashSet<>());
    }

    monsterQuotaMap.forEach((monsterClass, count) -> {

      CreatureMovementHandler movementHandler = new CreatureMovementHandler(map);

      for (int i = 0; i < count; i++) {

        Position position = null;
        int selectedQuadrant;

        while (position == null && !occupiedPositions.isEmpty()) {
          // Find quadrant with the least Monsters, or if several have the same population, one of them is randomly chosen.
          selectedQuadrant = findLeastPopulatedQuadrant(occupiedPositions);
          do {
            // Find a valid and unoccupied Position for this monster
            position = getValidPositionInQuadrant(selectedQuadrant, monsterClass);
          } while (occupiedPositions.get(selectedQuadrant).contains(position));

          if (position == null) {
            // no valid and unoccupied Position was found: remove this quadrant from the map for next iteration
            occupiedPositions.remove(selectedQuadrant);
          } else {
            // a new valid unoccupied Position was found, add it to the occupied ones
            occupiedPositions.get(selectedQuadrant).add(position);
          }
        }

        if (position == null) {
          log.warn("No valid position found for monster {} after checking all quadrants", monsterClass.getSimpleName());
          continue; // Skip this monster if no valid position is found
        }

        // Uses reflection to build the monster with the correct constructor.
        try {
          Constructor<? extends Monster> constructor = monsterClass.getDeclaredConstructor(String.class, int.class, int.class,
              MovementHandler.class);
          Monster monster = constructor.newInstance(monsterClass.getSimpleName() + " " + (i + 1), position.x(), position.y(), movementHandler);
          map.addMonster(monster);
          map.occupyTile(position);

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    });


  }

  /**
   * Returns the quadrant number for the specified coordinates.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The quadrant number.
   */
  private int getQuadrant(int x, int y) {
    int quadrantsPerRow = (int) Math.sqrt(quadrantsCount);
    int quadrantWidth = mapWidth / quadrantsPerRow;
    int quadrantHeight = mapHeight / quadrantsPerRow;
    int quadrantX = x / quadrantWidth;
    int quadrantY = y / quadrantHeight;
    return quadrantY * quadrantsPerRow + quadrantX + 1;
  }

  /**
   * Returns a list of available quadrants, excluding the quadrant where the adventurer is located.
   *
   * @return A list of available quadrant numbers.
   */
  private List<Integer> getAvailableQuadrants() {
    int adventurerX = map.getAdventurer().getTileX();
    int adventurerY = map.getAdventurer().getTileY();

    int adventurerQuadrant = getQuadrant(adventurerX, adventurerY);
    List<Integer> availableQuadrants = new ArrayList<>();
    for (int i = 1; i <= quadrantsCount; i++) {
      if (i != adventurerQuadrant) {
        availableQuadrants.add(i);
      }
    }
    return availableQuadrants;
  }

  /**
   * Finds the least populated quadrant from the given map of occupied positions.
   *
   * @param occupiedPositionsPerQuadrant A map of occupied positions per quadrant.
   * @return The number of the least populated quadrant, or a random quadrant's number amongst the less populated ones.
   */
  private int findLeastPopulatedQuadrant(Map<Integer, Set<Position>> occupiedPositionsPerQuadrant) {
    int minCount = Integer.MAX_VALUE;
    List<Integer> leastPopulatedQuadrants = new ArrayList<>();

    for (Map.Entry<Integer, Set<Position>> entry : occupiedPositionsPerQuadrant.entrySet()) {
      int quadrant = entry.getKey();
      int count = entry.getValue().size();

      if (count < minCount) {
        minCount = count;
        leastPopulatedQuadrants.clear();
        leastPopulatedQuadrants.add(quadrant);
      } else if (count == minCount) {
        leastPopulatedQuadrants.add(quadrant);
      }
    }

    // Returns a random quadrants amongst the less populated ones
    return leastPopulatedQuadrants.get(random.nextInt(leastPopulatedQuadrants.size()));
  }

  /**
   * Returns a valid position within the specified quadrant for the given monster class.
   *
   * @param quadrant     The quadrant number.
   * @param monsterClass The class of the monster.
   * @return A valid position within the quadrant, or {@code null} if none are valid.
   */
  private Position getValidPositionInQuadrant(int quadrant, Class<? extends Monster> monsterClass) {
    int quadrantsPerRow = (int) Math.sqrt(quadrantsCount);
    int quadrantWidth = mapWidth / quadrantsPerRow;
    int quadrantHeight = mapHeight / quadrantsPerRow;
    int quadrantX = (quadrant - 1) % quadrantsPerRow;
    int quadrantY = (quadrant - 1) / quadrantsPerRow;

    Set<Position> invalidPositions = new HashSet<>();

    while (invalidPositions.size() < POSITIONS_PER_QUADRANT) {
      int x = random.nextInt(quadrantX * quadrantWidth, (quadrantX + 1) * quadrantWidth);
      int y = random.nextInt(quadrantY * quadrantHeight, (quadrantY + 1) * quadrantHeight);

      Tile.Type type = map.getTileTypeAt(x, y);
      if (isValidTileForMonster(type, monsterClass, x, y)) {
        return new Position(x, y);
      }
      invalidPositions.add(new Position(x, y));
    }
    return null;
  }

  /**
   * Checks if the specified tile type is valid for the given monster class at the specified coordinates.
   *
   * @param type         The tile type.
   * @param monsterClass The class of the monster, extending the abstract {@code Monster} class.
   * @param x            The x-coordinate.
   * @param y            The y-coordinate.
   * @return True if the tile is valid for the monster, false otherwise.
   */
  private boolean isValidTileForMonster(Tile.Type type, Class<? extends Monster> monsterClass, int x, int y) {
    if (monsterClass == Lurker.class) {
      return type == Tile.Type.WOOD;
    } else if (monsterClass == Sniffer.class || monsterClass == Mugger.class) {
      return type == Tile.Type.PATH && PathfindingUtil.hasPath(x, y, map.getAdventurer().getTileX(), map.getAdventurer().getTileY(), mapWidth,
          mapHeight, (currentX, currentY) -> map.getTileTypeAt(currentX, currentY) == Type.PATH);
    } else {
      log.warn("Unexpected Monster class : {}", monsterClass.getSimpleName());
    }
    return false;
  }

}
