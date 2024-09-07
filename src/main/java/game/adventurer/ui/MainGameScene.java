package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.Adventurer;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.ui.common.BaseScene;
import javafx.animation.FadeTransition;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGameScene extends BaseScene {

  private static final int PADDING = 20;
  public static final Logger LOG = LoggerFactory.getLogger(MainGameScene.class);
  private GameMap gameMap;
  private Pane gamePane;
  private Circle adventurerCircle;
  private Circle treasureCircle;
  private double tileSize;
  private double xOffset;
  private double yOffset;
  @Getter
  private int movesCount;
  @Setter
  private Runnable onGameEnd;
  @Setter
  private Runnable onGameOver;

  private final Rectangle effectOverlay = new Rectangle();


  //V3 pattern Factory
  private MainGameScene(StackPane root, SharedSize sharedSize) {
    super(root, sharedSize);
  }

  public static MainGameScene create(GameMap gameMap, SharedSize sharedSize) {
    StackPane root = new StackPane();
    MainGameScene scene = new MainGameScene(root, sharedSize);
    scene.gameMap = gameMap;
    scene.initialize();
    return scene;
  }

  @Override
  protected void initialize() {
    double windowWidth = sharedSize.getWidth();
    double windowHeight = sharedSize.getHeight();

    StackPane root = (StackPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);

    gamePane = new Pane();
    root.getChildren().add(gamePane);
    root.setStyle("-fx-background-color: #403f3f;");

    // Add a listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> {
      handleResize();
      effectOverlay.setWidth(newVal.doubleValue());
      updateSize();
    });
    heightProperty().addListener((obs, oldVal, newVal) -> {
      handleResize();
      effectOverlay.setHeight(newVal.doubleValue());
      updateSize();
    });

    // Set handler for keyboard events
    setOnKeyPressed(this::handleKeyPress);

    movesCount = 0;

    effectOverlay.setWidth(windowWidth);
    effectOverlay.setHeight(windowHeight);
    effectOverlay.setVisible(false);
    root.getChildren().add(effectOverlay); // Must be the last children added to appear above all other children.

    // Initialize the map
    initializeGameMap();
  }

  private void handleKeyPress(KeyEvent event) {
    MoveResult moveResult = MoveResult.BLOCKED;
    switch (event.getCode()) {
      case UP -> {
        moveResult = gameMap.moveAdventurer(0, -1);
        movesCount++;
      }
      case DOWN -> {
        moveResult = gameMap.moveAdventurer(0, 1);
        movesCount++;
      }
      case LEFT -> {
        moveResult = gameMap.moveAdventurer(-1, 0);
        movesCount++;
      }
      case RIGHT -> {
        moveResult = gameMap.moveAdventurer(1, 0);
        movesCount++;
      }
      default -> handleOtherKeys(event);
    }
    switch (moveResult) {
      case MOVED -> {
        movesCount++;
        if (isTreasureCollected()) {
          if (onGameEnd != null) {
            onGameEnd.run();
          } else {
            LOG.warn("onGameEnd is null");
          }
        } else {
          updateGameView();
        }
      }
      case WOUNDED -> {
        showDamageEffect();
        if (isAdventurerDead(gameMap.getAdventurer())) {
          if (onGameOver != null) {
            onGameOver.run();
          } else {
            LOG.warn("onGameOver is null");
          }
        }

//        updateGameView(); // Update view to reflect health change
      }
      case OUT_OF_BOUNDS -> showOutOfBoundsEffect();
      case BLOCKED -> LOG.info("BLOCKED");
      // Do nothing for blocked moves
    }
  }

  private void handleOtherKeys(KeyEvent event) {
    LOG.warn("Unhandled key press: {}", event.getCode());
  }

  private void updateGameView() {
    int tileX = gameMap.getAdventurer().getTileX();
    int tileY = gameMap.getAdventurer().getTileY();

    // Calculate the offset to center the map
    xOffset = (getWidth() - gameMap.getMapWidth() * tileSize) / 2;
    yOffset = (getHeight() - gameMap.getMapHeight() * tileSize) / 2;

    // Update the Adventurer's position on the map
    double advX = xOffset + (tileX + 0.5) * tileSize;
    double advY = yOffset + (tileY + 0.5) * tileSize;

    LOG.debug("Scene dimensions: {} x {}", getWidth(), getHeight());
    LOG.debug("Map dimensions: {} x {}", gameMap.getMapWidth(), gameMap.getMapHeight());
    LOG.debug("Tile size: {}", tileSize);
    LOG.debug("Offsets: x={}, y={}", xOffset, yOffset);

    adventurerCircle.setCenterX(advX);
    adventurerCircle.setCenterY(advY);

    LOG.debug("Adventurer position: Tile({}, {}), Pixel({}, {})",
        gameMap.getAdventurer().getTileX(),
        gameMap.getAdventurer().getTileY(),
        advX, advY);
  }

  private void handleResize() {
    int mapWidth = gameMap.getMapWidth();
    int mapHeight = gameMap.getMapHeight();
    calculateMapDimensions(mapWidth, mapHeight);

    // Update size and position of each tile
    for (int y = 0; y < mapHeight; y++) {
      for (int x = 0; x < mapWidth; x++) {
        Rectangle rect = (Rectangle) gamePane.getChildren().get(y * mapWidth + x);
        rect.setX(xOffset + x * tileSize);
        rect.setY(yOffset + y * tileSize);
        rect.setWidth(tileSize);
        rect.setHeight(tileSize);
      }
    }

    // Update size and position of the adventurer
    adventurerCircle.setRadius(tileSize / 2);
    updateGameView();

    // Update size and position of treasure
    treasureCircle.setRadius(tileSize / 2);
    treasureCircle.setCenterX(xOffset + (gameMap.getTreasure().getTileX() + 0.5) * tileSize);
    treasureCircle.setCenterY(yOffset + (gameMap.getTreasure().getTileY() + 0.5) * tileSize);

    // Forcing a visual update
    gamePane.requestLayout();
  }


  private void initializeGameMap() {
    gamePane.getChildren().clear();

    int mapWidth = gameMap.getMapWidth();
    int mapHeight = gameMap.getMapHeight();
    calculateMapDimensions(mapWidth, mapHeight);

    // Create and position tiles representation
    for (int y = 0; y < mapHeight; y++) {
      for (int x = 0; x < mapWidth; x++) {
        Tile tile = gameMap.getGrid()[y][x];
        Rectangle rect = new Rectangle(tileSize, tileSize);
        rect.setFill(tile.getType() == Tile.Type.PATH ? Color.web("#B87065") : Color.web("#206600"));
        rect.setX(xOffset + x * tileSize);
        rect.setY(yOffset + y * tileSize);
        rect.setStroke(Color.web("#4a5246")); // grey-greenish tile border
        rect.setStrokeWidth(0.5);
        gamePane.getChildren().add(rect);
      }
    }

    // Adding treasure
    treasureCircle = new Circle(tileSize / 2, Color.GOLD);
    treasureCircle.setCenterX(xOffset + (gameMap.getTreasure().getTileX() + 0.5) * tileSize);
    treasureCircle.setCenterY(yOffset + (gameMap.getTreasure().getTileY() + 0.5) * tileSize);
    gamePane.getChildren().add(treasureCircle);

    // Adding adventurer
    adventurerCircle = new Circle(tileSize / 2, Color.BLUE);
    double advX = xOffset + (gameMap.getAdventurer().getTileX() + 0.5) * tileSize;
    double advY = yOffset + (gameMap.getAdventurer().getTileY() + 0.5) * tileSize;
    adventurerCircle.setCenterX(advX);
    adventurerCircle.setCenterY(advY);
    gamePane.getChildren().add(adventurerCircle);

    updateGameView();
  }

  private void calculateMapDimensions(int mapWidth, int mapHeight) {
    double windowWidth = getWidth();
    double windowHeight = getHeight();

    // Calculate tile size to adapt to window
    tileSize = Math.min(
        (windowWidth - 2 * PADDING) / mapWidth,
        (windowHeight - 2 * PADDING) / mapHeight
    );

    // Calculate the offset to center the map
    xOffset = (windowWidth - mapWidth * tileSize) / 2;
    yOffset = (windowHeight - mapHeight * tileSize) / 2;
  }

  private boolean isTreasureCollected() {
    return gameMap.getAdventurer().getTileX() == gameMap.getTreasure().getTileX() &&
        gameMap.getAdventurer().getTileY() == gameMap.getTreasure().getTileY();
  }

  private void showDamageEffect() {
    effectOverlay.setFill(Color.rgb(255, 0, 0, 0.5));
    effectOverlay.setVisible(true);

    FadeTransition fade = new FadeTransition(Duration.millis(200), effectOverlay);
    fade.setFromValue(0.5);
    fade.setToValue(0.0);

    fade.setOnFinished(event -> effectOverlay.setVisible(false));
    fade.play();
  }

  private void showOutOfBoundsEffect() {
    effectOverlay.setFill(Color.rgb(70, 70, 245, 0.5));
    effectOverlay.setVisible(true);

    FadeTransition fade = new FadeTransition(Duration.millis(200), effectOverlay);
    fade.setFromValue(0.5);
    fade.setToValue(0.0);

    fade.setOnFinished(event -> effectOverlay.setVisible(false));
    fade.play();
  }

  private boolean isAdventurerDead(Adventurer adventurer) {
    return adventurer.getHealth() <= 0;
  }

}



