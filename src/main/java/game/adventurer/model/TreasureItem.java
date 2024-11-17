package game.adventurer.model;

import java.util.Random;
import lombok.Getter;


public class TreasureItem {

  @Getter
  private final String nameKey;
  @Getter
  private final String glowColor;
  private final Random random = new Random();
  protected String[] itemNamesKeys = {
      "treasure.socks",
      "treasure.oldTruffle",
      "treasure.cat",
      "treasure.alcoholismRing",
      "treasure.elephantEgg",
      "treasure.shadow",
      "treasure.maroon"
  };

  protected static final String[] ITEM_COLORS = {
      "gold",
      "#ffae00",
      "#66ff00",
      "#00C4FF"
  };

  public TreasureItem() {
    this.nameKey = getRandomItemNameKey();
    this.glowColor = getRandomItemColor();
  }

  private String getRandomItemNameKey() {
    int index = random.nextInt(itemNamesKeys.length);
    return itemNamesKeys[index];
  }

  private String getRandomItemColor() {
    int index = random.nextInt(ITEM_COLORS.length);
    return ITEM_COLORS[index];
  }
}
