package game.adventurer.ui.common;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SceneTransitionsManager {

  private SceneTransitionsManager() {
  }


  public static void crossFadeTransition(Stage stage, BaseScene newScene, Duration duration) {
    BaseScene oldScene = (BaseScene) stage.getScene();

    // Create a transitionScene
    TransitionScene<BaseScene> transitionScene = new TransitionScene<>(oldScene.getSharedSize(), oldScene, newScene);

    // Define the transition Stage as the current scene
    stage.setScene(transitionScene);

    // Create the cross-fading transition
    Timeline timeline = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(oldScene.getRoot().opacityProperty(), 1.0),
            new KeyValue(newScene.getRoot().opacityProperty(), 0.0)
        ),
        new KeyFrame(duration,
            new KeyValue(oldScene.getRoot().opacityProperty(), 0.0),
            new KeyValue(newScene.getRoot().opacityProperty(), 1.0)
        )
    );

    timeline.setOnFinished(event ->
            // remove the oldScene from the StackPane
            transitionScene.removeOldScene()
        // Lots of trouble setting the stage to the new scene.
        // Let's keep it that way as the oldScene has been removed, it should not cause problems.
    );

    timeline.play();
  }

}
