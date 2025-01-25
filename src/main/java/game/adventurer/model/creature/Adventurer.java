package game.adventurer.model.creature;

import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.Move;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Adventurer extends Creature {

  // TODO : overload the facing direction so it's not random for the Adventurer but opposing it's side of the Map

  public Adventurer(String name, int tileX, int tileY, int health, int moveSpeed) {
    super(name, tileX, tileY, health, moveSpeed);
    allowedTileTypes = Set.of(Type.PATH);
  }

  public Adventurer(String name, int tileX, int tileY) {
    super(name, tileX, tileY);
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
    if (lastMoveTime + 300 < currentTime) {
      this.previousTileX = this.tileX;
      this.previousTileY = this.tileY;
      this.tileX += move.getDx();
      this.tileY += move.getDy();
      lastMoveTime = currentTime;
      return true;
    }
    log.debug("Trying to move too early  :{}/300ms", currentTime - lastMoveTime);
    return false;
  }
}
