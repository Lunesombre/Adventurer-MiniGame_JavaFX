package game.adventurer.model.base;


import game.adventurer.model.enums.WoundCause;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Wound {

  @Setter
  protected WoundCause cause;
  @Setter
  protected int healthCost;
  protected String woundMessage;

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
        ", woundMessage='" + (woundMessage == null ? "null" : woundMessage) + '\'' +
        '}';
  }
}
