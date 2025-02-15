package game.adventurer.ui.animation;


import game.adventurer.ui.MainGameScene;
import game.adventurer.ui.common.TriangleCreatureRepresentation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreatureAnimationManager {

  private final MainGameScene mainGameScene;

  public CreatureAnimationManager(MainGameScene mainGameScene) {
    this.mainGameScene = mainGameScene;
  }

  public void animateCreature(Node creatureRepresentation, int fromX, int fromY, int toX, int toY) {
    // FRAMES * FRAME_DURATION_MS should be equal to the duration used in Creature.move
    final int FRAMES = 60;
    final int FRAME_DURATION_MS = 5;
    double startX = mainGameScene.getXOffset() + (fromX + 0.5) * mainGameScene.getTileSize();
    double startY = mainGameScene.getYOffset() + (fromY + 0.5) * mainGameScene.getTileSize();
    double endX = mainGameScene.getXOffset() + (toX + 0.5) * mainGameScene.getTileSize();
    double endY = mainGameScene.getYOffset() + (toY + 0.5) * mainGameScene.getTileSize();

    double dx = (endX - startX) / FRAMES;
    double dy = (endY - startY) / FRAMES;
    switch (creatureRepresentation) {
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
        Timeline timeline = new Timeline();
        for (int i = 1; i <= FRAMES; i++) {
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
        timeline.play();
      }
      default -> log.error("Unhandled Creature representation");

    }
  }

}
