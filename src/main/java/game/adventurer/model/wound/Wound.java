package game.adventurer.model.wound;


import game.adventurer.model.creature.Creature;
import game.adventurer.model.enums.WoundCause;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Wound {

  @Setter
  protected WoundCause cause;
  @Setter
  protected int healthCost;
  protected String woundMessageKey;

  protected Wound(WoundCause cause, int healthCost) {
    this.cause = cause;
    this.healthCost = healthCost;
  }

  protected Wound(WoundCause cause) {
    this.cause = cause;
  }

  protected boolean isFatal(Creature creature) {
    return creature.getHealth() - healthCost <= 0;
  }

  @Override
  public String toString() {
    return "Wound{" +
        "cause=" + cause +
        ", woundMessageKey='" + (woundMessageKey == null ? "null" : woundMessageKey) + '\'' +
        '}';
  }
}
