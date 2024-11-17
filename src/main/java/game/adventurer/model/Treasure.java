package game.adventurer.model;

import lombok.Getter;

@Getter
public class Treasure {

  private final TreasureItem item;
  private final int tileX; // tileX position on the game map
  private final int tileY; // tileY position on the game map

  public Treasure(int tileX, int y) {
    this.item = new TreasureItem();
    this.tileX = tileX;
    this.tileY = y;
  }

  @Override
  public String toString() {
    return "Treasure{" +
        "item=" + item.getNameKey() +
        ", tileX=" + tileX +
        ", tileY=" + tileY +
        '}';
  }
}
