package game.adventurer.ui;

import static javafx.scene.paint.Color.rgb;

import game.adventurer.common.SharedSize;
import game.adventurer.controller.RightPanelController;
import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.Adventurer;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.Treasure;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.util.PathfindingUtil;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGameScene extends BaseScene {

  private static final int PADDING = 40;
  public static final String TREASURE_IS_CLOSE = "Le trésor est proche !";
  public static final String FAILED_INITIAL_DISTANCE = "Failed to calculate initial distance to treasure. This should never happen.";
  private static final double MESSAGE_BOX_MIN_WIDTH = 230.0;
  private static final double MESSAGE_BOX_MIN_HEIGHT = 250.0;
  public static final Logger LOG = LoggerFactory.getLogger(MainGameScene.class);
  private int initialHealth;
  private GameMap gameMap;
  private Pane mapView;
  private VBox leftPanel;
  private VBox rightPanel;
  private final RightPanelController rightPanelController = new RightPanelController(PADDING, MESSAGE_BOX_MIN_WIDTH);
  @Getter
  private DifficultyLevel difficultyLevel;
  @Getter
  private int initialDistanceToTreasure;
  private boolean hasSentMessage = false;

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
  private MainGameScene(BorderPane root, SharedSize sharedSize) {
    super(root, sharedSize);
  }

  public static MainGameScene create(GameMap gameMap, SharedSize sharedSize, DifficultyLevel difficultyLevel) throws InvalidGameStateException {
    BorderPane root = new BorderPane();
    MainGameScene scene = new MainGameScene(root, sharedSize);
    scene.gameMap = gameMap;
    scene.difficultyLevel = difficultyLevel;
    scene.initialize();
    return scene;
  }

  @Override
  protected void initialize() throws InvalidGameStateException {
    initialHealth = gameMap.getAdventurer().getHealth();
    double windowWidth = sharedSize.getWidth();
    double windowHeight = sharedSize.getHeight();
    initialDistanceToTreasure = calculateMovesToTreasure();
    if (initialDistanceToTreasure == -1) {
      LOG.error(FAILED_INITIAL_DISTANCE);
      throw new InvalidGameStateException(FAILED_INITIAL_DISTANCE);
    }

    BorderPane root = (BorderPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);

    initializeGameMap();
    createLeftPanel();
    createRightPanel();

    double mapWidth = windowWidth * 0.6; // 60% for the map
    double sideWidth = windowWidth * 0.25; // 25% for each side panel

    leftPanel.setPrefWidth(sideWidth);
    rightPanel.setPrefWidth(sideWidth);
    mapView.setPrefWidth(mapWidth);

    Image backgroundImage = new Image("/assets/images/2201_w032_n003_321b_p1_321.jpg");
    BackgroundImage background = new BackgroundImage(
        backgroundImage,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.DEFAULT,
        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
    );
    leftPanel.setBackground(new Background(background));
    rightPanel.setBackground(new Background(background));

    Image backgroundMapImage = new Image("/assets/images/2201_w032_n003_321b_p1_321c.jpg");
    BackgroundImage backgroundMap = new BackgroundImage(
        backgroundMapImage,
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER,
        new BackgroundSize(100, 100, true, true, false, true)
    );
    mapView.setBackground(new Background(backgroundMap));

    root.setLeft(leftPanel);
    root.setRight(rightPanel);
    root.setCenter(mapView);

    // Set handler for keyboard events
    setOnKeyPressed(this::handleKeyPress);

    movesCount = 0;

    effectOverlay.setWidth(windowWidth);
    effectOverlay.setHeight(windowHeight);
    effectOverlay.setVisible(false);
    root.getChildren().add(effectOverlay); // Must be the last children added to appear above all other children.

    setupResizeListeners();
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
          if (!treasureCircle.isVisible()) {
            updateTreasureVisibility();
          }
          updateGameView();
        }
      }
      case WOUNDED -> {
        showDamageEffect();
        rightPanelController.addMessage(gameMap.getWoundsList().getLast().getWoundMessage());
        if (isAdventurerDead(gameMap.getAdventurer())) {
          if (onGameOver != null) {
            onGameOver.run();
          } else {
            LOG.warn("onGameOver is null");
          }
        }
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

    // Update the Adventurer's position on the map
    double advX = xOffset + (tileX + 0.5) * tileSize;
    double advY = yOffset + (tileY + 0.5) * tileSize;

    LOG.debug("Scene dimensions: {} x {}", getWidth(), getHeight());
    LOG.debug("Map dimensions: {} x {}", gameMap.getMapWidth(), gameMap.getMapHeight());
    LOG.debug("Tile size: {}", tileSize);
    LOG.debug("Offsets: x={}, y={}", xOffset, yOffset);

    // Update size and position of the adventurer
    adventurerCircle.setCenterX(advX);
    adventurerCircle.setCenterY(advY);
    adventurerCircle.setRadius(tileSize / 2);

    // Update size and position of treasure
    treasureCircle.setRadius(tileSize / 2);
    treasureCircle.setCenterX(xOffset + (gameMap.getTreasure().getTileX() + 0.5) * tileSize);
    treasureCircle.setCenterY(yOffset + (gameMap.getTreasure().getTileY() + 0.5) * tileSize);

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
        Rectangle rect = (Rectangle) mapView.getChildren().get(y * mapWidth + x);
        rect.setX(xOffset + x * tileSize);
        rect.setY(yOffset + y * tileSize);
        rect.setWidth(tileSize);
        rect.setHeight(tileSize);
      }
    }

    updateGameView();

