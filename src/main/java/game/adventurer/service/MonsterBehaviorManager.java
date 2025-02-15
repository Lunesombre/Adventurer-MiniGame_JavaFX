package game.adventurer.service;

import static game.adventurer.util.PathfindingUtil.calculateSearchArea;
import static game.adventurer.util.PathfindingUtil.findNearestTileOfType;
import static game.adventurer.util.PathfindingUtil.shortestPath;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.MissingCreatureException;
import game.adventurer.exceptions.WrongTypeOfCreatureException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.MonsterStatus;
import game.adventurer.ui.animation.CreatureAnimationManager;
import game.adventurer.util.MiscUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
  public static final String PURSUE_CALLED_NO_ADVENTURER_KNOWN_POSITION = "pursue() called but: {}. Resetting the monster status to NEUTRAL";

  private final GameMap gameMap;
  private final Map<Creature, Node> creaturesRepresentationMap;
  private final List<Timeline> activeTimelines;

  public MonsterBehaviorManager(GameMap gameMap, Map<Creature, Node> creaturesRepresentationMap, List<Timeline> activeTimelines) {
    this.gameMap = gameMap;
    this.creaturesRepresentationMap = creaturesRepresentationMap;
    this.activeTimelines = activeTimelines;
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

  /**
   * Moves all monsters on the game map according to their behavior.
   *
   * @param creatureAnimationManager the animation manager handling creature animations
   * @throws MissingCreatureException if a monster is not found in the representation map
   */
  public void moveMonsters(CreatureAnimationManager creatureAnimationManager) throws MissingCreatureException, WrongTypeOfCreatureException {
    Adventurer adventurer = gameMap.getAdventurer();
    for (Monster monster : gameMap.getMonsters()) {
      if (monster instanceof Mugger mugger) {
        moveMugger(creatureAnimationManager, mugger, adventurer);
      } else if (monster instanceof Sniffer sniffer) {
        moveSniffer(creatureAnimationManager, sniffer, adventurer);
      } else if (monster instanceof Lurker lurker) {
        moveLurker(creatureAnimationManager, lurker, adventurer);
      }
    }
  }

  /**
   * Handles the movement logic for a Mugger.
   *
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param mugger                   the Mugger instance to move
   * @param adventurer               the Adventurer instance to track
   * @throws MissingCreatureException if the Mugger is not found in the representation map
   */
  private void moveMugger(CreatureAnimationManager creatureAnimationManager, Mugger mugger, Adventurer adventurer)
      throws MissingCreatureException, WrongTypeOfCreatureException {
    checkMonstersPresence(mugger);
    logStartedMovingMessage(mugger);

    AtomicBoolean hasReachLastSeenPosition = new AtomicBoolean(false);
    AtomicBoolean justLeftMonsterFoV = new AtomicBoolean(false);
    AtomicReference<LinkedHashSet<Position>> pathToExplore = new AtomicReference<>(new LinkedHashSet<>());

    Timeline timeline = createTimelineForMonster(mugger, creatureAnimationManager, adventurer, hasReachLastSeenPosition, justLeftMonsterFoV,
        pathToExplore);

    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    activeTimelines.add(timeline);
  }

  /**
   * Handles the movement logic for a Sniffer.
   * <p>
   * {@code moveSniffer()} does not use the {@code createTimelineForMonster()} method nor other methods shared with other types of {@code Monster}
   * except {@code handleNeutralMovement()}
   *
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param sniffer                  the Sniffer instance to move
   * @param adventurer               the Adventurer instance to track
   * @throws MissingCreatureException if the Sniffer is not found in the representation map
   */
  private void moveSniffer(CreatureAnimationManager creatureAnimationManager, Sniffer sniffer, Adventurer adventurer)
      throws MissingCreatureException {
    checkMonstersPresence(sniffer);
    logStartedMovingMessage(sniffer);

    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event ->
        handleSnifferMovement(sniffer, creatureAnimationManager, adventurer)
    ));

    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    activeTimelines.add(timeline);
  }

  /**
   * Handles the movement logic for a Lurker.
   *
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param lurker                   the Lurker instance to move
   * @param adventurer               the Adventurer instance to track
   * @throws MissingCreatureException if the Lurker is not found in the representation map
   */
  private void moveLurker(CreatureAnimationManager creatureAnimationManager, Lurker lurker, Adventurer adventurer)
      throws MissingCreatureException, WrongTypeOfCreatureException {
    checkMonstersPresence(lurker);
    logStartedMovingMessage(lurker);

    AtomicBoolean hasReachLastSeenPosition = new AtomicBoolean(false);
    AtomicBoolean justLeftMonsterFoV = new AtomicBoolean(false);
    AtomicReference<LinkedHashSet<Position>> pathToExplore = new AtomicReference<>(new LinkedHashSet<>());

    Timeline timeline = createTimelineForMonster(lurker, creatureAnimationManager, adventurer, hasReachLastSeenPosition, justLeftMonsterFoV,
        pathToExplore);

    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
    activeTimelines.add(timeline);
  }

  /**
   * Creates a timeline for a monster's movement behavior.
   * <p>
   * <i>NB:</i> not used to handle Sniffers.
   *
   * @param monster                  the monster to move
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   * @param hasReachLastSeenPosition flag indicating if the monster has reached the last seen position of the adventurer
   * @param justLeftMonsterFoV       flag indicating if the monster has just lost sight of the adventurer
   * @param pathToExplore            the path the monster should explore when searching
   * @return a configured Timeline instance for the monster's movement
   */
  private Timeline createTimelineForMonster(Monster monster, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer,
      AtomicBoolean hasReachLastSeenPosition, AtomicBoolean justLeftMonsterFoV, AtomicReference<LinkedHashSet<Position>> pathToExplore)
      throws WrongTypeOfCreatureException {
    if (monster instanceof Sniffer) {
      throw new WrongTypeOfCreatureException("This method cannot be used with" + Sniffer.class.getName());
    }
    return new Timeline(new KeyFrame(Duration.millis(100), event ->
        handleMonsterMovement(monster, creatureAnimationManager, adventurer, hasReachLastSeenPosition, justLeftMonsterFoV, pathToExplore)
    ));
  }

  /**
   * Handles the movement of a monster based on its current status.
   * <p><i>NB:</i> not used to handle Sniffers.
   *
   * @param monster                  the monster to move
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   * @param hasReachLastSeenPosition flag indicating if the monster has reached the last seen position of the adventurer
   * @param justLeftMonsterFoV       flag indicating if the monster has just lost sight of the adventurer
   * @param pathToExplore            the path the monster should explore when searching
   */
  private void handleMonsterMovement(Monster monster, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer,
      AtomicBoolean hasReachLastSeenPosition, AtomicBoolean justLeftMonsterFoV, AtomicReference<LinkedHashSet<Position>> pathToExplore) {

    if (monster.getStatus().equals(MonsterStatus.NEUTRAL)) {
      if (monster instanceof Lurker lurker) {
        handleLurkerNeutralMovement(lurker, creatureAnimationManager, adventurer);
      } else if (monster.canMove()) {
        handleNeutralMovement(monster, creatureAnimationManager, adventurer);
      }

    } else if (monster.getStatus().equals(MonsterStatus.ALERTED) && monster.canMove()) {
      handleAlertedMovement(monster, creatureAnimationManager, adventurer, justLeftMonsterFoV);
    } else if (monster.getStatus().equals(MonsterStatus.IN_SEARCH) && monster.canMove() && monster.getLastSeenAdventurerPosition() != null) {
      handleSearchMovement(monster, creatureAnimationManager, adventurer, hasReachLastSeenPosition, justLeftMonsterFoV, pathToExplore);
    }
  }

  /**
   * Handles the movement logic for a neutral monster.
   *
   * @param monster                  the monster to move - it is supposed to be a Mugger or a Sniffer
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   */
  private void handleNeutralMovement(Monster monster, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer) {
    boolean triggerAnimation = monster.wander();
    if (triggerAnimation) {
      animateCreature(creatureAnimationManager, monster);
    }
    // calculates and updates the monster's field of view after move
    calculateMonsterFieldOfView(monster);
    // checks if it sees the Adventurer
    if (detectAdventurer(monster, adventurer)) {
      updateLastSeenAdventurerPosition(monster, adventurer);
      monster.setStatus(MonsterStatus.ALERTED);
      log.info(STATUS_CHANGE_MESSAGE, monster.getName(), adventurer.getTileX(), adventurer.getTileY(), monster.getStatus());
    }
    log.trace("{} position : y={}, x={}, direction:{}", monster.getName(), monster.getTileY(), monster.getTileX(), monster.getFacingDirection());
  }

  /**
   * Handles the specific movement logic for a neutral Lurker.
   *
   * @param lurker                   the Lurker instance to move
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   */
  private void handleLurkerNeutralMovement(Lurker lurker, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer) {
    boolean triggerAnimation = false;
    long currentTime = System.currentTimeMillis();
    boolean canMoveOnPathTile = lurker.getLastMoveTime() + 1200 < currentTime; // if the lurker is on a Tile.Type.PATH it means
    // it has chased the Adventurer and lost it, and is now exhausted, thus slower, until he gets back to the woods
    if (gameMap.getTileTypeAt(lurker.getTileX(), lurker.getTileY()).equals(Type.PATH)) {
      if (canMoveOnPathTile) {
        Position nearestWood = findNearestTileOfType(new Position(lurker.getTileX(), lurker.getTileY()), gameMap, Type.WOOD);
        if (nearestWood != null) {
          LinkedHashSet<Position> shortestPath = (LinkedHashSet<Position>) shortestPath(lurker,
              new Position(lurker.getTileX(), lurker.getTileY()), nearestWood, gameMap);
          try {
            Position nextPos = shortestPath.getFirst();
            lurker.moveTo(nextPos);
            triggerAnimation = true;
          } catch (NoSuchElementException e) {
            log.warn("No path to woods found for {}: {}", lurker.getName(), e.getMessage());
          }
        }
      }
    } else if (lurker.canMove()) {
      // Lurker is back in the woods and doesn't see the adventurer, it won't travel through PATH tiles
      lurker.setRushCounter(0); // reset the Lurker's ability to rush the Adventurer
      lurker.getAllowedTileTypes().remove(Type.PATH);
      triggerAnimation = lurker.wander();
    }
    if (triggerAnimation) {
      animateCreature(creatureAnimationManager, lurker);
    }
    // calculates and updates the monster's field of view after move
    calculateMonsterFieldOfView(lurker);
    log.trace("LURKER FOV size: {}, FOV:{}", lurker.getVisibleTiles().size(), lurker.getVisibleTiles());
    // checks if it sees the Adventurer
    if (detectAdventurer(lurker, adventurer)) {
      updateLastSeenAdventurerPosition(lurker, adventurer);
      lurker.setStatus(MonsterStatus.ALERTED);
      lurker.getAllowedTileTypes().add(Type.PATH); // authorizes the Lurker to move on PATH tiles when he finds the adventurer
      log.info(STATUS_CHANGE_MESSAGE, lurker.getName(), adventurer.getTileX(), adventurer.getTileY(), lurker.getStatus());
    }

    log.trace("{} position : y={}, x={}, direction:{}", lurker.getName(), lurker.getTileY(), lurker.getTileX(), lurker.getFacingDirection());
  }

  /**
   * Handles the movement logic for an alerted monster.
   *
   * @param monster                  the monster to move - a Lurker or a Mugger (no Sniffers)
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   * @param justLeftMonsterFoV       flag indicating if the monster has just lost sight of the adventurer
   */
  private void handleAlertedMovement(Monster monster, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer,
      AtomicBoolean justLeftMonsterFoV) {
    try {
      monster.pursue(gameMap);
      animateCreature(creatureAnimationManager, monster);
      calculateMonsterFieldOfView(monster);
      if (detectAdventurer(monster, adventurer)) {
        updateLastSeenAdventurerPosition(monster, adventurer);
      } else {
        monster.setStatus(MonsterStatus.IN_SEARCH);
        justLeftMonsterFoV.set(true);
        log.info("{} has lost sight of Adventurer at x={},y={} and is now {} ", monster.getName(), adventurer.getTileX(), adventurer.getTileY(),
            monster.getStatus());
      }
    } catch (InvalidGameStateException e) {
      log.error(PURSUE_CALLED_NO_ADVENTURER_KNOWN_POSITION, e.getMessage());
      monster.setStatus(MonsterStatus.NEUTRAL);
    }
  }

  /**
   * Handles the movement logic for a searching monster.
   *
   * @param monster                  the monster to move
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   * @param hasReachLastSeenPosition flag indicating if the monster has reached the last seen position of the adventurer
   * @param justLeftMonsterFoV       flag indicating if the monster has just lost sight of the adventurer
   * @param pathToExplore            the path the monster should explore when searching
   */
  private void handleSearchMovement(Monster monster, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer,
      AtomicBoolean hasReachLastSeenPosition, AtomicBoolean justLeftMonsterFoV, AtomicReference<LinkedHashSet<Position>> pathToExplore) {
    if (justLeftMonsterFoV.get()) {
      pathToExplore.set((LinkedHashSet<Position>) shortestPath(monster, new Position(monster.getTileX(), monster.getTileY()),
          monster.getLastSeenAdventurerPosition(), gameMap));
      // Storing Field of View when lost sight of the Adventurer
      monster.setStoredFOV(calculateMonsterFieldOfView(monster));
      justLeftMonsterFoV.set(false);
    }

    if (!hasReachLastSeenPosition.get() && monster.getSearchArea().isEmpty() && !pathToExplore.get().isEmpty()) {
      monster.moveTo(pathToExplore.get().getFirst());
      monster.getStoredFOV()
          .add(pathToExplore.get().getFirst()); // adds it to the set of positions not to explore once the adventurer's last seen position is reached
      pathToExplore.get().removeFirst();
      // calculates and updates the monster's field of view after move
      calculateMonsterFieldOfView(monster);
      // checks if it sees the Adventurer
      if (detectAdventurer(monster, adventurer)) {
        updateLastSeenAdventurerPosition(monster, adventurer);
        monster.setStatus(MonsterStatus.ALERTED);
        log.info(STATUS_CHANGE_MESSAGE, monster.getName(), adventurer.getTileX(), adventurer.getTileY(), monster.getStatus());
        return;
      }
      // verifies the Monster has reached the lastSeenPosition before its next move
      hasReachLastSeenPosition.set(monster.getLastSeenAdventurerPosition().equals(new Position(monster.getTileX(), monster.getTileY())));
      if (hasReachLastSeenPosition.get()) {
        // if it has reached it, calculate the search area for next moves
        monster.setSearchArea(calculateSearchArea(monster, monster.getLastSeenAdventurerPosition(), gameMap));
        hasReachLastSeenPosition.set(false);
        if (monster.getSearchArea().isEmpty()) {
          // prevents monster from freezing if the search area is empty already
          monster.chill();
        }
      }
    } else {
      monster.search(gameMap);
    }
    animateCreature(creatureAnimationManager, monster);
    // calculates and updates the monster's field of view after move
    calculateMonsterFieldOfView(monster);
    // checks if it sees the Adventurer
    if (detectAdventurer(monster, adventurer)) {
      monster.setSearchTarget(null);
      monster.getSearchArea().clear();
      updateLastSeenAdventurerPosition(monster, adventurer);
      monster.setStatus(MonsterStatus.ALERTED);
      log.info(STATUS_CHANGE_MESSAGE, monster.getName(), adventurer.getTileX(), adventurer.getTileY(), monster.getStatus());
    }
  }

  /**
   * Handles the movement logic for a Sniffer.
   *
   * @param sniffer                  the Sniffer instance to move
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param adventurer               the Adventurer instance to track
   */
  private void handleSnifferMovement(Sniffer sniffer, CreatureAnimationManager creatureAnimationManager, Adventurer adventurer) {
    if (sniffer.getStatus().equals(MonsterStatus.NEUTRAL) && sniffer.canMove()) {
      handleNeutralMovement(sniffer, creatureAnimationManager, adventurer);
    } else if (sniffer.getStatus().equals(MonsterStatus.ALERTED) && sniffer.canMove()) {
      try {
        sniffer.pursue(gameMap);
        animateCreature(creatureAnimationManager, sniffer);
        calculateMonsterFieldOfView(sniffer);
        if (detectAdventurer(sniffer, adventurer)) {
          updateLastSeenAdventurerPosition(sniffer, adventurer);
        } else {
          sniffer.setStatus(MonsterStatus.IN_SEARCH);
          sniffer.setSearchTarget(new Position(adventurer.getTileX(), adventurer.getTileY()));
          log.info("{} has lost sight of Adventurer at {} and is now {} ", sniffer.getName(), sniffer.getSearchTarget(), sniffer.getStatus());
        }
      } catch (InvalidGameStateException e) {
        log.error(PURSUE_CALLED_NO_ADVENTURER_KNOWN_POSITION, e.getMessage());
        sniffer.setStatus(MonsterStatus.NEUTRAL);
      }
    } else if (sniffer.getStatus().equals(MonsterStatus.IN_SEARCH) && sniffer.canMove() && sniffer.getLastSeenAdventurerPosition() != null) {
      sniffer.search(gameMap);
      animateCreature(creatureAnimationManager, sniffer);
      calculateMonsterFieldOfView(sniffer);
      if (detectAdventurer(sniffer, adventurer)) {
        sniffer.setSearchTarget(null);
        sniffer.getSearchArea().clear(); // Sniffer's search area isn't used anyway
        updateLastSeenAdventurerPosition(sniffer, adventurer);
        sniffer.setStatus(MonsterStatus.ALERTED);
        log.info(STATUS_CHANGE_MESSAGE, sniffer.getName(), adventurer.getTileX(), adventurer.getTileY(), sniffer.getStatus());
      } else if (sniffer.getStatus().equals(MonsterStatus.IN_SEARCH)) {
        sniffer.setSearchTarget(new Position(adventurer.getTileX(), adventurer.getTileY()));
      } else {
        // Sniffer might have chilled out if the Adventurer managed to get very far
        sniffer.setSearchTarget(null);
      }
    }
  }

  /**
   * Animates a creature's movement on the game map.
   *
   * @param creatureAnimationManager the animation manager handling creature animations
   * @param monster                  the monster to animate
   */
  private void animateCreature(CreatureAnimationManager creatureAnimationManager, Monster monster) {
    creatureAnimationManager.animateCreature(
        creaturesRepresentationMap.get(monster),
        monster.getPreviousTileX(),
        monster.getPreviousTileY(),
        monster.getTileX(),
        monster.getTileY()
    );
  }

  /**
   * Updates the last seen position of the adventurer for a given monster.
   *
   * @param monster    the monster tracking the adventurer
   * @param adventurer the Adventurer instance being tracked
   */
  private void updateLastSeenAdventurerPosition(Monster monster, Adventurer adventurer) {
    Position adventurerPosition = new Position(adventurer.getTileX(), adventurer.getTileY());
    monster.setLastSeenAdventurerPosition(adventurerPosition);
  }

  /**
   * Logs movement-related messages for a monster.
   *
   * @param monster the monster whose movement is being logged
   */
  private void logStartedMovingMessage(Monster monster) {
    log.info("The monster '{}' started moving", monster.getName());
  }

  private void checkMonstersPresence(Monster monster) throws MissingCreatureException {
    if (!creaturesRepresentationMap.containsKey(monster)) {
      throw new MissingCreatureException("Creature " + monster.getName() + " not found in the creaturesRepresentationMap");
    }
  }

}
