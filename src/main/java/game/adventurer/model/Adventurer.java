package game.adventurer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Adventurer {

  private String name;
  private int x; // Position x on the game map
  private int y; // Position y on the game map
  private int health; // unused at first
  private int moveSpeed; // unused at first, it shall be the number of tiles the player can move per turn

  @Override
  public String toString() {
    return "Adventurer{" +
        "name='" + name + '\'' +
        ", x=" + x +
        ", y=" + y +
        ", health=" + health +
        ", moveSpeed=" + moveSpeed +
        '}';
  }
}
