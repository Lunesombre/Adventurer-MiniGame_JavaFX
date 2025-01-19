package game.adventurer.model.enums;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum Move {
  UP(0, -1),
  DOWN(0, 1),
  LEFT(-1, 0),
  RIGHT(1, 0);

  private final int dx;
  private final int dy;

  Move(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
  }
}
