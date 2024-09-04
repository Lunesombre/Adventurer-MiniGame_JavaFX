package game.adventurer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Tile {

  public enum Type {PATH, WOOD}

  private Type type;
  private int x;
  private int y;

  @Override
  public String toString() {
    return "Tile{" +
        "type=" + type +
        ", tileX=" + x +
        ", tileY=" + y +
        '}';
  }
}
