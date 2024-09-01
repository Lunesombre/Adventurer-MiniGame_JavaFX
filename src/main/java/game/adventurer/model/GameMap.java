package game.adventurer.model;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GameMap {

  private Tile[][] grid;
  private int mapWidth;
  private int mapHeight;
  private Adventurer adventurer;
  private Treasure treasure;

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
