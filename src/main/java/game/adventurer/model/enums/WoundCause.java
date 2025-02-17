package game.adventurer.model.enums;

import lombok.Getter;

@Getter
public enum WoundCause {
  WOODS("wound.cause.woods"),
  MONSTER("wound.cause.monster"),
  MUGGER("wound.cause.mugger"),
  SNIFFER("wound.cause.sniffer"),
  LURKER("wound.cause.lurker");

  private final String causeNameKey;

  WoundCause(String causeNameKey) {
    this.causeNameKey = causeNameKey;
  }
}
