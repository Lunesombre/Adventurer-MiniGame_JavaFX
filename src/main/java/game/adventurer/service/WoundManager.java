package game.adventurer.service;

import game.adventurer.controller.RightPanelController;
import game.adventurer.exceptions.WrongTypeOfCreatureException;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.WoundCause;
import game.adventurer.model.wound.MonsterWound;
import game.adventurer.model.wound.WoodsWound;
import game.adventurer.model.wound.Wound;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class WoundManager {

  private final List<Wound> adventurerWoundsList;

  public WoundManager(List<Wound> adventurerWoundsList) {
    this.adventurerWoundsList = adventurerWoundsList;
  }

  public Wound createWound(Creature perpetrator, Creature victim) throws WrongTypeOfCreatureException {
    return createWound(perpetrator, victim, true);
  }

  public Wound createWound(Creature victim) {
    return createWoodWound(victim);
  }

  public Wound createWound(Creature perpetrator, Creature victim, boolean causedByMonster) throws WrongTypeOfCreatureException {
    return createMonsterWound(perpetrator, victim, causedByMonster);
  }

  private WoodsWound createWoodWound(Creature victim) {
    WoodsWound wound = new WoodsWound(WoundCause.WOODS);
    wound.setWoundsMessage(victim);
    victim.setHealth(victim.getHealth() - wound.getHealthCost());
    adventurerWoundsList.add(wound);
    return wound;
  }

  private Wound createMonsterWound(Creature perpetrator, Creature victim, boolean causedByMonster) throws WrongTypeOfCreatureException {
    WoundCause cause = getMonsterWoundCause(perpetrator);

    MonsterWound wound = new MonsterWound(cause, (Monster) perpetrator, causedByMonster);
    wound.setWoundsMessage(victim);
    victim.setHealth(victim.getHealth() - wound.getHealthCost());
    if (victim instanceof Adventurer) {
      adventurerWoundsList.add(wound);
    }
    return wound;
  }

  private static WoundCause getMonsterWoundCause(Creature perpetrator) throws WrongTypeOfCreatureException {
    return switch (perpetrator) {
      case Lurker ignored -> WoundCause.LURKER;
      case Sniffer ignored -> WoundCause.SNIFFER;
      case Mugger ignored -> WoundCause.MUGGER;
      case null -> throw new RuntimeException("Cannot be called with null as perpetrator");
      default -> throw new WrongTypeOfCreatureException("Unauthorized type of creature used: " + perpetrator.getClass());
    };

  }

  public void handleWound(RightPanelController rightPanelController, Runnable onGameOver, Creature victim) {
    rightPanelController.addMessage(adventurerWoundsList.getLast().getWoundMessageKey());
    if (isCreatureDead(victim)) {
      if (victim instanceof Adventurer) {
        if (onGameOver != null) {
          onGameOver.run();
        } else {
          log.error("onGameOver is null");
        }
      } else {
        log.trace("Creature {} is dead.", victim);
      }
    }
  }

  private boolean isCreatureDead(Creature creature) {
    return creature.getHealth() <= 0;
  }


}
