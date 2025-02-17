package game.adventurer.model.wound;


import game.adventurer.model.creature.Creature;
import game.adventurer.model.enums.WoundCause;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Wound {

  protected static final Random RANDOM = new Random();

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

  public abstract void setWoundsMessage(Creature creature);

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
