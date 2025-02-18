package game.adventurer.model.creature;

import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.MonsterStatus;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mugger extends Monster {

  public static final int BASE_DAMAGES = 3;
  public static final int INITIAL_COOLDOWN_TIME = 800;

  public Mugger(String name, int tileX, int tileY, int health, MovementHandler movementHandler) {
    super(name, tileX, tileY, health, 1, movementHandler);
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;
  }

  public Mugger(String name, int tileX, int tileY, MovementHandler movementHandler) {
    super(name, tileX, tileY, movementHandler);
    this.moveSpeed = 1;
    this.baseDamages = BASE_DAMAGES;
    this.allowedTileTypes = Set.of(Type.PATH);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;

  }

  @Override
  public boolean canMove() {
    long currentTime = System.currentTimeMillis();
    return switch (status) {
      case NEUTRAL -> lastMoveTime + cooldownTime < currentTime; // initially 800 ms between moves
      case ALERTED -> lastMoveTime + (3L * cooldownTime / 4) < currentTime; // initially 600 ms
      case IN_SEARCH -> lastMoveTime + cooldownTime < currentTime;
    };
  }

  @Override
  public boolean wander() {
    if (status.equals(MonsterStatus.NEUTRAL) && canMove()) {
      // Generates a random int between 0 and 9
      int chance = random.nextInt(10);

      if (chance < 5) {
        // The Mugger lingers on 50% of the time.
        log.trace("{} stays still.", getName());
        return false;
      }
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
  public int resetCooldownTime() {
    return INITIAL_COOLDOWN_TIME;
  }


}
