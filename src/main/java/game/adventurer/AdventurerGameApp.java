package game.adventurer;

import game.adventurer.common.SharedSize;
import game.adventurer.config.SceneConfig;
import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.service.MapGenerator;
import game.adventurer.ui.GameMapScene;
import game.adventurer.ui.SplashScreen;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurerGameApp extends Application {

  private final SceneConfig sceneConfig = SceneConfig.getInstance();
  private final SharedSize sharedSize = new SharedSize(sceneConfig.getWidth(), sceneConfig.getHeight());
  private Stage primaryStage;
  public static final Logger LOG = LoggerFactory.getLogger(AdventurerGameApp.class);

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    primaryStage.getIcons().add(new Image("assets/icons/A.png"));

    SplashScreen splashScreen = SplashScreen.create("Adventurer Game", sharedSize);
    primaryStage.setScene(splashScreen);
    primaryStage.show();

    // Transition to the game scene after 3 seconds
    PauseTransition delay = new PauseTransition(Duration.seconds(3));
    delay.setOnFinished(event -> showGameMap());
    delay.play();
  }

  private void showGameMap() {
    try {
      GameMap gameMap = MapGenerator.generateMap(20, 20);
      GameMapScene gameMapScene = GameMapScene.create(gameMap, sharedSize);
      primaryStage.setScene(gameMapScene);
    } catch (NoValidRangeException e) {
      LOG.error(e.getMessage(), e);
    }
  }

}
