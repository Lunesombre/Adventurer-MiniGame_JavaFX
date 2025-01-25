package game.adventurer.service;

import static game.adventurer.util.PathfindingUtil.calculateSearchArea;
import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.MissingCreatureException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.enums.MonsterStatus;
import game.adventurer.ui.animation.CreatureAnimationManager;
import game.adventurer.util.MiscUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MonsterBehaviorManager {

  public static final String STATUS_CHANGE_MESSAGE = "{} has detected Adventurer at {},{} and is now {} ";

  private final GameMap gameMap;
  private final Map<Creature, Node> creaturesRepresentationMap;
  private final List<Timeline> activeTimelines;

  public MonsterBehaviorManager(GameMap gameMap, Map<Creature, Node> creaturesRepresentationMap, List<Timeline> activeTimelines) {
    this.gameMap = gameMap;
    this.creaturesRepresentationMap = creaturesRepresentationMap;
    this.activeTimelines = activeTimelines;
  }

  public void moveMonsters(CreatureAnimationManager creatureAnimationManager) throws MissingCreatureException {
    Adventurer adventurer = gameMap.getAdventurer();
    for (Monster monster : gameMap.getMonsters()) {
      if (monster instanceof Mugger mugger) {
        if (!creaturesRepresentationMap.containsKey(mugger)) {
          throw new MissingCreatureException("Creature " + mugger.getName() + " not found in the creaturesRepresentationMap");
        }
        log.info("The monster '{}' started moving", mugger.getName());

        AtomicBoolean hasReachLastSeenPosition = new AtomicBoolean(false);
        AtomicBoolean justLeftMonsterFoV = new AtomicBoolean(false);
        AtomicReference<LinkedHashSet<Position>> pathToExplore = new AtomicReference<>(new LinkedHashSet<>());

        // Creates Timelines to move the Monsters
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {

          if (mugger.getStatus().equals(MonsterStatus.NEUTRAL) && mugger.canMove()) {
            boolean triggerAnimation = mugger.wander();
            if (triggerAnimation) {
              creatureAnimationManager.animateCreature(
                  creaturesRepresentationMap.get(mugger),
                  mugger.getPreviousTileX(),
                  mugger.getPreviousTileY(),
                  mugger.getTileX(),
                  mugger.getTileY()
              );
              // calculates and updates the monster's field of view after move
              calculateMonsterFieldOfView(mugger);
              // checks if it sees the Adventurer
              if (detectAdventurer(mugger, adventurer)) {
                Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
                mugger.setLastSeenAdventurerPosition(adventurerPosition);
                mugger.setStatus(MonsterStatus.ALERTED);
                log.info(STATUS_CHANGE_MESSAGE, mugger.getName(), adventurer.getTileX(), adventurer.getTileY(),
                    mugger.getStatus());
              }
            }

            log.trace("{} position : y={}, x={}, direction:{}", mugger.getName(), mugger.getTileY(), mugger.getTileX(), mugger.getFacingDirection());
          } else if (mugger.getStatus().equals(MonsterStatus.ALERTED) && mugger.canMove()) {
            try {
              mugger.pursue(gameMap);
              creatureAnimationManager.animateCreature(
                  creaturesRepresentationMap.get(mugger),
                  mugger.getPreviousTileX(),
                  mugger.getPreviousTileY(),
                  mugger.getTileX(),
                  mugger.getTileY()
              );
              // calculates and updates the monster's field of view after move
              calculateMonsterFieldOfView(mugger);
              // checks if it sees the Adventurer
              if (detectAdventurer(mugger, adventurer)) {
                Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
                mugger.setLastSeenAdventurerPosition(adventurerPosition);
              } else {
                mugger.setStatus(MonsterStatus.IN_SEARCH);
                justLeftMonsterFoV.set(true);
                log.info("{} has lost sight of Adventurer at x={},y={} and is now {} ", mugger.getName(), adventurer.getTileX(),
                    adventurer.getTileY(),
                    mugger.getStatus());
              }

            } catch (InvalidGameStateException e) {
              log.error("pursue() called but: {}. Resetting the monster status to NEUTRAL ", e.getMessage());
              mugger.setStatus(MonsterStatus.NEUTRAL);
            }

          } else if (mugger.getStatus().equals(MonsterStatus.IN_SEARCH) && mugger.canMove() && mugger.getLastSeenAdventurerPosition() != null) {
            if (justLeftMonsterFoV.get()) {
              pathToExplore.set((LinkedHashSet<Position>)
                  shortestPath(mugger, new Position(mugger.getTileX(), mugger.getTileY()), mugger.getLastSeenAdventurerPosition(), gameMap));
              // Storing Field of View when lost sight of the Adventurer
              mugger.setStoredFOV(calculateMonsterFieldOfView(mugger));

              justLeftMonsterFoV.set(false);
            }

            if (!hasReachLastSeenPosition.get() && monster.getSearchArea().isEmpty() && !pathToExplore.get().isEmpty()) {
              mugger.moveTo(pathToExplore.get().getFirst());
              mugger.getStoredFOV().add(pathToExplore.get()
                  .getFirst()); // adds it to the set of positions not to explore once the adventurer's last seen position is reached
              pathToExplore.get().removeFirst();
              // calculates and updates the monster's field of view after move
              calculateMonsterFieldOfView(mugger);
              // checks if it sees the Adventurer
              if (detectAdventurer(mugger, adventurer)) {
                Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
                mugger.setLastSeenAdventurerPosition(adventurerPosition);
                mugger.setStatus(MonsterStatus.ALERTED);
                log.info(STATUS_CHANGE_MESSAGE, mugger.getName(), adventurer.getTileX(), adventurer.getTileY(),
                    mugger.getStatus());
                return;
              }
              // verifies the Monster has reached the lastSeenPosition before its next move
              hasReachLastSeenPosition.set(mugger.getLastSeenAdventurerPosition().equals(new Position(mugger.getTileX(), mugger.getTileY())));
              if (hasReachLastSeenPosition.get()) {
                // if it has reached it, calculate the search area for next moves
                mugger.setSearchArea(calculateSearchArea(mugger, mugger.getLastSeenAdventurerPosition(), gameMap));
                hasReachLastSeenPosition.set(false);
                if (mugger.getSearchArea().isEmpty()) {
                  // prevents monster from freezing if the search area is empty already
                  mugger.chill();
                }
              }
            } else {
              mugger.search(gameMap);
            }
            creatureAnimationManager.animateCreature(
                creaturesRepresentationMap.get(mugger),
                mugger.getPreviousTileX(),
                mugger.getPreviousTileY(),
                mugger.getTileX(),
                mugger.getTileY()
            );
            // calculates and updates the monster's field of view after move
            calculateMonsterFieldOfView(mugger);
            // checks if it sees the Adventurer
            if (detectAdventurer(mugger, adventurer)) {
              mugger.setSearchTarget(null);
              mugger.getSearchArea().clear();
              Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
              mugger.setLastSeenAdventurerPosition(adventurerPosition);
              mugger.setStatus(MonsterStatus.ALERTED);
              log.info(STATUS_CHANGE_MESSAGE, mugger.getName(), adventurer.getTileX(), adventurer.getTileY(),
                  mugger.getStatus());
            }
          }

        }));

        // Repeats indefinitely until the Timeline is stopped
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        activeTimelines.add(timeline);

      }
    }
  }

  /**
   * Calculates the field of view for a specified Monster and updates its visibleTiles set.
   *
   * @param monster The monster for which to calculate the field of view.
   * @return A set of positions visible by the monster.
   */
  public Set<Position> calculateMonsterFieldOfView(Monster monster) {
    Set<Position> visibleTiles = MiscUtil.calculateFieldOfView(monster, gameMap);
    monster.setVisibleTiles(visibleTiles);
    return visibleTiles;
  }


  /**
   * Detects if the specified adventurer is within a given monster's field of view.
   *
   * @param monster    The monster whose field of view is being checked.
   * @param adventurer The adventurer whose position is being checked.
   * @return {@code true} if the adventurer is within the monster's field of view,{@code false} otherwise.
   */
  public boolean detectAdventurer(Monster monster, Adventurer adventurer) {
    Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
    return monster.getVisibleTiles().contains(adventurerPosition);
  }

}
