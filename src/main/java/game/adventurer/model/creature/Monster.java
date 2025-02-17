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
      log.info("{} goes to nextValidNeighbor in {}, searchArea size : {}", this.name, nextValidNeighbor, searchArea.size());
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
          log.info("searchArea size : {} for {}", searchArea.size(), this.name);
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
            log.info("{} : searchTarget {} reached, searchArea size: {}", this.name, searchTarget, searchArea.size());
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
            (LinkedHashSet<Position>) shortestPath(this, new Position(this.getTileX(), this.getTileY()), lastSeenAdventurerPosition,
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
    Move move = associateDirectionToMove(getFacingDirection());
    int nextX = tileX + move.getDx();
    int nextY = tileY + move.getDy();

    // If the tile is not authorized, attempt to turn until a valid direction is found.
    int attempts = 0;
    while (!movementHandler.isAllowedTile(nextX, nextY, this)) {
      Direction initialDirection = getFacingDirection();
      ++attempts;
      switch (attempts) {
        case 1 -> {
          // Try turning in a random direction
          turnRandomly();
          move = associateDirectionToMove(getFacingDirection());
          nextX = tileX + move.getDx();
          nextY = tileY + move.getDy();
        }
        case 2 -> {
          turnAround(); // turns in the opposite direction to that previously tried
          move = associateDirectionToMove(getFacingDirection());
          nextX = tileX + move.getDx();
          nextY = tileY + move.getDy();
        }
        default -> {
          setFacingDirection(initialDirection.getOpposite()); // takes the opposite direction to the original one
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
    this.facingDirection.set(
        (random.nextInt(2) % 2 == 0) ? this.facingDirection.get().turnQuarterClockwise() : facingDirection.get().turnQuarterCounterClockwise());
  }

  private void turnAround() {
    this.facingDirection.set(facingDirection.get().getOpposite());
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
    int dx = pos.x() - this.getTileX();
    int dy = pos.y() - this.getTileY();
    Direction facingDirection;
    switch (dx) {
      case 0 -> {
        switch (dy) {
          case 1 -> facingDirection = Direction.SOUTH;
          case -1 -> facingDirection = Direction.NORTH;
          default -> throw new RuntimeException("Impossible movement, deltaV required: x =" + dx + ", y=" + dy + ".");
        }
      }
      case 1 -> facingDirection = Direction.EAST;
      case -1 -> facingDirection = Direction.WEST;
      default -> throw new RuntimeException("Impossible movement, deltaV required: x =" + dx + ", y=" + dy + ".");
    }

    this.previousTileX = tileX;
    this.previousTileY = tileY;
    this.tileX = pos.x();
    this.tileY = pos.y();
    this.setFacingDirection(facingDirection);
    lastMoveTime = System.currentTimeMillis();

  }

  @Override
  public String toString() {
    return "Monster{" +
        "name='" + name + '\'' +
        ", status=" + status +
        ", baseDamages=" + baseDamages +
        ", random=" + random +
        ", lastSeenAdventurerPosition=" + lastSeenAdventurerPosition +
        ", searchArea=" + searchArea +
        ", searchTarget=" + searchTarget +
        ", storedFOV=" + storedFOV +
        ", tileX=" + tileX +
        ", tileY=" + tileY +
        ", health=" + health +
        ", moveSpeed=" + moveSpeed +
        ", lastMoveTime=" + lastMoveTime +
        ", previousTileX=" + previousTileX +
        ", previousTileY=" + previousTileY +
        ", allowedTileTypes=" + allowedTileTypes +
        ", facingDirection=" + facingDirection +
        ", visibleTiles=" + visibleTiles +
        '}';
  }
}
