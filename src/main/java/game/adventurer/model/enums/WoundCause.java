package game.adventurer.model.enums;

import lombok.Getter;

@Getter
public enum WoundCause {
  WOODS("wound.cause.woods"),
  MONSTER("wound.cause.monster");

  private final String causeNameKey;

  WoundCause(String causeNameKey) {
    this.causeNameKey = causeNameKey;
  }
}
