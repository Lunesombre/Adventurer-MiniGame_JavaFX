package game.adventurer.model.creature;

import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.Move;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Adventurer extends Creature {

  // TODO : overload the facing direction so it's not random for the Adventurer but opposing it's side of the Map
  private static final int INITIAL_COOLDOWN_TIME = 300;

  public Adventurer(String name, int tileX, int tileY, int health, int moveSpeed) {
    super(name, tileX, tileY, health, moveSpeed);
    allowedTileTypes = Set.of(Type.PATH);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;
  }

  public Adventurer(String name, int tileX, int tileY) {
    super(name, tileX, tileY);
    this.cooldownTime = INITIAL_COOLDOWN_TIME;
  }

  @Override
  public int resetCooldownTime() {
    return INITIAL_COOLDOWN_TIME;
  }

  @Override
  public String toString() {
    return "Adventurer{" +
        "cause='" + name + '\'' +
        ", tileX=" + tileX +
        ", tileY=" + tileY +
        ", health=" + health +
        ", moveSpeed=" + moveSpeed +
        '}';
  }

  public boolean move(Move move) {
    long currentTime = System.currentTimeMillis();
    if (lastMoveTime + cooldownTime < currentTime) {
      int newX = currentPosition.x() + move.getDx();
      int newY = currentPosition.y() + move.getDy();
      this.previousTileX = this.tileX;
      this.previousTileY = this.tileY;
      this.tileX += move.getDx();
      this.tileY += move.getDy();
      this.previousPosition = currentPosition;
      this.currentPosition = new Position(newX, newY);
      lastMoveTime = currentTime;
      return true;
    }
    log.debug("Trying to move too early  :{}/{}ms", currentTime - lastMoveTime, cooldownTime);
    return false;
  }
}
