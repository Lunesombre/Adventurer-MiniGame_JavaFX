package game.adventurer.model.creature;

import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.MonsterStatus;
import game.adventurer.util.PathfindingUtil;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sniffer extends Monster {

  public static final int BASE_DAMAGES = 1;
  public static final int INITIAL_COOLDOWN_TIME = 1000;

  public Sniffer(String name, int tileX, int tileY, int health, int moveSpeed, MovementHandler movementHandler) {
    super(name, tileX, tileY, health, moveSpeed, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;
  }

  public Sniffer(String name, int tileX, int tileY, MovementHandler movementHandler) {
    super(name, tileX, tileY, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;

  }

  @Override
  public boolean canMove() {
    long currentTime = System.currentTimeMillis();
    return switch (status) {
      case NEUTRAL, IN_SEARCH -> lastMoveTime + cooldownTime < currentTime;
      case ALERTED -> lastMoveTime + (4L * cooldownTime / 5) < currentTime; // initially 800 ms
    };
  }

  @Override
  public boolean wander() {
    if (status.equals(MonsterStatus.NEUTRAL) && canMove()) {
      previousTileX = tileX;
      previousTileY = tileY;
      previousPosition = currentPosition;
      randomMove();
      log.trace("{} position : y={}, x={}, direction:{}", getName(), getTileY(), getTileX(), getFacingDirection());
      return true;
    }
    return false;
  }


  @Override
  public void search(GameMap gameMap) {
    // TODO: change its behaviour in regard of map size and difficulty level
    Position currentPosition = new Position(tileX, tileY);

    if (shortestPath(this, currentPosition, searchTarget, gameMap).size() < 16) {
      try {
        pursue(gameMap);
      } catch (InvalidGameStateException e) {
        log.debug(e.getMessage());
        chill();
      }
    } else {
      chill();
    }
  }

  @Override
  public void pursue(GameMap gameMap) throws InvalidGameStateException {
    if (status.equals(MonsterStatus.ALERTED)) {
      super.pursue(gameMap);
    } else if (status.equals(MonsterStatus.IN_SEARCH) && canMove()) {
      if (searchTarget != null) {
        previousTileX = tileX;
        previousTileY = tileY;
        previousPosition = currentPosition;
        // uses PathfindingUtil to find the next tile to go get the Adventurer
        LinkedHashSet<Position> pathToAdventurer =
            (LinkedHashSet<Position>) PathfindingUtil.shortestPath(this, new Position(this.getTileX(), this.getTileY()), searchTarget,
                gameMap);
        // move to this tile
        if (!pathToAdventurer.isEmpty()) {
          moveTo(pathToAdventurer.getFirst());
          pathToAdventurer.removeFirst(); // removes the Position where the Monster arrives
        }
      } else {
        throw new InvalidGameStateException(this.getName() + " has no searchTarget when it should.");
      }
    }

  }

  @Override
  public int resetCooldownTime() {
    return INITIAL_COOLDOWN_TIME;
  }
}
