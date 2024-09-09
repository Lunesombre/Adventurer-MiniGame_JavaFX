package game.adventurer.model;

import game.adventurer.model.base.Creature;
import lombok.Getter;

@Getter
public class Adventurer extends Creature {

  public Adventurer(String name, int tileX, int tileY, int health, int moveSpeed) {
    super(name, tileX, tileY, health, moveSpeed);
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
}
