package game.adventurer.ui;

import game.adventurer.config.SceneConfig;
import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class GameMapScene extends Scene {

  private static final int PADDING = 20;
  private GameMap gameMap;
  private Pane gamePane;


  //V3 pattern Factory
  private GameMapScene(StackPane root, int width, int height) {
    super(root, width, height);
  }

  public static GameMapScene create(GameMap gameMap) {
    SceneConfig config = SceneConfig.getInstance();
    StackPane root = new StackPane();
    GameMapScene scene = new GameMapScene(root, config.getWidth(), config.getHeight());
    scene.gameMap = gameMap;
    scene.initializeScene();
    return scene;
  }

  private void initializeScene() {
    SceneConfig config = SceneConfig.getInstance();
    int windowWidth = config.getWidth();
    int windowHeight = config.getHeight();

    StackPane root = (StackPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);

    gamePane = new Pane();
    root.getChildren().add(gamePane);

    // Add a listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateGameMap());
    heightProperty().addListener((obs, oldVal, newVal) -> updateGameMap());

    // Initialiser la carte
    updateGameMap();
  }

  private void updateGameMap() {
    gamePane.getChildren().clear();
    double windowWidth = getWidth();
    double windowHeight = getHeight();
    int mapWidth = gameMap.getMapWidth();
    int mapHeight = gameMap.getMapHeight();

    // Calculate tile size to adapt to window
    double tileSize = Math.min(
        (windowWidth - 2 * PADDING) / mapWidth,
        (windowHeight - 2 * PADDING) / mapHeight
    );

    // Calculate the offset to center the map
    double xOffset = (windowWidth - mapWidth * tileSize) / 2;
    double yOffset = (windowHeight - mapHeight * tileSize) / 2;

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

    // Adding adventurer
    Circle adventurer = new Circle(tileSize / 2, Color.BLUE);
    adventurer.setCenterX(xOffset + (gameMap.getAdventurer().getX() + 0.5) * tileSize);
    adventurer.setCenterY(yOffset + (gameMap.getAdventurer().getY() + 0.5) * tileSize);
    gamePane.getChildren().add(adventurer);

    // Adding treasure
    Circle treasure = new Circle(tileSize / 2, Color.GOLD);
    treasure.setCenterX(xOffset + (gameMap.getTreasure().getX() + 0.5) * tileSize);
    treasure.setCenterY(yOffset + (gameMap.getTreasure().getY() + 0.5) * tileSize);
    gamePane.getChildren().add(treasure);

    // TODO : changer taille écran titre aussi
    // TODO : changer police du titre
    // TODO : mettre une taille max de la carte, selon résolution écran utilisateur
    // TODO : new Game screen avec selection Nom aventurier et taille de la map
    // TODO : ajouter logo jeu à gauche et dans barre des tâches
  }
}



