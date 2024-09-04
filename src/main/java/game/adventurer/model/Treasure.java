package game.adventurer.model;

import lombok.Getter;

@Getter
public class Treasure {

  private final TreasureItem item;
  private final int x; // Position tileX on the game map
  private final int y; // Position tileY on the game map

  public Treasure(int x, int y) {
    this.item = new TreasureItem();
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return "Treasure{" +
        "item=" + item.getName() +
        ", tileX=" + x +
        ", tileY=" + y +
        '}';
  }
}
