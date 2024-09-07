package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.TreasureItem;
import game.adventurer.ui.common.BaseScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class EndGameScene extends BaseScene {

  private final GameMap gameMap;
  private final int movesCount;

  public static final String RESTART_BUTTON_LABEL = "Rejouer";
  public static final String QUIT_BUTTON_LABEL = "Quitter jeu";
  private final Button quitButton = new Button(QUIT_BUTTON_LABEL);
  private final Button restartButton = new Button(RESTART_BUTTON_LABEL);

  public EndGameScene(SharedSize sharedSize, GameMap gameMap, int movesCount) {
    super(new VBox(20), sharedSize);
    this.gameMap = gameMap;
    this.movesCount = movesCount;

    initialize();
  }


  @Override
  protected void initialize() {
    VBox root = (VBox) getRoot();
    root.setAlignment(Pos.CENTER);

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

    Label congratsLabel = new Label("Félicitations " + gameMap.getAdventurer().getName() + " !");
    congratsLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 40px; -fx-font-weight: bold;");
    congratsLabel.setPadding(new Insets(20.0));

    Label movesLabel = new Label("Nombre de mouvements : " + movesCount);
    movesLabel.setPadding(new Insets(20.0));

    TextFlow treasureFlow = getTreasureFlow(gameMap.getTreasure().getItem());

    Label scoreLabel = new Label("Score : " + calculateScore());
    scoreLabel.setPadding(new Insets(20.0));

    HBox buttonsBox = new HBox(20);
    buttonsBox.setAlignment(Pos.CENTER);
    quitButton.getStyleClass().add("quit-button");
    buttonsBox.getChildren().addAll(restartButton, quitButton);

    scoreBox.getChildren().addAll(congratsLabel, movesLabel, treasureFlow, scoreLabel, buttonsBox);
    root.getChildren().addAll(scoreBox);

    // Adding listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());
  }

  private TextFlow getTreasureFlow(TreasureItem treasureItem) {
    String treasureName = treasureItem.getName();
    String treasureGlowColor = treasureItem.getGlowColor();
    Text prefixText = new Text("Trésor trouvé : ");
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
    return (int) (1000 * (mapSize / 10 / (double) movesCount) * (1 + obstaclePercentage) * (playerHealth / 100.0));
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
        if (gameMap.getGrid()[y][x].getType() != Tile.Type.PATH) {
          count++;
        }
      }
    }
    return count;
  }

}
