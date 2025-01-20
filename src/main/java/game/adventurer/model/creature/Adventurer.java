package game.adventurer.model.creature;

import lombok.Getter;

@Getter
public class Adventurer extends Creature {

  // TODO : overload the facing direction so it's not random for the Adventurer but opposing it's side of the Map

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
