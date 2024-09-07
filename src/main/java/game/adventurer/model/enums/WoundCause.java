package game.adventurer.model.enums;

import lombok.Getter;

@Getter
public enum WoundCause {
  WOODS("Bois infranchissables"),
  MONSTER("Monstre");

  private final String causeName;

  WoundCause(String causeName) {
    this.causeName = causeName;
  }
}
