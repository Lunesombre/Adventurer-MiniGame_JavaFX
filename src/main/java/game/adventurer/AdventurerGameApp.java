package game.adventurer;

import game.adventurer.common.SharedSize;
import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.service.MapGenerator;
import game.adventurer.ui.GameMapScene;
import game.adventurer.ui.SplashScreen;
import game.adventurer.util.ScreenUtils;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurerGameApp extends Application {


  private SharedSize sharedSize;
  private Stage primaryStage;
  private static final float INITIAL_SCREEN_SIZE_RATIO = 0.8f;
  private static final Logger LOG = LoggerFactory.getLogger(AdventurerGameApp.class);

  @Override
  public void start(Stage primaryStage) {
    double initialAppWidth = ScreenUtils.getScreenWidth() * INITIAL_SCREEN_SIZE_RATIO;
    double initialAppHeight = ScreenUtils.getScreenHeight() * INITIAL_SCREEN_SIZE_RATIO;
    sharedSize = new SharedSize(initialAppWidth, initialAppHeight);

    this.primaryStage = primaryStage;
    primaryStage.getIcons().add(new Image("assets/icons/A.png"));

    double minWidth = Math.min(1024,
        ScreenUtils.getScreenWidth()); // to handle the improbable case where the user's screen size would be under 1024x768
    double minHeight = Math.min(768,
        ScreenUtils.getScreenHeight()); // to handle the improbable case where the user's screen size would be under 1024x768
    // prevents reducing the app dimension under defined size: avoid UI breaking
    primaryStage.setMinWidth(minWidth);
    primaryStage.setMinHeight(minHeight);

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
