package game.adventurer.ui;

import static game.adventurer.AdventurerGameApp.highScoreManager;
import static game.adventurer.util.MiscUtil.calculateFieldOfView;
import static game.adventurer.util.MiscUtil.handleInvalidGameState;
import static javafx.scene.paint.Color.rgb;

import game.adventurer.common.Localizable;
import game.adventurer.common.SharedSize;
import game.adventurer.controller.RightPanelController;
import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.MissingCreatureException;
import game.adventurer.exceptions.WrongTypeOfCreatureException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.Treasure;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Lurker;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.creature.Mugger;
import game.adventurer.model.creature.Sniffer;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.Direction;
import game.adventurer.model.enums.Move;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.service.MonsterBehaviorManager;
import game.adventurer.ui.animation.CreatureAnimationManager;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.ui.common.CreditsOverlay;
import game.adventurer.ui.common.OptionsPanel;
import game.adventurer.ui.common.ScoreBoard;
import game.adventurer.ui.common.TriangleCreatureRepresentation;
import game.adventurer.ui.common.option.KeyBindingOption;
import game.adventurer.ui.common.option.LanguageOption;
import game.adventurer.ui.common.option.ScoreBoardOption;
import game.adventurer.util.PathfindingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainGameScene extends BaseScene implements Localizable {

  private static final int PADDING = 40;
  private static final double MESSAGE_BOX_MIN_WIDTH = 230.0;
  private static final double MESSAGE_BOX_MIN_HEIGHT = 250.0;
  public static final String FAILED_INITIAL_DISTANCE = "Failed to calculate initial distance to treasure. This should never happen.";
  public static final String MEDIEVAL_FONT = "medieval-font";
  private int initialHealth;
  private GameMap gameMap;
  private Pane mapView;
  private VBox leftPanel;
  private VBox rightPanel;
  @Getter
  private final RightPanelController rightPanelController = new RightPanelController(PADDING, MESSAGE_BOX_MIN_WIDTH);
  @Getter
  private DifficultyLevel difficultyLevel;
  @Getter
  private int initialDistanceToTreasure;
  private boolean hasSentMessage = false;
  private boolean isPaused = false;

  private Circle adventurerCircle;
  private Group treasureCross;
  @Getter
  private double tileSize;
  @Getter
  private double xOffset;
  @Getter
  private double yOffset;
  @Getter
  private int movesCount;
  @Setter
  private Runnable onGameEnd;
  @Getter
  @Setter
  private Runnable onGameOver;

  private final Rectangle effectOverlay = new Rectangle();
  private OptionsPanel options;
  private StackPane pause;
  private Rectangle pauseRectangle;
  private KeyBindingOption movementBindings;

  private ScoreBoard scoreBoard;
  private ScoreBoardOption scoreBoardOption;
  private final LocalizedMessageService localizedMessageService = LocalizedMessageService.getInstance();
  private final LocalizationService localizationService;
  private final HostServices hostServices;
  private CreditsOverlay creditsOverlay;

  private Rectangle[][] tileRectangles; // 2D array to store references to the mapView rectangles
  private Set<Position> visibleTiles; // Positions of the Tiles that the Adventurer has in its field of view

  private final List<Timeline> activeTimelines = new ArrayList<>(); // stores timelines to properly handle them on scene change
  private final Map<Creature, Node> creaturesRepresentationMap = new HashMap<>(); // stores link between a Creature the Node representing it
  private CreatureAnimationManager creatureAnimationManager;
  private MonsterBehaviorManager monsterBehaviorManager;

  /*
  Localizable elements
   */
  Text credits;
  Text pauseText;
  LanguageOption languageOption;
  VBox optionsBox;


  //V3 pattern Factory
  private MainGameScene(BorderPane root, SharedSize sharedSize, LocalizationService localizationService, HostServices hostServices) {
    super(root, sharedSize);
    this.localizationService = localizationService;
    this.hostServices = hostServices;
  }

  public static MainGameScene create(GameMap gameMap, SharedSize sharedSize, DifficultyLevel difficultyLevel, LocalizationService localizationService,
      HostServices hostServices)
      throws InvalidGameStateException {
    BorderPane root = new BorderPane();
    MainGameScene scene = new MainGameScene(root, sharedSize, localizationService, hostServices);
    scene.gameMap = gameMap;
    scene.difficultyLevel = difficultyLevel;
    scene.tileRectangles = new Rectangle[gameMap.getMapHeight()][gameMap.getMapWidth()];
    scene.initialize();
    return scene;
  }

  @Override
  protected void initialize() throws InvalidGameStateException {
    initialHealth = gameMap.getAdventurer().getHealth();
    this.visibleTiles = gameMap.getAdventurer().getVisibleTiles(); // visibleTiles references the set in Adventurer
    double windowWidth = sharedSize.getWidth();
    double windowHeight = sharedSize.getHeight();
    initialDistanceToTreasure = calculateMovesToTreasure();
    if (initialDistanceToTreasure == -1) {
      log.error(FAILED_INITIAL_DISTANCE);
      throw new InvalidGameStateException(FAILED_INITIAL_DISTANCE);
    }

    BorderPane root = (BorderPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);

    initializeGameMap();
    createLeftPanel();
    scoreBoard = new ScoreBoard(highScoreManager, sharedSize.getWidth());
    scoreBoard.updateSize(sharedSize.getWidth(), sharedSize.getHeight()); // updateSize and position
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

    root.setCenter(mapView);

    // Set handler for keyboard events
    setOnKeyPressed(this::handleKeyPress);

    movesCount = 0;

    effectOverlay.setWidth(windowWidth);
    effectOverlay.setHeight(windowHeight);
    effectOverlay.setVisible(false);
    root.getChildren().add(effectOverlay); // Must be added after other children to appear above them.

    pauseRectangle = new Rectangle(windowWidth, windowHeight, Color.color(1.0, 1.0, 1.0, 0.3));
    pauseRectangle.setPickOnBounds(false);
    pauseText = new Text(localizedMessageService.getMessage("mainScene.pause"));
    pauseText.setStyle("-fx-font-size:64px;");
    pauseText.getStyleClass().add(MEDIEVAL_FONT);
    pauseText.setFill(Color.DARKRED);
    pauseText.setStroke(Color.BLACK);
    pauseText.setEffect(new Bloom(0.2));
    pause = new StackPane();
    pause.getChildren().addAll(pauseRectangle, pauseText);
    pause.setVisible(false);
    pause.setPickOnBounds(false);
    pause.setLayoutX(windowWidth / 2);
    pause.setLayoutY(windowHeight / 2);

    root.getChildren().add(pause);
    root.setRight(rightPanel); // Defining the setRight here for it to never be covered by the pause StackPane
    root.getChildren().add(scoreBoard); // Last to be added, the initially hidden ScoreBoard

    // Adding the credits overlay panel
    creditsOverlay = new CreditsOverlay(hostServices, localizationService, windowWidth, windowHeight);
    root.getChildren().add(creditsOverlay);

    // Hide the ScoreBoard by clicking outside it
    root.setOnMouseClicked(e -> {
      if (scoreBoard.isShowing() && !scoreBoard.getBoundsInParent().contains(e.getX(), e.getY())) {
        toggleScoreBoard(e);
      }
    });

    setupResizeListeners();

    // Handle focus
    // Listener for focus (map/options)
    options.addToggleListener(isShowing -> {
      if (Boolean.TRUE.equals(isShowing)) {
        options.requestFocus();
        if (!isPaused) {
          togglePause();
        }
      } else {
        mapView.requestFocus();
        togglePause();
      }
    });

    mapView.requestFocus(); // The map has initial focus

    // Register this class as Localizable - done after the localizable texts are set to avoid Null Pointer Exception.
    localizationService.registerLocalizable(this);

    creatureAnimationManager = new CreatureAnimationManager(this, this.gameMap);
    monsterBehaviorManager = new MonsterBehaviorManager(gameMap, creaturesRepresentationMap, activeTimelines);
    startGameLoop();
    startMonsterMovement();


  }

  @Override
  protected void onSizeChanged(double width, double height) {
    options.updateSize(width, height);

    scoreBoard.updateSize(width, height);
    scoreBoard.updateStyles(width);
    scoreBoard.layout();

    scoreBoardOption.adjustContentLabelsFontSize(width);

    creditsOverlay.updateSize(width, height);

    // Determine if other listeners need to be put here or not
  }

  private void handleKeyPress(KeyEvent event) {
    if (options.isShowing()) {
      return;
    }
    Map<String, KeyCode> bindings = movementBindings.getBindings();
    KeyCode keyCode = event.getCode();
    MoveResult moveResult;
    if (bindings.containsValue(keyCode)) {
      moveResult = handleAdventurerMovement(keyCode, bindings);
    } else {
      moveResult = switch (keyCode) {
        case SPACE, P -> {
          if (creditsOverlay.isVisible()) {
            creditsOverlay.hide();
          }
          togglePause();
          yield MoveResult.BLOCKED;
        }
        case ESCAPE -> {
          if (creditsOverlay.isVisible()) {
            creditsOverlay.hide();
          } else {
            togglePause();
          }
          yield MoveResult.BLOCKED;
        }
        case BACK_SPACE -> {
          options.toggleOptions();
          if (!isPaused) {
            togglePause();
          }
          yield MoveResult.BLOCKED;
        }
        case C -> {
          if (isPaused) {
            if (creditsOverlay.isVisible()) {
              creditsOverlay.hide();
            } else {
              creditsOverlay.show();
            }
          } else {
            if (creditsOverlay.isVisible()) {
              creditsOverlay.hide();
            } else {
              creditsOverlay.show();
            }
            togglePause();
          }
          yield MoveResult.BLOCKED;
        }
        default -> {
          handleOtherKeys(event);
          yield MoveResult.BLOCKED;
        }
      };
    }

    handleMoveResult(moveResult);
    event.consume();
  }

  private MoveResult handleAdventurerMovement(KeyCode keyCode, Map<String, KeyCode> bindings) {
    if (isPaused) {
      return MoveResult.BLOCKED;
    }
    return switch (keyCode) {
      case KeyCode ignored when keyCode == bindings.get("option.kb.binding.label.up") -> {
        gameMap.getAdventurer().setFacingDirection(Direction.NORTH);
        yield gameMap.moveAdventurer(Move.UP);
      }
      case KeyCode ignored when keyCode == bindings.get("option.kb.binding.label.down") -> {
        gameMap.getAdventurer().setFacingDirection(Direction.SOUTH);
        yield gameMap.moveAdventurer(Move.DOWN);
      }
      case KeyCode ignored when keyCode == bindings.get("option.kb.binding.label.left") -> {
        gameMap.getAdventurer().setFacingDirection(Direction.WEST);
        yield gameMap.moveAdventurer(Move.LEFT);
      }
      case KeyCode ignored when keyCode == bindings.get("option.kb.binding.label.right") -> {
        gameMap.getAdventurer().setFacingDirection(Direction.EAST);
        yield gameMap.moveAdventurer(Move.RIGHT);
      }
      default -> MoveResult.BLOCKED;
    };
  }

  private void handleMoveResult(MoveResult moveResult) {
    switch (moveResult) {
      case MOVED -> handleSuccessfulMove();
      case WOUNDED -> handleWound();
      case OUT_OF_BOUNDS -> handleOutOfBoundsMove();
      case BLOCKED -> log.debug("BLOCKED");
    }
  }

  private void handleSuccessfulMove() {
    movesCount++;
    // start animation
    try {
      creatureAnimationManager.animateCreature(Map.entry(gameMap.getAdventurer(), adventurerCircle),
          gameMap.getAdventurer().getPreviousTileX(),
          gameMap.getAdventurer().getPreviousTileY(),
          gameMap.getAdventurer().getTileX(),
          gameMap.getAdventurer().getTileY()
      );
    } catch (WrongTypeOfCreatureException e) {
      handleInvalidGameState(this.getClass(), e);
    }
    if (isTreasureCollected()) {
      if (onGameEnd != null) {
        // Short pause before EndGame Screen so that the player understands what happens
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(event -> onGameEnd.run());
        pause.play();
      } else {
        log.error("onGameEnd is null");
      }
    } else {
      if (!treasureCross.isVisible()) {
        updateTreasureVisibility();
      }
    }
  }

  public void handleWound() {
    gameMap.getWoundManager().handleWound(rightPanelController, onGameOver, gameMap.getAdventurer());
    showDamageEffect();
  }

  private void handleOutOfBoundsMove() {
    showOutOfBoundsEffect();
    rightPanelController.addMessage("mainScene.outOfBoundsMove");
  }

  private void handleOtherKeys(KeyEvent event) {
    log.warn("Unhandled key press: {}", event.getCode());
  }

  /**
   * Updates the visual representation of the adventurer's field of view and the visibility of certain game elements.
   * <p>
   * This method updates the adventurer's field of view display based on the current game state and controls the visibility of specific game objects
   * (like the monsters' representations), making them appear only when they are within the adventurer's visible tiles.
   * </p>
   */
  private void updateVisibilityAndFieldOfView() {
    // Set Adventurer field of View visually
    updateAdventurerFieldOfView();

    // displays/hide the Monster representation based on the Adventurer FoV
    for (Monster monster : gameMap.getMonsters()) {
      Position monsterPos = new Position(monster.getTileX(), monster.getTileY());
      creaturesRepresentationMap.get(monster).setVisible(visibleTiles.contains(monsterPos));
    }

  }

  /**
   * Handles the resizing of the game map and updates the visual representation of all elements. This method recalculates the map dimensions, resizes
   * and repositions all tiles, updates the position and size of creatures (monsters and the adventurer), and adjusts the treasure's visual
   * representation accordingly.
   * <p>
   * After all updates, it forces a layout refresh to ensure the UI is correctly redrawn.
   * </p>
   */
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

    // update size and position for creatures' representations
    // update size and position of Monsters
    for (Monster monster : gameMap.getMonsters()) {
      updateCreatureVisual(monster, creaturesRepresentationMap.get(monster));
    }
    // Update the size and position of the adventurer on the map
    updateCreatureVisual(gameMap.getAdventurer(), adventurerCircle);

    // Update size and position of treasure
    updateTreasureCrossVisual();

    // Forcing a visual update
    mapView.requestLayout();
  }

  /**
   * Computes the graphical position of a tile-based element on the map. This method calculates the position by centering the element within its
   * tile.<p> It is used when the Scene is resized, to properly position the center of the Circle in the Scene
   *
   * @param tileCoord The tile coordinate (X or Y) of the element on the grid.
   * @param offset    The offset to apply to the position (typically xOffset or yOffset).
   * @return The computed pixel position on the screen.
   */
  private double computeCenterCoord(int tileCoord, double offset) {
    return offset + (tileCoord + 0.5) * tileSize;
  }

  private void updateCreatureVisual(Creature creature, Node visualRep) {
    double centerX = computeCenterCoord(creature.getTileX(), xOffset);
    double centerY = computeCenterCoord(creature.getTileY(), yOffset);

    switch (visualRep) {
      case Circle circle -> {
        circle.setRadius(tileSize / 2);
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
      }
      case Rectangle rect -> {
        rect.setWidth(tileSize);
        rect.setHeight(tileSize);
        rect.setX(centerX - tileSize / 2);
        rect.setY(centerY - tileSize / 2);
      }
      case ImageView imageView -> {
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);
        imageView.setX(centerX - tileSize / 2);
        imageView.setY(centerY - tileSize / 2);
      }
      case TriangleCreatureRepresentation triangle -> {
        triangle.setSideLength(tileSize * 0.9);
        triangle.setLayoutX(centerX);
        triangle.setLayoutY(centerY);
      }
      default -> throw new IllegalStateException("Unhandled Node type: " + visualRep);
    }
  }

  private void updateTreasureCrossVisual() {
    double crossSize = tileSize / 2;
    double centerX = computeCenterCoord(gameMap.getTreasure().getTileX(), xOffset);
    double centerY = computeCenterCoord(gameMap.getTreasure().getTileY(), yOffset);

    Line line1 = (Line) treasureCross.getChildren().get(0);
    Line line2 = (Line) treasureCross.getChildren().get(1);

    // Calculate coordinates for the cross' lines
    double halfSize = crossSize / 2;
    line1.setStartX(-halfSize);
    line1.setStartY(-halfSize);
    line1.setEndX(halfSize);
    line1.setEndY(halfSize);

    line2.setStartX(-halfSize);
    line2.setStartY(halfSize);
    line2.setEndX(halfSize);
    line2.setEndY(-halfSize);

    // Positions the Group
    treasureCross.setLayoutX(centerX);
    treasureCross.setLayoutY(centerY);
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
        rect.setFill(tile.getType() == Type.PATH ? Color.web("#B87065") : Color.web("#206600"));
        rect.setX(xOffset + x * tileSize);
        rect.setY(yOffset + y * tileSize);
        rect.setStroke(Color.web("#4a5246")); // grey-greenish tile border
        rect.setStrokeWidth(0.5);
        mapView.getChildren().add(rect);
        tileRectangles[y][x] = rect; // Store reference to the rectangle
      }
    }

    // Adding treasure
    treasureCross = new Group();
    double xCenter = xOffset + (gameMap.getTreasure().getTileX() + 0.5) * tileSize;
    double yCenter = yOffset + (gameMap.getTreasure().getTileY() + 0.5) * tileSize;
    double halfSize = tileSize / 4; // handles size of the "X"

    Line line1 = new Line(xCenter - halfSize, yCenter - halfSize, xCenter + halfSize, yCenter + halfSize);
    Line line2 = new Line(xCenter - halfSize, yCenter + halfSize, xCenter + halfSize, yCenter - halfSize);
    line1.setStroke(Color.DARKRED);
    line1.setStrokeWidth(3);
    line2.setStroke(Color.DARKRED);
    line2.setStrokeWidth(3);

    treasureCross.getChildren().addAll(line1, line2);

    if (difficultyLevel != DifficultyLevel.EASY) {
      treasureCross.setVisible(false);
    }
    mapView.getChildren().add(treasureCross);

    // Adding adventurer
    adventurerCircle = new Circle(tileSize / 2, Color.BLUE);
    double advX = xOffset + (gameMap.getAdventurer().getTileX() + 0.5) * tileSize;
    double advY = yOffset + (gameMap.getAdventurer().getTileY() + 0.5) * tileSize;
    adventurerCircle.setCenterX(advX);
    adventurerCircle.setCenterY(advY);
    mapView.getChildren().add(adventurerCircle);

    // Adding initial field of view representation
    visibleTiles.addAll(calculateFieldOfView(gameMap.getAdventurer(), gameMap));
    for (Position pos : visibleTiles) {
      Rectangle rect = tileRectangles[pos.y()][pos.x()];
      if (gameMap.getTileTypeAt(pos.x(), pos.y()) == Type.PATH) {
        rect.setFill(Color.web("#D8A095"));
      }
    }

    // Adding Monsters representations
    for (Monster monster : gameMap.getMonsters()) {
      TriangleCreatureRepresentation monsterRender = new TriangleCreatureRepresentation(tileSize * 0.9, monster.getFacingDirection());
      double maDvX = xOffset + (monster.getTileX() + 0.5) * tileSize;
      double maDvY = yOffset + (monster.getTileY() + 0.5) * tileSize;

      // Move the triangle to (maDvX, maDvY)
      monsterRender.setLayoutX(maDvX);
      monsterRender.setLayoutY(maDvY);

      mapView.getChildren().add(monsterRender);
      switch (monster) {
        case Mugger ignored -> monsterRender.setFill(Color.RED);
        case Sniffer ignored -> monsterRender.setFill(Color.YELLOW);
        case Lurker ignored -> monsterRender.setFill(Color.ORCHID);
        default -> log.warn("Unknown Monster added to game map, no render possible");

      }
      creaturesRepresentationMap.put(monster, monsterRender);
    }

    // Listener for facingDirection of Monsters
    for (Entry<Creature, Node> entry : creaturesRepresentationMap.entrySet()) {
      entry.getKey().facingDirectionProperty().addListener((observable, oldValue, newValue) -> {
        if (entry.getValue() instanceof TriangleCreatureRepresentation triangle) {
          triangle.setRotation(newValue, true);
        }
      });
    }

    handleResize();
  }

  private void calculateMapDimensions(int mapWidth, int mapHeight) {
    double availableWidth = getWidth() / 2;
    double availableHeight = getHeight();

    log.debug("availableWidth : {}, availableHeight : {}", availableWidth, availableHeight);

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

  public void showDamageEffect() {
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

  private void createLeftPanel() {
    leftPanel = new VBox(10);
    leftPanel.setPadding(new Insets(10));

    HBox playerNameBox = new HBox();
    Label playerNameLabel = new Label(gameMap.getAdventurer().getName());
    playerNameLabel.setStyle("-fx-font-size: 42px; -fx-text-fill: #ffc400");
    playerNameLabel.getStyleClass().add(MEDIEVAL_FONT);
    playerNameBox.setAlignment(Pos.CENTER);
    playerNameBox.getChildren().add(playerNameLabel);
    playerNameBox.setBackground(new Background(new BackgroundFill(rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));

    HBox imageBox = new HBox();
    imageBox.setAlignment(Pos.CENTER);
    Image charImage = new Image("assets/images/adventurer_chara_v_0_2.png");
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
    borderGlow.setColor(rgb(195, 130, 0));
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
    statsLabel.getStyleClass().add(MEDIEVAL_FONT);
    statsLabel.setPadding(new Insets(16));
    statsBox.setBackground(new Background(new BackgroundFill(rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));

    HBox healthBox = new HBox(20);
    healthBox.setAlignment(Pos.CENTER);
    ImageView heart = new ImageView("assets/icons/heartR.png");
    heart.fitWidthProperty().bind(leftPanel.widthProperty().multiply(0.08));
    heart.setPreserveRatio(true);
    Text playerHealth = new Text();
    playerHealth.setStyle("-fx-font-weight: bold");
    playerHealth.setFill(rgb(0, 120, 0));
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

    optionsBox = new VBox(5);
    optionsBox.setAlignment(Pos.TOP_RIGHT);

    scoreBoardOption = new ScoreBoardOption("option.highScores.label", scoreBoard, highScoreManager);

    Map<String, KeyCode> defaultMovementBindings = new HashMap<>();
    defaultMovementBindings.put("option.kb.binding.label.up", KeyCode.UP);
    defaultMovementBindings.put("option.kb.binding.label.down", KeyCode.DOWN);
    defaultMovementBindings.put("option.kb.binding.label.left", KeyCode.LEFT);
    defaultMovementBindings.put("option.kb.binding.label.right", KeyCode.RIGHT);

    movementBindings = new KeyBindingOption("option.keybinding.label", defaultMovementBindings);
    languageOption = new LanguageOption(localizedMessageService, localizationService, 18);

    options = new OptionsPanel(sharedSize.getWidth(), sharedSize.getHeight(), languageOption, scoreBoardOption, movementBindings);
    optionsBox.getChildren().add(options);

    VBox messagesBox = new VBox(5);
    messagesBox.setPadding(new Insets(20));
    messagesBox.setBackground(new Background(new BackgroundFill(rgb(80, 80, 80, 0.6), new CornerRadii(15.0), Insets.EMPTY)));
    messagesBox.setMinSize(MESSAGE_BOX_MIN_WIDTH, MESSAGE_BOX_MIN_HEIGHT);
    // Initialize the messages' controller
    rightPanelController.initMessagesBox(messagesBox);
    // Set the first message.
    if (difficultyLevel != DifficultyLevel.EASY) {
      String messageKey = "mainScene.otherModes.startingMessage";
      String directionHint = getDirectionHint(gameMap.getAdventurer(), gameMap.getTreasure());
      String distanceHint = getDistanceHint();
      rightPanelController.addMessage(messageKey, gameMap.getAdventurer().getName(), distanceHint, directionHint);
    } else {
      String messageKey = "mainScene.easyMode.startingMessage";
      rightPanelController.addMessage(messageKey, gameMap.getAdventurer().getName());
    }

    VBox contactBox = new VBox(5);
    contactBox.setAlignment(Pos.BOTTOM_RIGHT);
    credits = new Text(localizedMessageService.getMessage("mainScene.credits"));

    credits.setOnMouseClicked((e -> {
      if (!isPaused) {
        togglePause();
      }
      creditsOverlay.show();
    }));

    contactBox.getChildren().add(credits);

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
      pauseRectangle.setWidth(newVal.doubleValue());
      pause.setLayoutX(newVal.doubleValue() / 2);
      if (!scoreBoard.isShowing()) {
        scoreBoard.setTranslateX(totalWidth); // keeps it hidden on Scene and windows resizing
      }

      handleResize();
      updateSize();
    });

    heightProperty().addListener((obs, oldVal, newVal) -> {
      leftPanel.setPrefHeight(newVal.doubleValue());
      rightPanel.setPrefHeight(newVal.doubleValue());
      mapView.setPrefHeight(newVal.doubleValue());
      effectOverlay.setHeight(newVal.doubleValue());
      pauseRectangle.setHeight(newVal.doubleValue());
      pause.setLayoutY(newVal.doubleValue() / 2);

      handleResize();
      updateSize();
    });
  }

  private void healthChangeListener(Text playerHealth) {
    // Add a Listener to check on health's changes.
    gameMap.getAdventurer().healthProperty().addListener((observable, oldValue, newValue) -> {
      int currentHealth = newValue.intValue();
      double healthPercentage = (double) currentHealth / initialHealth;

      if (healthPercentage > 0.7) {
        playerHealth.setFill(rgb(0, 120, 0)); // Green : > 70%
      } else if (healthPercentage > 0.5) {
        playerHealth.setFill(rgb(175, 160, 0)); // Yellow : > 50%
      } else if (healthPercentage > 0.3) {
        playerHealth.setFill(rgb(190, 115, 0)); // Orange : > 30%
      } else {
        playerHealth.setFill(rgb(195, 0, 0)); // Red : <= 30%
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
      case DifficultyLevel.EASY -> treasureCross.setVisible(true);
      case DifficultyLevel.NORMAL -> {
        int movesToTreasure = calculateMovesToTreasure();
        if (movesToTreasure <= 3 && !hasSentMessage) {
          rightPanelController.addMessage("mainScene.treasureIsClose");
          hasSentMessage = true;
        }
        if (movesToTreasure > 3 && hasSentMessage) {
          hasSentMessage = false;
        }
        treasureCross.setVisible(movesToTreasure <= 2);
      }
      case DifficultyLevel.HARD -> {
        int movesToTreasure = calculateMovesToTreasure();
        if (movesToTreasure <= 2 && !hasSentMessage) {
          rightPanelController.addMessage("mainScene.treasureIsClose");
          hasSentMessage = true;
        }
        if (movesToTreasure > 2 && hasSentMessage) {
          hasSentMessage = false;
        }
        treasureCross.setVisible(movesToTreasure <= 1);
      }
      default -> treasureCross.setVisible(false);
    }
  }

  private String getDistanceHint() {

    if ((gameMap.getMapWidth() <= 11 && initialDistanceToTreasure >= 10)
        || (gameMap.getMapWidth() > 11 && gameMap.getMapWidth() < 21 && initialDistanceToTreasure >= 20)
        || (gameMap.getMapWidth() >= 21 && initialDistanceToTreasure >= 30)) {
      return "mainScene.treasureDistanceHint";
    }
    return "";
  }

  private static String getDirectionHint(Adventurer adventurer, Treasure treasure) {
    int dx = treasure.getTileX() - adventurer.getTileX();
    int dy = treasure.getTileY() - adventurer.getTileY();

    // wanted to use Java 21+ pattern matching, but I can't get it to work, so...
    return switch (Math.signum(dx) + " " + Math.signum(dy)) {
      case "0.0 1.0" -> "mainScene.treasureDirectionHintS";
      case "0.0 -1.0" -> "mainScene.treasureDirectionHintN";
      case "1.0 0.0" -> "mainScene.treasureDirectionHintE";
      case "-1.0 0.0" -> "mainScene.treasureDirectionHintW";
      case "1.0 1.0" -> "mainScene.treasureDirectionHintSE";
      case "-1.0 1.0" -> "mainScene.treasureDirectionHintSW";
      case "1.0 -1.0" -> "mainScene.treasureDirectionHintNE";
      case "-1.0 -1.0" -> "mainScene.treasureDirectionHintNW";
      default -> "";
    };
  }

  private void togglePause() {
    isPaused = !isPaused;
    if (isPaused) {
      pauseMonsters();
    } else {
      unpauseMonsters();
    }
    pause.setVisible(isPaused);
    log.info("en pause : {}", isPaused);
  }

  private void toggleScoreBoard(MouseEvent event) {
    scoreBoard.setVisible(true);
    scoreBoard.toggleDisplay();
    if (scoreBoard.isShowing()) {
      scoreBoardOption.getShowScoresLabel().setText(localizedMessageService.getMessage("highScores.hide"));
    } else {
      scoreBoardOption.getShowScoresLabel().setText(localizedMessageService.getMessage("highScores.show"));
    }
    event.consume();
  }


  @Override
  public void updateLanguage(Locale newLocale) {
    credits.setText(localizedMessageService.getMessage("mainScene.credits"));
    pauseText.setText(localizedMessageService.getMessage("mainScene.pause"));
    scoreBoardOption.updateLanguage(newLocale);
    options.updateLanguage();
    rightPanelController.updateLanguage(newLocale);
  }

  private void updateAdventurerFieldOfView() {
    Set<Position> newVisibleTiles = calculateFieldOfView(gameMap.getAdventurer(), gameMap);
    Set<Position> previouslyVisibleTiles = new HashSet<>(visibleTiles);

    // Resets tiles that are no longer visible
    for (Position pos : previouslyVisibleTiles) {
      if (!newVisibleTiles.contains(pos)) {
        Rectangle rect = tileRectangles[pos.y()][pos.x()];
        if (gameMap.getTileTypeAt(pos.x(), pos.y()) == Type.PATH) {
          rect.setFill(Color.web("#B87065")); // Original PATH color
        }
      }
    }
    // Updates newly visible tiles
    for (Position pos : newVisibleTiles) {
      Rectangle rect = tileRectangles[pos.y()][pos.x()];
      if (gameMap.getTileTypeAt(pos.x(), pos.y()) == Type.PATH) {
        rect.setFill(Color.web("#D8A095")); // Lighter PATH color
      }
    }
    // Update the stored visible tiles
    visibleTiles.clear();
    visibleTiles.addAll(newVisibleTiles);
  }

  private void startMonsterMovement() throws MissingCreatureException, WrongTypeOfCreatureException {
    monsterBehaviorManager.moveMonsters(creatureAnimationManager);
  }

  public void stopActiveTimelines() {
    for (Timeline timeline : activeTimelines) {
      timeline.stop();
      log.info("Timeline {} stopped", timeline);
    }
    activeTimelines.clear(); // Clears list
  }

  private void startGameLoop() {
    final int refreshRate = 60;
    Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(1000.0 / refreshRate), event -> updateVisibilityAndFieldOfView()));
    gameLoop.setCycleCount(Animation.INDEFINITE); // Infinitely loops as long as gameLoop isn't stopped.
    activeTimelines.add(gameLoop);
    gameLoop.play();
  }

  private void pauseMonsters() {
    for (int i = 0; i < activeTimelines.size(); i++) {
      // we don't want to pause the gameLoop timeline that is the last one in the list
      activeTimelines.get(i).pause();
    }
  }

  private void unpauseMonsters() {
    for (Timeline tl : activeTimelines) {
      tl.play();
    }
  }


}



