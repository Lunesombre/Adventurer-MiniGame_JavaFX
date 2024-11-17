package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.GameMap;
import game.adventurer.model.Score;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.TreasureItem;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.ui.common.ScoreBoard;
import java.time.LocalDateTime;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndGameScene extends BaseScene {

  private final GameMap gameMap;
  private final int movesCount;
  private final DifficultyLevel difficultyLevel;
  private final double initialDistanceToTreasure;

  public final String restartButtonLabel;
  public final String quitButtonLabel;
  private final Button quitButton;
  private final Button restartButton;
  @Getter
  private Score newScore;
  private final HighScoreManager highScoreManager;
  private ScoreBoard scoreBoard;
  private Text toggleScoreBoardText;

  private final LocalizedMessageService localizedMessageService = LocalizedMessageService.getInstance();

  public EndGameScene(SharedSize sharedSize, GameMap gameMap, int movesCount, DifficultyLevel difficultyLevel, int initialDistanceToTreasure,
      HighScoreManager highScoreManager) {
    super(new StackPane(), sharedSize);
    this.gameMap = gameMap;
    this.movesCount = movesCount;
    this.difficultyLevel = difficultyLevel;
    this.initialDistanceToTreasure = initialDistanceToTreasure;
    this.highScoreManager = highScoreManager;
    this.restartButtonLabel = localizedMessageService.getMessage("button.replay");
    this.quitButtonLabel = localizedMessageService.getMessage("button.exit");
    quitButton = new Button(quitButtonLabel);
    restartButton = new Button(restartButtonLabel);
    initialize();

  }


  @Override
  protected void initialize() {
    StackPane root = (StackPane) getRoot();
    root.setAlignment(Pos.CENTER);

    VBox mainContent = new VBox(20);
    mainContent.setAlignment(Pos.CENTER);

    VBox scoreBox = new VBox(); // Define a VBox for the score info
    scoreBox.setAlignment(Pos.CENTER);
    scoreBox.setMaxWidth(root.getWidth() * 0.75);
    scoreBox.setPadding(new Insets(40.0));
    BorderStroke stroke = new BorderStroke(Paint.valueOf("blue"), BorderStrokeStyle.SOLID, new CornerRadii(25.0), BorderWidths.DEFAULT);
    Border border = new Border(stroke);
    scoreBox.setBorder(border);
    Background backgroundScoreBox = new Background(new BackgroundFill(
        Paint.valueOf("#FFFFFF66"),
        new CornerRadii(25.0),
        Insets.EMPTY
    ));
    scoreBox.setBackground(backgroundScoreBox);

    int calculatedScore = calculateScore();
    newScore = new Score(gameMap.getAdventurer().getName(), calculatedScore, LocalDateTime.now(), movesCount, difficultyLevel);
    highScoreManager.addScore(newScore);

    Label congratsLabel = new Label(localizedMessageService.getMessage("endgame.greeting", gameMap.getAdventurer().getName()));
    congratsLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 40px; -fx-font-weight: bold;");
    congratsLabel.setPadding(new Insets(20.0));

    Label movesLabel = new Label(localizedMessageService.getMessage("endgame.moveCount", movesCount));
    movesLabel.setPadding(new Insets(20.0));

    TextFlow treasureFlow = getTreasureFlow(gameMap.getTreasure().getItem());

    Label scoreLabel = new Label(localizedMessageService.getMessage("endgame.score", calculatedScore));
    scoreLabel.setPadding(new Insets(20.0));

    HBox buttonsBox = new HBox(20);
    buttonsBox.setAlignment(Pos.CENTER);
    quitButton.getStyleClass().add("quit-button");
    buttonsBox.getChildren().addAll(restartButton, quitButton);

    //ScoreBoard
    scoreBoard = new ScoreBoard(highScoreManager, sharedSize.getWidth());
    // scoreBoardText
    toggleScoreBoardText = new Text(localizedMessageService.getMessage("highScores.show"));
    toggleScoreBoardText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;");
    toggleScoreBoardText.setOnMouseClicked(this::toggleScoreBoard);
    toggleScoreBoardText.setOnMouseEntered(e -> toggleScoreBoardText.setStyle("-fx-fill: #aca29c; -fx-font-size: 14px; -fx-cursor: hand;"));
    toggleScoreBoardText.setOnMouseExited(e -> toggleScoreBoardText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;"));

    scoreBox.getChildren().addAll(congratsLabel, movesLabel, treasureFlow, scoreLabel, buttonsBox);
    mainContent.getChildren().addAll(scoreBox, toggleScoreBoardText);
    root.getChildren().addAll(mainContent, scoreBoard);
    scoreBoard.updateSize(sharedSize.getWidth(), sharedSize.getHeight()); // updateSize and position,
    // done after the scoreBoard is added to root to prevent errors.
    StackPane.setAlignment(scoreBoard, Pos.CENTER_RIGHT);
    // Hide the ScoreBoard by clicking outside it
    root.setOnMouseClicked(e -> {
      if (scoreBoard.isShowing() && !scoreBoard.getBoundsInParent().contains(e.getX(), e.getY())) {
        toggleScoreBoard(e);
      }
    });

  }

  @Override
  protected void onSizeChanged(double width, double height) {
    scoreBoard.updateSize(width, height);
    scoreBoard.updateStyles(width);
    scoreBoard.layout();
    scoreBoard.requestLayout();
  }

  private void toggleScoreBoard(MouseEvent event) {
    scoreBoard.setVisible(true);
    scoreBoard.toggleDisplay();
    if (scoreBoard.isShowing()) {
      toggleScoreBoardText.setText(localizedMessageService.getMessage("highScores.hide"));
    } else {
      toggleScoreBoardText.setText(localizedMessageService.getMessage("highScores.show"));
    }
    event.consume();
  }

  private TextFlow getTreasureFlow(TreasureItem treasureItem) {
    String treasureName = localizedMessageService.getMessage(treasureItem.getNameKey());
    String treasureGlowColor = treasureItem.getGlowColor();
    Text prefixText = new Text(localizedMessageService.getMessage("endgame.treasure"));
    Text treasureText = new Text(treasureName);
    treasureText.setStyle("-fx-font-weight: bold; -fx-fill: " + treasureGlowColor + ";");
    Bloom bloom = new Bloom();
    bloom.setThreshold(0.5);
    treasureText.setEffect(bloom);
    TextFlow treasureFlow = new TextFlow(prefixText, treasureText);
    treasureFlow.setPadding(new Insets(20.0));
    treasureFlow.setTextAlignment(TextAlignment.CENTER);
    return treasureFlow;
  }

  private int calculateScore() {
    int mapSize = gameMap.getMapHeight() * gameMap.getMapWidth();
    int terrainObstacleCount = countTerrainObstacles();
    double obstaclePercentage = (double) terrainObstacleCount / mapSize;
    int playerHealth = gameMap.getAdventurer().getHealth();

    double difficultyModifier;
    switch (difficultyLevel) {
      case DifficultyLevel.EASY -> difficultyModifier = 1.0;
      case DifficultyLevel.HARD -> difficultyModifier = 2.0;
      default -> difficultyModifier = 1.5;
    }
    double treasureDistance = initialDistanceToTreasure / 20;
    return (int) (1000 * ((double) mapSize / 10 / movesCount) * (1 + obstaclePercentage) * (playerHealth / 100.0) * difficultyModifier
        * treasureDistance);
  }


  public void setOnRestartGame(Runnable action) {
    restartButton.setOnAction(e -> action.run());
  }

  public void setOnQuitGame(Runnable action) {
    quitButton.setOnAction(e -> action.run());
  }


  private int countTerrainObstacles() {
    int count = 0;
    for (int y = 0; y < gameMap.getMapHeight(); y++) {
      for (int x = 0; x < gameMap.getMapWidth(); x++) {
        if (gameMap.getTileTypeAt(x, y) != Type.PATH) {
          count++;
        }
      }
    }
    return count;
  }

}
