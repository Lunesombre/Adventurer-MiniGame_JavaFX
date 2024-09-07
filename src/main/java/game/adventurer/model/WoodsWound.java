package game.adventurer.model;

import game.adventurer.model.base.Creature;
import game.adventurer.model.base.Wound;
import game.adventurer.model.enums.WoundCause;
import java.util.Random;

public class WoodsWound extends Wound {

  private static final Random RANDOM = new Random();

  private static final String[] DEATH_MESSAGES = {
      "A force de lacérations par les ronces et autres, vous vous êtes vidé de votre sang.",
      "C'était la plante empoisonnée de trop ! Vous avez succombé.",
      "Affaibli par la fatigue et vos nombreuses blessures, vous êtes tombé... en plein sur une branche brisée, sur laquelle vous vous êtes empalé. Une bien vilaine façon de partir. ",
      "Cette racine traître vous a fait trébucher et votre tête a heurté un rocher... Vous sombrez dans un sommeil dont on ne revient jamais."
  };
  private static final String[] NON_LETHAL_MESSAGES = {
      "Aïe ! Ces ronces acérées vous ont salement griffé.",
      "Vous n'auriez pas dû essayer de traverser ces buissons épineux... Vous saignez.",
      "La peau vous brûle depuis que vous avez touché ces plantes qui vous barrent la route.",
      "Les branches basses vous fouettent violemment. On dirait que ces arbres... bougent ?"
  };


  public WoodsWound(WoundCause cause) {
    super(cause);
    this.healthCost = 1;
  }

  public void setWoundsMessage(Creature creature) {
    if (isFatal(creature)) {
      int index = RANDOM.nextInt(DEATH_MESSAGES.length);
      this.woundMessage = DEATH_MESSAGES[index];
    } else {
      int index = RANDOM.nextInt(NON_LETHAL_MESSAGES.length);
      this.woundMessage = NON_LETHAL_MESSAGES[index];
    }
  }
}
