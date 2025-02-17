package game.adventurer.model.creature;

import static game.adventurer.util.MiscUtil.getDistance;
import static game.adventurer.util.PathfindingUtil.getValidNeighbors;
import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.MonsterStatus;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Lurker extends Monster {

  @Getter
  private static final int BASE_DAMAGES = 10;
  private static final int INITIAL_COOLDOWN_TIME = 800;
  @Getter
  @Setter
  private int rushCounter = 0;
  private boolean isRushing = false;

  public Lurker(String name, int tileX, int tileY, int health, int moveSpeed, MovementHandler movementHandler) {
    super(name, tileX, tileY, health, moveSpeed, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = new HashSet<>();
    this.allowedTileTypes.add(Type.WOOD);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;
  }

  public Lurker(String name, int tileX, int tileY, MovementHandler movementHandler) {
    super(name, tileX, tileY, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = new HashSet<>();
    this.allowedTileTypes.add(Type.WOOD);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;

  }

  /**
   * Determines whether the monster can move based on its status and the time elapsed since its last move. Different statuses have different movement
   * cooldowns.
   *
   * @return true if the monster is allowed to move, false otherwise.
   */
  @Override
  public boolean canMove() {
    long currentTime = System.currentTimeMillis();
    return switch (status) {
      case NEUTRAL -> lastMoveTime + cooldownTime < currentTime; // initially 800 ms between moves
      case ALERTED -> {
        if (isRushing) {
          yield lastMoveTime + cooldownTime / 4 < currentTime; // 200 ms
        } else {
          yield lastMoveTime + 3L * cooldownTime / 4 < currentTime; // 600 ms
        }
      }
      case IN_SEARCH -> lastMoveTime + cooldownTime < currentTime;
    };
  }

  /**
   * Allows the Lurker to randomly move if it is in the NEUTRAL state and can move. The Lurker has a 60% chance to stay still and a 40% chance to
   * move.
   *
   * @return true if the Lurker moved, false otherwise.
   */
  @Override
  public boolean wander() {
    if (status.equals(MonsterStatus.NEUTRAL) && canMove()) {
      // Generates a random int between 0 and 4 included
      int chance = random.nextInt(5);

      if (chance < 3) {
        // The Lurker stays still 60% of the time.
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
   * Controls the Lurker's pursuit behavior when it is in the ALERTED state. The Lurker calculates the shortest path to the adventurer and moves
   * accordingly. If inside woods, it may try to remain hidden while approaching. If on a path, it may rush towards the adventurer.
   *
   * @param gameMap The game map on which the Lurker moves.
   */
  @Override
  public void pursue(GameMap gameMap) {
    if (status.equals(MonsterStatus.ALERTED)) {
      Adventurer adventurer = gameMap.getAdventurer();
      lastSeenAdventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
      Position lurkerPosition = new Position(this.tileX, this.tileY);
      LinkedHashSet<Position> path = (LinkedHashSet<Position>) shortestPath(this, lurkerPosition, lastSeenAdventurerPosition,
          gameMap);
      if (path.isEmpty()) {
        return; // no path to adventurer, return
      }
      if (path.size() > 4 && gameMap.getTileTypeAt(tileX, tileY) == Type.WOOD) {
        shadowStalk(gameMap); // tries to get closer to the Adventurer while staying hidden inside woods
      } else if (gameMap.getTileTypeAt(tileX, tileY) == Type.WOOD && canMove()) {
        moveTo(path.getFirst()); // moves out of the woods
      } else if (gameMap.getTileTypeAt(tileX, tileY) == Type.PATH && rushCounter < 3) {
        // rushes to the Adventurer
        isRushing = true;
        if (canMove()) {
          moveTo(path.getFirst());
          rushCounter++;
        }
      } else if (gameMap.getTileTypeAt(tileX, tileY) == Type.PATH) {
        // has already rushed to the Adventurer, follows him, a bit slower though
        isRushing = false;
        if (canMove()) {
          moveTo(path.getFirst());
        }
      }
    }
  }

  /**
   * Allows the Lurker to stalk its target while remaining hidden within the woods. It moves to a nearby position in the woods that is closest to the
   * adventurer.
   *
   * @param gameMap The game map on which the Lurker moves.
   */
  private void shadowStalk(GameMap gameMap) {
    if (status.equals(MonsterStatus.ALERTED) && canMove()) {
      Position currentPosition = new Position(this.getTileX(), this.getTileY());
      Set<Position> possiblePositionInWoods = getValidNeighbors(currentPosition, gameMap, Set.of(Type.WOOD), false);
      Position nextPosition = null;
      int manhattanDistance = Integer.MAX_VALUE;
      for (Position position : possiblePositionInWoods) {
        if (nextPosition == null
            || getDistance(nextPosition, new Position(gameMap.getAdventurer().getTileX(), gameMap.getAdventurer().getTileY())) < manhattanDistance) {
          nextPosition = position;
          manhattanDistance = getDistance(nextPosition, new Position(gameMap.getAdventurer().getTileX(), gameMap.getAdventurer().getTileY()));
        }
      }
      if (nextPosition != null) {
        moveTo(nextPosition);
      }
    }
  }

  /**
   * Resets the Lurker to a neutral state, resetting the rushing behavior.
   */
  @Override
  public void chill() {
    super.chill();
    rushCounter = 0;
  }

  @Override
  public int resetCooldownTime() {
    return INITIAL_COOLDOWN_TIME;
  }
}
