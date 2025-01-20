package game.adventurer.model.creature;

public class Monster extends Creature {

  protected Monster(String name, int tileX, int tileY, int health, int moveSpeed) {
    super(name, tileX, tileY, health, moveSpeed);
  }

  protected Monster(String name, int tileX, int tileY) {
    super(name, tileX, tileY);
  }

}
