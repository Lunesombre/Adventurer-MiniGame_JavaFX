package game.adventurer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
        "name='" + name + '\'' +
        ", tileX=" + tileX +
        ", tileY=" + tileY +
        ", health=" + health +
        ", moveSpeed=" + moveSpeed +
        '}';
  }
}
