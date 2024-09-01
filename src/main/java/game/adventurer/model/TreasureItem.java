package game.adventurer.model;

import java.util.Random;
import lombok.Getter;


public class TreasureItem {

  @Getter
  private final String name;
  private final Random random = new Random();
  protected static final String[] ITEM_NAMES = {
      "Chaussettes de Bob l'éponge", "Une truffe de l'an 2000", "le chat de la mère Michelle"
  };

  public TreasureItem() {
    this.name = getRandomItemName();
  }

  private String getRandomItemName() {
    int index = random.nextInt(ITEM_NAMES.length);
    return ITEM_NAMES[index];
  }

}
