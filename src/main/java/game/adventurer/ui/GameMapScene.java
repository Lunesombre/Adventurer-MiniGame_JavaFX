package game.adventurer.ui;

import game.adventurer.model.GameMap;
import game.adventurer.model.Tile;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class GameMapScene extends Scene {

  public GameMapScene(GameMap gameMap) {
    super(new GridPane(), 800, 600);
    GridPane grid = (GridPane) getRoot();

    // représentation visuelle de la grille
    for (int y = 0; y < gameMap.getMapHeight(); y++) {
      for (int x = 0; x < gameMap.getMapWidth(); x++) {
        Tile tile = gameMap.getGrid()[y][x];
        Rectangle rect = new Rectangle(30, 30);
        rect.setFill(tile.getType() == Tile.Type.PATH ? Color.web("#B87065") : Color.web("#206600"));
        grid.add(rect, x, y);
      }
    }

    // représentation de l'aventurier et du trésor
    Circle adventurer = new Circle(15, Color.BLUE);
    grid.add(adventurer, gameMap.getAdventurer().getX(), gameMap.getAdventurer().getY());

    Circle treasure = new Circle(15, Color.GOLD);
    grid.add(treasure, gameMap.getTreasure().getX(), gameMap.getTreasure().getY());
  }
}


