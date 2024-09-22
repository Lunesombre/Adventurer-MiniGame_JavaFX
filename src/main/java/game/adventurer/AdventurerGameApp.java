package game.adventurer;

import static game.adventurer.ui.common.SceneTransitionsManager.crossFadeTransition;

import game.adventurer.common.SharedSize;
import game.adventurer.config.AppConfig;
import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MapSize;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.MapGenerator;
import game.adventurer.ui.EndGameScene;
import game.adventurer.ui.GameOverScene;
import game.adventurer.ui.MainGameScene;
import game.adventurer.ui.PlayerSetupScene;
import game.adventurer.ui.SplashScreen;
import game.adventurer.util.ScreenUtils;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdventurerGameApp extends Application {


  private SharedSize sharedSize;
  private Stage primaryStage;
  private static final float INITIAL_SCREEN_SIZE_RATIO = 0.8f;
  private static final String APP_NAME = "Adventurer Game";
  private static final HighScoreManager highScoreManager = new HighScoreManager();

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

    showSplashScreen();
  }

  private void showSplashScreen() {
    SplashScreen splashScreen = SplashScreen.create(APP_NAME, sharedSize);

    primaryStage.setScene(splashScreen);
    primaryStage.show();

    // Wait a bit before initiating transition
    PauseTransition initialDelay = new PauseTransition(Duration.seconds(2));
    initialDelay.setOnFinished(event -> {
      // Creating next scene
      PlayerSetupScene playerSetupScene = new PlayerSetupScene(sharedSize);
      playerSetupScene.setOnStartGame(this::startGame);

      // Cross dade transition to playerSetupScene
      crossFadeTransition(primaryStage, playerSetupScene, Duration.seconds(1));
    });
    initialDelay.play();
  }

  private void showPlayerSetup() {
    PlayerSetupScene playerSetupScene = new PlayerSetupScene(sharedSize);
    playerSetupScene.setOnStartGame(this::startGame);
    primaryStage.setScene(playerSetupScene);
  }

  private void startGame(String playerName, MapSize mapSize, DifficultyLevel difficultyLevel) {
    try {
      GameMap gameMap = MapGenerator.generateMap(mapSize.getSize(), mapSize.getSize(), playerName);
      MainGameScene mainGameScene = MainGameScene.create(gameMap, sharedSize, difficultyLevel);
      mainGameScene.setOnGameEnd(() -> showEndGame(gameMap, mainGameScene.getMovesCount(), mainGameScene.getDifficultyLevel(),
          mainGameScene.getInitialDistanceToTreasure()));
      mainGameScene.setOnGameOver(() -> showGameOver(gameMap));
      primaryStage.setScene(mainGameScene);
      log.info("Starting game with player: {}, and map size: {}", playerName, mapSize);
    } catch (NoValidRangeException e) {
      log.error(e.getMessage(), e);
    } catch (InvalidGameStateException e) {
      handleInvalidGameState(e);
    }
  }

  private void showEndGame(GameMap gameMap, int movesCount, DifficultyLevel difficultyLevel, int initialDistanceToTreasure) {
    EndGameScene endGameScene = new EndGameScene(sharedSize, gameMap, movesCount, difficultyLevel, initialDistanceToTreasure, highScoreManager);
    endGameScene.setOnRestartGame(this::showPlayerSetup);
    endGameScene.setOnQuitGame(this::quitGame);
    primaryStage.setScene(endGameScene);
  }

  private void showGameOver(GameMap gameMap) {
    GameOverScene gameOverScene = new GameOverScene(gameMap, sharedSize);
    gameOverScene.setOnReplayGame(this::showPlayerSetup);
    gameOverScene.setOnQuitGame(this::quitGame);
    primaryStage.setScene(gameOverScene);
  }

  private void quitGame() {
    Platform.exit(); // Clean exit
    System.exit(0); // Even cleaner if background threads running or resources to free
  }

  private void handleInvalidGameState(InvalidGameStateException e) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Critical Error");
      alert.setHeaderText("Error: " + e.getMessage());
      alert.setContentText("The game cannot continue due to an invalid state. Game will close upon closing this alert.");
      String cssPath = AppConfig.getInstance().getGlobalStylePath();
      alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
      alert.getDialogPane().getStyleClass().add("alert");
      Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/crane-et-os.png")));
      alert.getDialogPane().setGraphic(new ImageView(icon));
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(icon);
      alert.showAndWait();
      quitGame();
    });
  }


}
