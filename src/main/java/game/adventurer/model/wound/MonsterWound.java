package game.adventurer.model.wound;

import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.WoundCause;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonsterWound extends Wound {

  private final Monster monster;

  private static final String[] LURKER_LETHAL_MK = {"wound.lurker.lethal.1", "wound.lurker.lethal.2"};
  private static final String[] LURKER_NON_LETHAL_MK = {"wound.lurker.non_lethal.1", "wound.lurker.non_lethal.2"};
  private static final String[] MUGGER_LETHAL_MK = {"wound.mugger.lethal.1", "wound.mugger.lethal.2"};
  private static final String[] MUGGER_NON_LETHAL_MK = {"wound.mugger.non_lethal.1", "wound.mugger.non_lethal.2", "wound.mugger.non_lethal.3"};
  private static final String[] SNIFFER_LETHAL_MK = {"wound.sniffer.lethal"};
  private static final String[] SNIFER_NON_LETHAL_MK = {"wound.sniffer.non_lethal"};

  public MonsterWound(WoundCause cause, Monster monster) {
    super(cause);
    this.monster = monster;
    this.healthCost = monster.getBaseDamages();
  }

  @Override
  public void setWoundsMessage(Creature creature) {
    switch (monster) {
      case Sniffer ignored -> {
        if (isFatal(creature)) {
          int index = RANDOM.nextInt(SNIFFER_LETHAL_MK.length);
          this.woundMessageKey = SNIFFER_LETHAL_MK[index];
        } else {
          int index = RANDOM.nextInt(SNIFER_NON_LETHAL_MK.length);
          this.woundMessageKey = SNIFER_NON_LETHAL_MK[index];
        }
      }
      case Lurker ignored -> {
        if (isFatal(creature)) {
          if (creature.getHealth() == Creature.DEFAULT_HEALTH) {
            this.woundMessageKey = LURKER_LETHAL_MK[1];
          } else {
            this.woundMessageKey = LURKER_LETHAL_MK[0];
          }
        } else {
          int index = RANDOM.nextInt(LURKER_NON_LETHAL_MK.length);
          this.woundMessageKey = LURKER_NON_LETHAL_MK[index];
        }

      }
      case Mugger ignored -> {
        if (isFatal(creature)) {
          int index = RANDOM.nextInt(MUGGER_LETHAL_MK.length);
          this.woundMessageKey = MUGGER_LETHAL_MK[index];
        } else {
          int index = RANDOM.nextInt(MUGGER_NON_LETHAL_MK.length);
          this.woundMessageKey = MUGGER_NON_LETHAL_MK[index];
        }
      }
      case null -> log.error("No message for null monster !");

      default -> throw new IllegalStateException("Unhandled Monster subclass: " + monster.getClass().getSimpleName());
    }

  }
}
