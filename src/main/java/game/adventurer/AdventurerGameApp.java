package game.adventurer;

import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.service.MapGenerator;
import game.adventurer.ui.GameMapScene;
import game.adventurer.ui.SplashScreen;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurerGameApp extends Application {

  private Stage primaryStage;
  public static final Logger LOG = LoggerFactory.getLogger(AdventurerGameApp.class);

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;

    SplashScreen splashScreen = new SplashScreen("Adventurer Game");
    primaryStage.setScene(splashScreen);
    primaryStage.show();

    // Transition vers la scène de jeu après 3 secondes
    PauseTransition delay = new PauseTransition(Duration.seconds(3));
    delay.setOnFinished(event -> showGameMap());
    delay.play();
  }

  private void showGameMap() {
    try {
      GameMap gameMap = MapGenerator.generateMap(20, 20);
      GameMapScene gameMapScene = new GameMapScene(gameMap);
      primaryStage.setScene(gameMapScene);
    } catch (NoValidRangeException e) {
      LOG.error(e.getMessage(), e);
    }
  }

}
