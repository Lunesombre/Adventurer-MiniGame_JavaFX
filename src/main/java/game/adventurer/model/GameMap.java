package game.adventurer.model;

import game.adventurer.model.Tile.Type;
import game.adventurer.model.base.Creature;
import game.adventurer.model.base.Wound;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.model.enums.WoundCause;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@AllArgsConstructor
@Getter
@Setter
public class GameMap {

  private Tile[][] grid;
  private int mapWidth;
  private int mapHeight;
  private Adventurer adventurer;
  private Treasure treasure;
  private static final Logger LOG = LoggerFactory.getLogger(GameMap.class);
  private List<Wound> woundsList;

  public GameMap(Tile[][] grid, int mapWidth, int mapHeight, Adventurer adventurer, Treasure treasure) {
    this.grid = grid;
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
    this.adventurer = adventurer;
    this.treasure = treasure;
    this.woundsList = new ArrayList<>();
  }

  public MoveResult moveAdventurer(int dx, int dy) {
    int newX = adventurer.getTileX() + dx;
    int newY = adventurer.getTileY() + dy;

    // First, check if the move is within the map bounds
    if (isOutOfMapBounds(newX, newY)) {
      LOG.warn("Vous ne pouvez pas quitter la carte sans le trésor !");
      return MoveResult.OUT_OF_BOUNDS;
    }

    // Then, check if it's a valid move within the map.
    if (!isValidMove(newX, newY, adventurer)) {
      // Si le mouvement n'est pas valide, vérifier si c'est à cause d'une case de type WOOD
      if (grid[newY][newX].getType() == Type.WOOD) {
        WoodsWound wound = new WoodsWound(WoundCause.WOODS);
        wound.setWoundsMessage(adventurer);
        adventurer.setHealth(adventurer.getHealth() - wound.getHealthCost());
        LOG.error("Blessure : {}", wound.getWoundMessage());
        LOG.warn("PV de {} : {}", adventurer.getName(), adventurer.getHealth());
        woundsList.add(wound);
        return MoveResult.WOUNDED;
      } else {
        LOG.debug("Mouvement invalide, l'aventurier reste en ({}, {})",
            adventurer.getTileX(), adventurer.getTileY());
        return MoveResult.BLOCKED;
      }

    }

    // If everything else is ok, proceed with the move.
    adventurer.setTileX(newX);
    adventurer.setTileY(newY);
    return MoveResult.MOVED;
  }

  private boolean isValidMove(int x, int y, Creature creature) {
    switch (creature) {
      case Adventurer ignored -> {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight && grid[y][x].getType() == Type.PATH;
      }
      case Monster ignored -> {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
      }
      default -> {
        return false;
      }
    }
  }

  private boolean isOutOfMapBounds(int x, int y) {
    return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
  }

  @Override
  public String toString() {
    return "GameMap{" +
        "grid=" + (grid == null ? "null" : gridToString()) +
        ", mapWidth=" + mapWidth +
        ", mapHeight=" + mapHeight +
        ", adventurer=" + adventurer +
        ", treasure=" + treasure +
        '}';
  }

  private String gridToString() {
    if (grid == null) {
      return "null";
    }
    return Arrays.stream(grid)
        .map(row -> row == null ? "null" : Arrays.stream(row)
            .map(tile -> tile == null ? "null" : tile.toString())
            .collect(Collectors.joining(", ", "[", "]")))
        .collect(Collectors.joining(",\n ", "[\n ", "\n]"));
  }
}
