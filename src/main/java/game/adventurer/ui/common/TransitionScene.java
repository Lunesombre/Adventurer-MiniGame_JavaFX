package game.adventurer.ui.common;

import game.adventurer.common.SharedSize;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import lombok.Getter;

/**
 * Represents a transition scene that blends two BaseScene instances for smooth visual transitions. This class extends BaseScene and uses a StackPane
 * to overlay the old and new scenes.
 *
 * @param <T> The type of BaseScene this transition scene will handle.
 */
public class TransitionScene<T extends BaseScene> extends BaseScene {

  private final T oldScene;
  @Getter
  private final T newScene;

  /**
   * Constructs a new TransitionScene.
   *
   * @param sharedSize The shared size information for the scene.
   * @param oldScene   The scene that is being transitioned from.
   * @param newScene   The scene that is being transitioned to.
   */
  public TransitionScene(
      SharedSize sharedSize, T oldScene, T newScene) {
    super(new StackPane(), sharedSize);
    this.oldScene = oldScene;
    this.newScene = newScene;
    initialize();
  }

  @Override
  protected void initialize() {
    newScene.getRoot().setOpacity(0.0); // avoids a visual glitch (with Scene set at opacity 1.0 briefly then put to 0.0)

    // Create a StackPane to contain both scenes
    StackPane root = (StackPane) getRoot();
    root.getChildren().addAll(oldScene.getRoot(), newScene.getRoot());

    // Ensure both scenes take all available space
    StackPane.setAlignment(oldScene.getRoot(), Pos.CENTER);
    StackPane.setAlignment(newScene.getRoot(), Pos.CENTER);
  }

  @Override
  protected void onSizeChanged(double width, double height) {
    oldScene.updateSize();
    newScene.updateSize();
  }

  /**
   * Removes the old scene from this transition scene. This method should be called after the transition is complete.
   */
  public void removeOldScene() {
    StackPane root = (StackPane) getRoot();
    root.getChildren().remove(oldScene.getRoot());
  }
}
