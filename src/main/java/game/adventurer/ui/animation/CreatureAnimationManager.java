package game.adventurer.ui.animation;


import game.adventurer.exceptions.WrongTypeOfCreatureException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.service.WoundManager;
import game.adventurer.ui.MainGameScene;
import game.adventurer.ui.common.TriangleCreatureRepresentation;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreatureAnimationManager {

  private final MainGameScene mainGameScene;
  private final GameMap gameMap;
  private final WoundManager woundManager;

  public CreatureAnimationManager(MainGameScene mainGameScene, GameMap gameMap) {
    this.mainGameScene = mainGameScene;
    this.gameMap = gameMap;
    this.woundManager = new WoundManager(gameMap.getWoundsList());
  }

  public void animateCreature(Map.Entry<Creature, Node> creatureNodeEntry, int fromX, int fromY, int toX, int toY)
      throws WrongTypeOfCreatureException {
    // FRAMES * FRAME_DURATION_MS should be equal to the duration used in Creature.move
    final int FRAMES = 60;
    final int FRAME_DURATION_MS = 5;
    double startX = mainGameScene.getXOffset() + (fromX + 0.5) * mainGameScene.getTileSize();
    double startY = mainGameScene.getYOffset() + (fromY + 0.5) * mainGameScene.getTileSize();
    double endX = mainGameScene.getXOffset() + (toX + 0.5) * mainGameScene.getTileSize();
    double endY = mainGameScene.getYOffset() + (toY + 0.5) * mainGameScene.getTileSize();

    double dx = (endX - startX) / FRAMES;
    double dy = (endY - startY) / FRAMES;

    Creature creature = creatureNodeEntry.getKey();

    switch (creatureNodeEntry.getValue()) {
      case Circle circle -> {
        Timeline timeline = new Timeline();
        for (int i = 1; i <= FRAMES; i++) {
          final int frame = i;
          KeyFrame keyFrame = new KeyFrame(
              Duration.millis(i * (double) FRAME_DURATION_MS),
              event -> {
                circle.setCenterX(startX + dx * frame);
                circle.setCenterY(startY + dy * frame);
              }
          );
          timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
      }
      case TriangleCreatureRepresentation triangle -> {
        boolean succesfulAttack = false;
        Timeline timeline = new Timeline();
        for (int i = 1; i <= FRAMES; i++) {
          if (i == FRAMES / 2) {
            // half the translation is done, let's check for the adventurer's position

            if (creature instanceof Monster) {
              Adventurer adventurer = gameMap.getAdventurer();
              Position creaturePosition = creature.getCurrentPosition();
              Position adventurerPosition = adventurer.getCurrentPosition();
              if (creaturePosition.equals(adventurerPosition)) {
                woundManager.createWound(creature, adventurer);
                woundManager.handleWound(mainGameScene.getRightPanelController(), mainGameScene.getOnGameOver(), adventurer);
                mainGameScene.showDamageEffect();
                succesfulAttack = true;
                // New KeyFrame to reset position of the Monster and its representation
                KeyFrame resetPositionFrame = new KeyFrame(
                    Duration.millis(i * (double) FRAME_DURATION_MS),
                    event -> {
                      // Reset position
                      triangle.setLayoutX(startX);
                      triangle.setLayoutY(startY);
                      creature.setTileX(creature.getPreviousTileX());
                      creature.setTileY(creature.getPreviousTileY());
                      creature.setCurrentPosition(creature.getPreviousPosition());
                    }
                );
                timeline.getKeyFrames().add(resetPositionFrame);
                // no more KeyFrames needed: break
                break;

              }
            }

          }
          final int frame = i;
          KeyFrame keyFrame = new KeyFrame(
              Duration.millis(i * (double) FRAME_DURATION_MS),
              event -> {
                triangle.setLayoutX(startX + dx * frame);
                triangle.setLayoutY(startY + dy * frame);
              }
          );
          timeline.getKeyFrames().add(keyFrame);
        }

        final boolean finalSuccessfulAttack = succesfulAttack;
        timeline.setOnFinished(event -> {
          if (finalSuccessfulAttack) {
            if (creature instanceof Sniffer) {
              creature.setCooldownTime(creature.getCooldownTime() + 100); // shorter cooldown for sniffers
            } else {
              creature.setCooldownTime(creature.getCooldownTime() + 500);
            }
          } else {
            creature.setCooldownTime(creature.resetCooldownTime());
            gameMap.freeTile(creature.getPreviousPosition()); // Try to free the previously occupied tile.
            gameMap.occupyTile(creature.getCurrentPosition()); // Occupies the new tile.
          }
        });
        timeline.play();
      }
      default -> log.error("Unhandled Creature representation");

    }
  }


}
