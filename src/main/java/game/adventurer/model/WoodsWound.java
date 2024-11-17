package game.adventurer.model;

import game.adventurer.model.base.Creature;
import game.adventurer.model.base.Wound;
import game.adventurer.model.enums.WoundCause;
import java.util.Random;

public class WoodsWound extends Wound {

  private static final Random RANDOM = new Random();

  private static final String[] DEATH_MESSAGES_KEYS = {
      "lethal.woods.1",
      "lethal.woods.2",
      "lethal.woods.3",
      "lethal.woods.4"
  };
  private static final String[] NON_LETHAL_MESSAGES_KEYS = {
      "wound.woods.1",
      "wound.woods.2",
      "wound.woods.3",
      "wound.woods.4"
  };


  public WoodsWound(WoundCause cause) {
    super(cause);
    this.healthCost = 1;
  }

  public void setWoundsMessage(Creature creature) {
    if (isFatal(creature)) {
      int index = RANDOM.nextInt(DEATH_MESSAGES_KEYS.length);
      this.woundMessageKey = DEATH_MESSAGES_KEYS[index];
    } else {
      int index = RANDOM.nextInt(NON_LETHAL_MESSAGES_KEYS.length);
      this.woundMessageKey = NON_LETHAL_MESSAGES_KEYS[index];
    }
  }
}
