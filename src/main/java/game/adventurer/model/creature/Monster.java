package game.adventurer.model.creature;

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

  protected abstract boolean canMove();

  protected abstract boolean wander();

  protected abstract void pursue(GameMap gameMap) throws InvalidGameStateException;

  protected abstract void search(GameMap gameMap);


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
