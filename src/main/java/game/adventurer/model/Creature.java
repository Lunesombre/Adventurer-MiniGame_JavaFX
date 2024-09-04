package game.adventurer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Creature {

  protected String name;
  protected int tileX; // Position tileX on the game map
  protected int tileY; // Position tileY on the game map
  protected int health; // unused at first
  protected int moveSpeed; // unused at first, it shall be the number of tiles the creature can move per turn

  private static final int DEFAULT_HEALTH = 5;
  private static final int DEFAULT_MOVE_SPEED = 1;


  protected Creature(String name, int tileX, int tileY, int health, int moveSpeed) {
    this.name = name;
    this.tileX = tileX;
    this.tileY = tileY;
    this.health = health;
    this.moveSpeed = moveSpeed;
  }

  protected Creature(String name, int tileX, int tileY) {
    this(name, tileX, tileY, DEFAULT_HEALTH, DEFAULT_MOVE_SPEED);
  }

  public void move(int dx, int dy) {
    this.tileX += dx;
    this.tileY += dy;

  }
}
