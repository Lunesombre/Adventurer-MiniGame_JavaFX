package game.adventurer.model.wound;

import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.WoundCause;
import game.adventurer.service.LocalizedMessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonsterWound extends Wound {

  private final Monster monster;
  private final boolean causedByMonster;

  private static final String[] LURKER_LETHAL_MK = {"wound.lurker.lethal.1", "wound.lurker.lethal.2"};
  private static final String[] LURKER_NON_LETHAL_MK = {"wound.lurker.non_lethal.1", "wound.lurker.non_lethal.2"};
  private static final String[] MUGGER_LETHAL_MK = {"wound.mugger.lethal.1", "wound.mugger.lethal.2"};
  private static final String[] MUGGER_NON_LETHAL_MK = {"wound.mugger.non_lethal.1", "wound.mugger.non_lethal.2", "wound.mugger.non_lethal.3"};
  private static final String[] SNIFFER_LETHAL_MK = {"wound.sniffer.lethal"};
  private static final String[] SNIFER_NON_LETHAL_MK = {"wound.sniffer.non_lethal"};
  private static final String[] RAN_INTO_MONSTER_MK = {"wound.ranIntoMonster.1", "wound.ranIntoMonster.2", "wound.ranIntoMonster.3"};
  private static final String[] RAN_INTO_MONSTER_LETHAL_MK = {"wound.ranIntoMonster.lethal.1"};

  public MonsterWound(WoundCause cause, Monster monster, boolean causedByMonster) {
    super(cause);
    this.monster = monster;
    this.causedByMonster = causedByMonster;
    this.healthCost = (causedByMonster) ? monster.getBaseDamages() : (int) Math.ceil((double) monster.getBaseDamages() / 3);
  }

  @Override
  public void setWoundsMessage(Creature creature) {
    if (!causedByMonster) {
      if (isFatal(creature)) {
        this.woundMessageKey = RAN_INTO_MONSTER_LETHAL_MK[0];

      } else {
        // Passing the whole message as a key to allow including monster's name.
        // The LocalizedMessageService handles it and returns the key as is when it cannot find it.
        this.woundMessageKey = LocalizedMessageService.getInstance()
            .getMessage(RAN_INTO_MONSTER_MK[RANDOM.nextInt(RAN_INTO_MONSTER_MK.length)], monster.getName());
      }
      return;
    }
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
