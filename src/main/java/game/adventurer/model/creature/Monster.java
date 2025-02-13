package game.adventurer.model.creature;

import static game.adventurer.util.PathfindingUtil.getValidNeighbor;
import static game.adventurer.util.PathfindingUtil.shortestDistance;
import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.enums.Direction;
import game.adventurer.model.enums.MonsterStatus;
import game.adventurer.model.enums.Move;
import game.adventurer.util.PathfindingUtil;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class Monster extends Creature {

  @Setter
  protected MonsterStatus status = MonsterStatus.NEUTRAL;

  protected int baseDamages;

  protected Random random = new Random();

  @Setter
  protected Position lastSeenAdventurerPosition = null;

  @Setter
  protected Set<Position> searchArea = new LinkedHashSet<>();
  @Setter
  protected Position searchTarget;
  @Setter
  protected Set<Position> storedFOV = new HashSet<>();


  protected Monster(String name, int tileX, int tileY, int health, int moveSpeed, MovementHandler movementHandler) {
    super(name, tileX, tileY, health, moveSpeed);
    this.movementHandler = movementHandler;
  }

  protected Monster(String name, int tileX, int tileY, MovementHandler movementHandler) {
    super(name, tileX, tileY);
    this.movementHandler = movementHandler;
  }

  public abstract boolean canMove();

  public abstract boolean wander();

  /**
   * Executes the search behavior for the monster on the game map.
   * <p>
   * If the search area is empty, the monster will chill. Otherwise, it will attempt to move to a valid neighboring position. If no valid neighbor is
   * found, it will search for the closest remaining tile in the search area and set it as the search target. If a search target is already set, the
   * monster will move towards it using the shortest path.
   *
   * @param gameMap The game map containing tile information and boundaries.
   */
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

  public void pursue(GameMap gameMap) throws InvalidGameStateException {
    if (status.equals(MonsterStatus.ALERTED) && canMove()) {
      if (lastSeenAdventurerPosition != null) {
        previousTileX = tileX;
        previousTileY = tileY;
        // uses PathfindingUtil to find the next tile to go get the Adventurer
        LinkedHashSet<Position> pathToAdventurer =
            (LinkedHashSet<Position>) PathfindingUtil.shortestPath(this, new Position(this.getTileX(), this.getTileY()), lastSeenAdventurerPosition,
                gameMap);
        // move to this tile
        if (!pathToAdventurer.isEmpty()) {
          moveTo(pathToAdventurer.getFirst());
          pathToAdventurer.removeFirst(); // removes the Position where the Monster arrives
        }
      } else {
        throw new InvalidGameStateException(this.getName() + " has no lastSeenAdventurerPosition when it should.");
      }
    }

  }


  protected void randomMove() {
    int chance = random.nextInt(100);
    if (chance < 60) {
      // move forward
      moveForward();
    } else if (chance < 95) {
      // turn randomly and move forward
      turnRandomly();
      moveForward();
    } else {
      // turn around
      turnAround();
    }
  }

  private void moveForward() {
    lastMoveTime = System.currentTimeMillis();
    Move move = associateDirectionToMove(facingDirection);
    int nextX = tileX + move.getDx();
    int nextY = tileY + move.getDy();

    // If the tile is not authorized, attempt to turn until a valid direction is found.
    int attempts = 0;
    while (!movementHandler.isAllowedTile(nextX, nextY, this)) {
      Direction initialDirection = facingDirection;
      ++attempts;
      switch (attempts) {
        case 1 -> {
          // Try turning in a random direction
          turnRandomly();
          move = associateDirectionToMove(facingDirection);
          nextX = tileX + move.getDx();
          nextY = tileY + move.getDy();
        }
        case 2 -> {
          turnAround(); // turns in the opposite direction to that previously tried
          move = associateDirectionToMove(facingDirection);
          nextX = tileX + move.getDx();
          nextY = tileY + move.getDy();
        }
        default -> {
          facingDirection = initialDirection.getOpposite(); // takes the opposite direction to the original one
          return; // exits method
        }
      }
    }
    // If a valid direction has been found, the Monster moves
    this.tileX += move.getDx();
    this.tileY += move.getDy();
  }


  private void turnRandomly() {
    // Random left or right turns
    this.facingDirection = (random.nextInt(2) % 2 == 0) ? facingDirection.turnQuarterClockwise() : facingDirection.turnQuarterCounterClockwise();
  }

  private void turnAround() {
    this.facingDirection = facingDirection.getOpposite();
  }

  private Move associateDirectionToMove(Direction direction) {
    return switch (direction) {
      case EAST -> Move.RIGHT;
      case WEST -> Move.LEFT;
      case NORTH -> Move.UP;
      case SOUTH -> Move.DOWN;
    };
  }

  public void chill() {
    this.setStatus(MonsterStatus.NEUTRAL);
    this.lastSeenAdventurerPosition = null;
    this.storedFOV.clear();
    this.searchTarget = null;
    log.info("{} is now chilling", this.name);
  }

  public void moveTo(Position pos) {
    if ((Math.abs(pos.x() - this.tileX) == 1 && Math.abs(pos.y() - this.tileY) == 0) ||
        (Math.abs(pos.x() - this.tileX) == 0 && Math.abs(pos.y() - this.tileY) == 1)) {
      this.previousTileX = tileX;
      this.previousTileY = tileY;
      this.tileX = pos.x();
      this.tileY = pos.y();
      lastMoveTime = System.currentTimeMillis();
    } else {
      log.error("{} cannot jump from {},{} to {},{}, something isn't working properly", this.name, this.tileX, this.tileY, pos.x(), pos.y());
    }
  }
}
