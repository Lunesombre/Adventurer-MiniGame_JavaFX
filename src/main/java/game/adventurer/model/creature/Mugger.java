package game.adventurer.model.creature;

import static game.adventurer.util.PathfindingUtil.getValidNeighbor;
import static game.adventurer.util.PathfindingUtil.shortestDistance;
import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.MonsterStatus;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mugger extends Monster {

  public static final int BASE_DAMAGES = 3;

  public Mugger(String name, int tileX, int tileY, int health, MovementHandler movementHandler) {
    super(name, tileX, tileY, health, 1, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
  }

  public Mugger(String name, int tileX, int tileY, MovementHandler movementHandler) {
    super(name, tileX, tileY, movementHandler);
    this.moveSpeed = 1;
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
  }

  @Override
  public boolean canMove() {
    long currentTime = System.currentTimeMillis();
    return switch (status) {
      case NEUTRAL -> lastMoveTime + 500 < currentTime; // 500 ms between moves
      case ALERTED -> lastMoveTime + 600 < currentTime;
      case IN_SEARCH -> lastMoveTime + 800 < currentTime;
    };
  }

  @Override
  public boolean wander() {
    if (status.equals(MonsterStatus.NEUTRAL) && canMove()) {
      // Generates a random int between 0 and 99
      int chance = random.nextInt(100);

      if (chance < 60) {
        // The Mugger lingers on 60% of the time.
        log.trace("{} stays still.", getName());
        return false;
      }
      previousTileX = tileX;
      previousTileY = tileY;
      randomMove();
      log.trace("{} position : y={}, x={}, direction:{}", getName(), getTileY(), getTileX(), getFacingDirection());
      return true;
    }
    return false;
  }

  /**
   * Executes the search behavior for the monster on the game map.
   * <p>
   * If the search area is empty, the monster will chill. Otherwise, it will attempt to move to a valid neighboring position. If no valid neighbor is
   * found, it will search for the closest remaining tile in the search area and set it as the search target. If a search target is already set, the
   * monster will move towards it using the shortest path.
   *
   * @param gameMap The game map containing tile information and boundaries.
   */
  @Override
  public void search(GameMap gameMap) {
    if (searchArea.isEmpty()) {
      chill();
      return;
    }
    Position currentPosition = new Position(this.tileX, this.tileY);
    Position nextValidNeighbor = getValidNeighbor(this, gameMap, true);
    if (nextValidNeighbor != null) {
      searchArea.remove(nextValidNeighbor);
      log.info("goes to nextValidNeigbor in {}, searchArea size : {}", nextValidNeighbor, searchArea.size());
      moveTo(nextValidNeighbor);
    } else {
      if (searchTarget == null) {
        // find the closest remaining Tile in the searchArea
        int shortestFoundDistance = Integer.MAX_VALUE;
        Position nextPosition = currentPosition;
        for (Position position : searchArea) {
          int newDistance = shortestDistance(this, currentPosition, position, gameMap);
          if (newDistance > 0 && newDistance < shortestFoundDistance) {
            shortestFoundDistance = newDistance;
            nextPosition = position;

          }
        }
        if (!nextPosition.equals(currentPosition)) {
          searchArea.remove(nextPosition);
          log.info("searchArea size : {}", searchArea.size());
          this.setSearchTarget(nextPosition);
        }
      } else {
        if (searchArea.isEmpty()) {
          chill();
          return;
        }
        LinkedHashSet<Position> path = (LinkedHashSet<Position>) shortestPath(this, currentPosition, searchTarget, gameMap);
        if (null != path && !path.isEmpty()) {
          moveTo(path.getFirst());
          searchArea.remove(path.getFirst());

          if (new Position(this.tileX, this.tileY).equals(searchTarget)) {
            log.info("searchTarget {} reached, searchArea size: {}", searchTarget, searchArea.size());
            setSearchTarget(null);
          }
        }
      }
    }
  }

}
