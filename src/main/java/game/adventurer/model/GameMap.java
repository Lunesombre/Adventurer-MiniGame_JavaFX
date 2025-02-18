package game.adventurer.model;

import static game.adventurer.util.MiscUtil.handleInvalidGameState;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.exceptions.WrongTypeOfCreatureException;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.enums.Move;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.model.wound.Wound;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.service.WoundManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class GameMap {

  private Tile[][] grid;
  private int mapWidth;
  private int mapHeight;
  private Adventurer adventurer;
  private Treasure treasure;
  private List<Monster> monsters = new ArrayList<>();
  private List<Wound> woundsList = new ArrayList<>();
  private Set<Position> occupiedTiles = new HashSet<>();
  private final WoundManager woundManager;

  public GameMap(Tile[][] grid, int mapWidth, int mapHeight, Adventurer adventurer, Treasure treasure) {
    this.grid = grid;
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
    this.adventurer = adventurer;
    this.treasure = treasure;
    this.woundManager = new WoundManager(woundsList);
  }

  public MoveResult moveAdventurer(Move move) {
    try {
      if (isOutOfMapBounds(adventurer.getTileX(), adventurer.getTileY())) {
        log.error("Adventurer's current coordinates ({}, {}) are invalid!",
            adventurer.getTileX(), adventurer.getTileY());
        throw new InvalidGameStateException(LocalizedMessageService.getInstance().getMessage("error.impossibleAdventurerLocation"));
      }
    } catch (InvalidGameStateException e) {
      handleInvalidGameState(getClass(), e);
    }
    int newX = adventurer.getTileX() + move.getDx();
    int newY = adventurer.getTileY() + move.getDy();

    // First, check if the move is within the map bounds
    if (isOutOfMapBounds(newX, newY)) {
      log.warn("Vous ne pouvez pas quitter la carte sans le trésor !");
      return MoveResult.OUT_OF_BOUNDS;
    }

    // Then, check if it's a valid move within the map.
    if (!isValidMove(newX, newY, adventurer)) {
      // If move ain't valid, verifies if it's because of a Type.WOOD Tile
      if (grid[newY][newX].getType() == Type.WOOD) {
        woundManager.createWound(adventurer);
        return MoveResult.WOUNDED;
      } else {
        log.info("Mouvement invalide, l'aventurier reste en ({}, {})",
            adventurer.getTileX(), adventurer.getTileY());
        return MoveResult.BLOCKED;
      }

    }
    // Checking the monsters' list to see if new position is currently occupied by a monster
    Optional<Monster> monster = monsters.stream()
        .filter(ms -> ms.getCurrentPosition().equals(new Position(newX, newY)))
        .findFirst();
    if (monster.isPresent()) {
      try {
        woundManager.createWound(monster.get(), adventurer, false);
        return MoveResult.WOUNDED;
      } catch (WrongTypeOfCreatureException e) {
        handleInvalidGameState(this.getClass(), e);
      }
    }

    // If everything else is ok, try to proceed with the move.
    boolean hasMoved = adventurer.move(move);
    // If move call wasn't too early, the Adventurer shall move, else return BLOCKED
    return hasMoved ? MoveResult.MOVED : MoveResult.BLOCKED;
  }

  private boolean isValidMove(int x, int y, Creature creature) {
    switch (creature) {
      case Adventurer ignored -> {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight && getTileTypeAt(x, y) == Type.PATH;
      }
      case Monster ignored -> {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
      }
      default -> {
        return false;
      }
    }
  }

  /**
   * Checks if a given position is valid for a specific creature on the game map.
   *
   * <p>The validity of a position depends on the type of creature.
   *
   * @param position The position to check for validity.
   * @param creature The creature for which the position is being checked.
   * @return true if the position is valid for the given creature, false otherwise.
   */
  public boolean isValidPosition(Position position, Creature creature) {
    switch (creature) {
      case Adventurer ignored -> {
        return position.x() >= 0 && position.x() < mapWidth && position.y() >= 0 && position.y() < mapHeight
            && getTileTypeAt(position.x(), position.y()) == Type.PATH;
      }
      case Monster monster -> {
        return position.x() >= 0 && position.x() < mapWidth && position.y() >= 0 && position.y() < mapHeight
            && monster.getAllowedTileTypes().contains(getTileTypeAt(position.x(), position.y()));
      }
      default -> {
        return false;
      }
    }
  }

  private boolean isOutOfMapBounds(int x, int y) {
    return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
  }

  public Type getTileTypeAt(int x, int y) {
    return Objects.requireNonNull(grid[y][x].getType(), "Tile type cannot be null");
  }

  public void addMonster(Monster monster) {
    monsters.add(monster);
  }

  public boolean isTileOccupied(int x, int y) {
    return occupiedTiles.contains(new Position(x, y));
  }

  // Methods to add/remove a Tile from occupiedTiles:
  public void occupyTile(Position position) {
    occupiedTiles.add(position);
  }

  public void freeTile(Position position) {
    boolean hasFreed = occupiedTiles.remove(position);
    if (!hasFreed) {
      log.warn("Cannot free : {} as not found in {} ", position, occupiedTiles);
    } else {
      log.trace("YAY ! Tile freed {}", position);
    }
  }

}