//    // Forcing a visual update
    mapView.requestLayout();
  }


  private void initializeGameMap() {
    mapView = new Pane();

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
        mapView.getChildren().add(rect);
      }
    }

    // Adding treasure
    treasureCircle = new Circle(tileSize / 2, Color.GOLD);
    treasureCircle.setCenterX(xOffset + (gameMap.getTreasure().getTileX() + 0.5) * tileSize);
    treasureCircle.setCenterY(yOffset + (gameMap.getTreasure().getTileY() + 0.5) * tileSize);
    if (difficultyLevel != DifficultyLevel.EASY) {
      treasureCircle.setVisible(false);
    }
    mapView.getChildren().add(treasureCircle);

    // Adding adventurer
    adventurerCircle = new Circle(tileSize / 2, Color.BLUE);
    double advX = xOffset + (gameMap.getAdventurer().getTileX() + 0.5) * tileSize;
    double advY = yOffset + (gameMap.getAdventurer().getTileY() + 0.5) * tileSize;
    adventurerCircle.setCenterX(advX);
    adventurerCircle.setCenterY(advY);
    mapView.getChildren().add(adventurerCircle);

    handleResize();
  }

  private void calculateMapDimensions(int mapWidth, int mapHeight) {
    double availableWidth = getWidth() / 2;
    double availableHeight = getHeight();

    LOG.debug("availableWidth : {}, availableHeight : {}", availableWidth, availableHeight);

    // Calculate tile size to adapt to window
    tileSize = Math.min(
        (availableWidth - 2 * PADDING) / mapWidth,
        (availableHeight - 2 * PADDING) / mapHeight
    );

    // Calculate the offset to center the map
    xOffset = (availableWidth - mapWidth * tileSize) / 2;
    yOffset = (availableHeight - mapHeight * tileSize) / 2;
  }

  private boolean isTreasureCollected() {
    return gameMap.getAdventurer().getTileX() == gameMap.getTreasure().getTileX() &&
        gameMap.getAdventurer().getTileY() == gameMap.getTreasure().getTileY();
  }

  private void showDamageEffect() {
    effectOverlay.setFill(rgb(255, 0, 0, 0.5));
    effectOverlay.setVisible(true);

    FadeTransition fade = new FadeTransition(Duration.millis(200), effectOverlay);
    fade.setFromValue(0.5);
    fade.setToValue(0.0);

    fade.setOnFinished(event -> effectOverlay.setVisible(false));
    fade.play();
  }

  private void showOutOfBoundsEffect() {
    effectOverlay.setFill(rgb(70, 70, 245, 0.5));
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

  private void createLeftPanel() {
    leftPanel = new VBox(10);
    leftPanel.setPadding(new Insets(10));

    HBox playerNameBox = new HBox();
    Label playerNameLabel = new Label(gameMap.getAdventurer().getName());
    playerNameLabel.setStyle("-fx-font-size: 42px; -fx-text-fill: #ffc400");
    playerNameLabel.getStyleClass().add("medieval-font");
    playerNameBox.setAlignment(Pos.CENTER);
    playerNameBox.getChildren().add(playerNameLabel);
    playerNameBox.setBackground(new Background(new BackgroundFill(Color.rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));

    HBox imageBox = new HBox();
    imageBox.setAlignment(Pos.CENTER);
    Image charImage = new Image("assets/images/placeholderHero.jpg");
    ImageView playerImage = new ImageView(charImage); // Character image (placeholder for now)
    playerImage.setPreserveRatio(true);
    playerImage.fitWidthProperty().bind(imageBox.widthProperty().multiply(0.8));

    // Creates a clip to round the corners.
    Rectangle clip = new Rectangle();
    clip.widthProperty().bind(imageBox.widthProperty().multiply(0.8));
    clip.heightProperty().bind(imageBox.widthProperty().multiply(0.8)); // Not a mistake. Based on width as it keeps its aspect ratio 1:1.
    clip.setArcWidth(25);
    clip.setArcHeight(25);
    playerImage.setClip(clip);

    // Create a Group containing the clipped image
    Group clippedImage = new Group(playerImage);

    // Applying a shadow effect to simulate a broder, then applying to the group.
    // Group is necessary, if the effect is applied directly on the ImageView, it is applied before the clip and thus... clipped!
    DropShadow borderGlow = new DropShadow();
    borderGlow.setOffsetY(0f);
    borderGlow.setOffsetX(0f);
    borderGlow.setColor(Color.rgb(195, 130, 0));
    borderGlow.setRadius(15.0);
    borderGlow.setSpread(0.9);
    borderGlow.setWidth(30);
    borderGlow.setHeight(30);
    clippedImage.setEffect(borderGlow);

    imageBox.getChildren().add(clippedImage);

    VBox statsBox = new VBox();
    statsBox.setAlignment(Pos.TOP_CENTER);
    Label statsLabel = new Label("Stats");
    statsLabel.setStyle("-fx-font-size: 30px; -fx-text-fill: #ffc400");
    statsLabel.getStyleClass().add("medieval-font");
    statsLabel.setPadding(new Insets(16));
    statsBox.setBackground(new Background(new BackgroundFill(Color.rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));

    HBox healthBox = new HBox(20);
    healthBox.setAlignment(Pos.CENTER);
    ImageView heart = new ImageView("assets/icons/heartR.png");
    heart.fitWidthProperty().bind(leftPanel.widthProperty().multiply(0.08));
    heart.setPreserveRatio(true);
    Text playerHealth = new Text();
    playerHealth.setStyle("-fx-font-weight: bold");
    playerHealth.setFill(Color.rgb(0, 120, 0));
    Bloom bloom = new Bloom(0.1);
    playerHealth.setEffect(bloom);
    playerHealth.textProperty().bind(gameMap.getAdventurer().healthProperty().asString());
    Text initialHealthText = new Text("/ " + gameMap.getAdventurer().getHealth());
    healthBox.getChildren().addAll(heart, playerHealth, initialHealthText);

    statsBox.getChildren().addAll(statsLabel, healthBox);

    leftPanel.getChildren().addAll(playerNameBox, imageBox, statsBox);

    // Set grow priority for the elements to distribute the height
    VBox.setVgrow(playerNameBox, Priority.ALWAYS);
    VBox.setVgrow(imageBox, Priority.ALWAYS);
    VBox.setVgrow(statsBox, Priority.ALWAYS);

    // Add a resize listener to adjust the size of each element
    leftPanel.heightProperty().addListener((obs, oldHeight, newHeight) -> {
      double totalHeight = newHeight.doubleValue();

      // Set the preferred heights based on the percentages
      playerNameBox.setPrefHeight(totalHeight * 0.20);  // 20% height
      imageBox.setPrefHeight(totalHeight * 0.40);       // 40% height
      statsBox.setPrefHeight(totalHeight * 0.40);       // 40% height
    });
    // Add a listener to change color of the health text
    healthChangeListener(playerHealth);
  }

  private void createRightPanel() {
    rightPanel = new VBox(10);
    rightPanel.setPadding(new Insets(10));

    VBox optionsBox = new VBox(5);
    optionsBox.setAlignment(Pos.TOP_RIGHT);
    Text optionsPlaceholder = new Text("Options placeholder");
    optionsBox.getChildren().add(optionsPlaceholder);
    VBox messagesBox = new VBox(5);
    messagesBox.setPadding(new Insets(20));
    messagesBox.setBackground(new Background(new BackgroundFill(Color.rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));
    messagesBox.setMinSize(MESSAGE_BOX_MIN_WIDTH, MESSAGE_BOX_MIN_HEIGHT);
    // Initialize the messages' controller
    rightPanelController.initMessagesBox(messagesBox);
    // Set the first message.
    String message = "Bonne quête, " + gameMap.getAdventurer().getName() + " !";
    if (difficultyLevel != DifficultyLevel.EASY) {
      message += "\n" + getTreasureHint(gameMap.getAdventurer(), gameMap.getTreasure());
    }
    rightPanelController.addMessage(message);

    VBox contactBox = new VBox(5);
    contactBox.setAlignment(Pos.BOTTOM_RIGHT);
    Text contactPH = new Text("Contact PH");
    Text legalPH = new Text("Legal PH");
    Text aboutUsPH = new Text("About Us PH");
    contactBox.getChildren().addAll(aboutUsPH, contactPH, legalPH);

    rightPanel.getChildren().addAll(optionsBox, messagesBox, contactBox);

    // Set grow priority for the elements to distribute the height
    VBox.setVgrow(optionsBox, Priority.ALWAYS);
    VBox.setVgrow(messagesBox, Priority.ALWAYS);
    VBox.setVgrow(contactBox, Priority.ALWAYS);

    // Add a resize listener to adjust the size of each element
    rightPanel.heightProperty().addListener((obs, oldHeight, newHeight) -> {
      double totalHeight = newHeight.doubleValue();

      // Set the preferred heights based on the percentages
      optionsBox.setPrefHeight(totalHeight * 0.20);  // 20% height
      messagesBox.setPrefHeight(totalHeight * 0.60);       // 60% height
      contactBox.setPrefHeight(totalHeight * 0.20);       // 20% height
    });
  }

  private void setupResizeListeners() {
    widthProperty().addListener((obs, oldVal, newVal) -> {
      double totalWidth = newVal.doubleValue();
      double mapWidth = totalWidth * 0.5; // 50% for the map
      double sideWidth = totalWidth * 0.25; // 25% for each side panel

      leftPanel.setPrefWidth(sideWidth);
      rightPanel.setPrefWidth(sideWidth);
      mapView.setPrefWidth(mapWidth);
      effectOverlay.setWidth(newVal.doubleValue());

      handleResize();
      updateSize();
    });

    heightProperty().addListener((obs, oldVal, newVal) -> {
      leftPanel.setPrefHeight(newVal.doubleValue());
      rightPanel.setPrefHeight(newVal.doubleValue());
      mapView.setPrefHeight(newVal.doubleValue());
      effectOverlay.setHeight(newVal.doubleValue());

      handleResize();
      updateSize();
    });
  }

  private void healthChangeListener(Text playerHealth) {
    // Add aListener to check on health's changes.
    gameMap.getAdventurer().healthProperty().addListener((observable, oldValue, newValue) -> {
      int currentHealth = newValue.intValue();
      double healthPercentage = (double) currentHealth / initialHealth;

      if (healthPercentage > 0.7) {
        playerHealth.setFill(Color.rgb(0, 120, 0)); // Green : > 70%
      } else if (healthPercentage > 0.5) {
        playerHealth.setFill(Color.rgb(175, 160, 0)); // Yellow : > 50%
      } else if (healthPercentage > 0.3) {
        playerHealth.setFill(Color.rgb(190, 115, 0)); // Orange : > 30%
      } else {
        playerHealth.setFill(Color.rgb(195, 0, 0)); // Red : <= 30%
      }
    });
  }

  private int calculateMovesToTreasure() {
    return PathfindingUtil.shortestPath(
        gameMap.getAdventurer().getTileX(), gameMap.getAdventurer().getTileY(),
        gameMap.getTreasure().getTileX(), gameMap.getTreasure().getTileY(),
        gameMap.getMapWidth(), gameMap.getMapHeight(),
        (x, y) -> gameMap.getTileTypeAt(x, y) == Type.PATH
    );
  }

  private void updateTreasureVisibility() {

    switch (difficultyLevel) {
      case DifficultyLevel.EASY -> treasureCircle.setVisible(true);
      case DifficultyLevel.NORMAL -> {
        int movesToTreasure = calculateMovesToTreasure();
        if (movesToTreasure <= 3 && !hasSentMessage) {
          rightPanelController.addMessage(TREASURE_IS_CLOSE);
          hasSentMessage = true;
        }
        if (movesToTreasure > 3 && hasSentMessage) {
          hasSentMessage = false;
        }
        treasureCircle.setVisible(movesToTreasure <= 2);
      }
      case DifficultyLevel.HARD -> {
        int movesToTreasure = calculateMovesToTreasure();
        if (movesToTreasure <= 2 && !hasSentMessage) {
          rightPanelController.addMessage(TREASURE_IS_CLOSE);
          hasSentMessage = true;
        }
        if (movesToTreasure > 2 && hasSentMessage) {
          hasSentMessage = false;
        }
        treasureCircle.setVisible(movesToTreasure <= 1);
      }
      default -> treasureCircle.setVisible(false);
    }
  }

  private String getTreasureHint(Adventurer adventurer, Treasure treasure) {
    final String direction = getDirectionString(adventurer, treasure);
    String distance = "";
    if ((gameMap.getMapWidth() <= 11 && initialDistanceToTreasure >= 10)
        || (gameMap.getMapWidth() > 11 && gameMap.getMapWidth() < 21 && initialDistanceToTreasure >= 20)
        || (gameMap.getMapWidth() >= 21 && initialDistanceToTreasure >= 30)) {
      distance = "loin ";
    }
    return String.format("Le trésor devrait être quelque part %svers %s.", distance, direction);
  }

  private static String getDirectionString(Adventurer adventurer, Treasure treasure) {
    int dx = treasure.getTileX() - adventurer.getTileX();
    int dy = treasure.getTileY() - adventurer.getTileY();
    String direction = "";
    if (dy > 0) {
      direction += "le SUD";
    } else if (dy < 0) {
      direction += "le NORD";
    }
    if (dx > 0) {
      if (!direction.isEmpty()) {
        direction += "-EST";
      } else {
        direction += "l'EST";
      }
    } else if (dx < 0) {
      if (!direction.isEmpty()) {
        direction += "-OUEST";
      } else {
        direction += "l'OUEST";
      }
    }
    return direction;
  }

}



