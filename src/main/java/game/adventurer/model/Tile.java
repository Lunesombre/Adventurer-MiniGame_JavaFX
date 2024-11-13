package game.adventurer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Tile {

  public enum Type {PATH, WOOD}

  @NonNull
  @Setter
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
