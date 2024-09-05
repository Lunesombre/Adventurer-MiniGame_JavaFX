package game.adventurer.model;

import java.util.Random;
import lombok.Getter;


public class TreasureItem {

  @Getter
  private final String name;
  @Getter
  private final String glowColor;
  private final Random random = new Random();
  protected static final String[] ITEM_NAMES = {
      "les Chaussettes de Bob l'éponge",
      "une Truffe de l'an 2000",
      "le Chat de la mère Michelle",
      "l'Anneau magique qui rend alcoolique",
      "un Oeuf d'éléphant (rare ++)"
  };

  protected static final String[] ITEM_COLORS = {
      "gold",
      "#ffae00",
      "#66ff00",
      "#00C4FF"
  };

  public TreasureItem() {
    this.name = getRandomItemName();
    this.glowColor = getRandomItemColor();
  }

  private String getRandomItemName() {
    int index = random.nextInt(ITEM_NAMES.length);
    return ITEM_NAMES[index];
  }

  private String getRandomItemColor() {
    int index = random.nextInt(ITEM_COLORS.length);
    return ITEM_COLORS[index];
  }

}
