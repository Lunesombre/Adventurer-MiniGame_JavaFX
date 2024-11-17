package game.adventurer;

import static game.adventurer.ui.common.SceneTransitionsManager.crossFadeTransition;
import static game.adventurer.util.MiscUtil.alertInitializer;

import game.adventurer.common.SharedSize;
import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.NoValidRangeException;
import game.adventurer.model.GameMap;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MapSize;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.service.MapGenerator;
import game.adventurer.ui.EndGameScene;
import game.adventurer.ui.GameOverScene;
import game.adventurer.ui.MainGameScene;
import game.adventurer.ui.PlayerSetupScene;
import game.adventurer.ui.SplashScreen;
import game.adventurer.util.ScreenUtils;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class AdventurerGameApp extends Application {


  private SharedSize sharedSize;
  private Stage primaryStage;
  private static final float INITIAL_SCREEN_SIZE_RATIO = 0.8f;
  private static final String APP_NAME = "Adventurer Game";
  public static final HighScoreManager highScoreManager = new HighScoreManager();
  private static ConfigurableApplicationContext springContext;
  private LocalizedMessageService localizedMessageService = LocalizedMessageService.getInstance();
  private LocalizationService localizationService;


  public static void launchWithSpringContext(String[] args, ConfigurableApplicationContext context) {
    springContext = context;
    launch(args);
  }

  @Override
  public void init() {
    // AdventurerGameApp needs an empty constructor
    // This gets the necessary services from Spring context
    localizationService = springContext.getBean(LocalizationService.class);
    // Init other services here as needed
  }


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
      PlayerSetupScene playerSetupScene = new PlayerSetupScene(sharedSize, highScoreManager, localizationService);
      playerSetupScene.setOnStartGame(this::startGame);

      // Cross-fade transition to playerSetupScene
      crossFadeTransition(primaryStage, playerSetupScene, Duration.seconds(1));
    });
    initialDelay.play();
  }

  private void showPlayerSetup() {
    PlayerSetupScene playerSetupScene = new PlayerSetupScene(sharedSize, highScoreManager, localizationService);
    playerSetupScene.setOnStartGame(this::startGame);
    primaryStage.setScene(playerSetupScene);
  }

  private void startGame(String playerName, MapSize mapSize, DifficultyLevel difficultyLevel) {
    try {
      GameMap gameMap = MapGenerator.generateMap(mapSize.getSize(), mapSize.getSize(), playerName);
      MainGameScene mainGameScene = MainGameScene.create(gameMap, sharedSize, difficultyLevel, localizationService);
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
      // Initialize the alert
      Alert alert = alertInitializer(getClass(), AlertType.ERROR, "Critical Error", "Error: " + e.getMessage(), "alert",
          "/assets/icons/crane-et-os.png", true);

      // Defines alert content (OK button is created by the AlertType.ERROR)
      alert.setContentText("The game cannot continue due to an invalid state. Game will close upon closing this alert.");

      alert.showAndWait();
      quitGame();
    });
  }


}
