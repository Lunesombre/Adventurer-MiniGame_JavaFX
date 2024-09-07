package game.adventurer.model.enums;

import lombok.Getter;

@Getter
public enum MapSize {

  SMALL(10), MEDIUM(20), LARGE(40);

  private final int size;

  MapSize(int size) {
    this.size = size;
  }

}
