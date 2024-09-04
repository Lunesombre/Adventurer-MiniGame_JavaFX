package game.adventurer.model;

import game.adventurer.model.Tile.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
@Getter
@Setter
public class GameMap {

  private Tile[][] grid;
  private int mapWidth;
  private int mapHeight;
  private Adventurer adventurer;
  private Treasure treasure;

  public boolean moveAdventurer(int dx, int dy) {
    int newX = adventurer.getTileX() + dx;
    int newY = adventurer.getTileY() + dy;

    if (isValidMove(newX, newY, adventurer)) {
      adventurer.setTileX(newX);
      adventurer.setTileY(newY);
      return true;
    }
    LoggerFactory.getLogger(GameMap.class).info("Invalid move, adventurer stays at ({}, {})",
        adventurer.getTileX(), adventurer.getTileY());
    return false;
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
