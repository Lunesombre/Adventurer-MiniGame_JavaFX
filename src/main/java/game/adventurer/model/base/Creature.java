package game.adventurer.model.base;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public abstract class Creature {

  protected String name;
  protected int tileX; // Position tileX on the game map
  protected int tileY; // Position tileY on the game map
  protected IntegerProperty health; // Number of life points of a Creature, observable
  protected int moveSpeed; // unused for now, but soon
  protected long lastMoveTime = 0;
  protected int previousTileX;
  protected int previousTileY;

  private static final int DEFAULT_HEALTH = 10;
  private static final int DEFAULT_MOVE_SPEED = 1;


  protected Creature(String name, int tileX, int tileY, int health, int moveSpeed) {
    this.name = name;
    this.tileX = tileX;
    this.tileY = tileY;
    this.health = new SimpleIntegerProperty(health);
    this.moveSpeed = moveSpeed;
  }

  protected Creature(String name, int tileX, int tileY) {
    this(name, tileX, tileY, DEFAULT_HEALTH, DEFAULT_MOVE_SPEED);
  }

  // Getter of the observable health property
  public IntegerProperty healthProperty() {
    return health;
  }

  // Regular health getter (returns an int)
  public int getHealth() {
    return health.get();
  }

  // Setter for health
  public void setHealth(int health) {
    this.health.set(health);
  }

  public boolean move(int dx, int dy) {
    long currentTime = System.currentTimeMillis();
    if (lastMoveTime + 300 < currentTime) {
      this.previousTileX = this.tileX;
      this.previousTileY = this.tileY;
      this.tileX += dx;
      this.tileY += dy;
      lastMoveTime = currentTime;
      return true;
    }
    log.debug("Trying to move too early  :{}/300ms", currentTime - lastMoveTime);
    return false;
  }
}
