package game.adventurer.model;

import static game.adventurer.util.MiscUtil.handleInvalidGameState;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.base.Creature;
import game.adventurer.model.base.Wound;
import game.adventurer.model.enums.Move;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.model.enums.WoundCause;
import game.adventurer.service.LocalizedMessageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public MoveResult moveAdventurer(Move move) {
    try {
      if (isOutOfMapBounds(adventurer.getTileX(), adventurer.getTileY())) {
        LOG.error("Adventurer's current coordinates ({}, {}) are invalid!",
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
      LOG.warn("Vous ne pouvez pas quitter la carte sans le trésor !");
      return MoveResult.OUT_OF_BOUNDS;
    }

    // Then, check if it's a valid move within the map.
    if (!isValidMove(newX, newY, adventurer)) {
      // If move ain't valid, verifies if it's because of a Type.WOOD Tile
      if (grid[newY][newX].getType() == Type.WOOD) {
        WoodsWound wound = new WoodsWound(WoundCause.WOODS);
        wound.setWoundsMessage(adventurer);
        adventurer.setHealth(adventurer.getHealth() - wound.getHealthCost());
        woundsList.add(wound);
        return MoveResult.WOUNDED;
      } else {
        LOG.info("Mouvement invalide, l'aventurier reste en ({}, {})",
            adventurer.getTileX(), adventurer.getTileY());
        return MoveResult.BLOCKED;
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
      case Monster ignored -> {
        return position.x() >= 0 && position.x() < mapWidth && position.y() >= 0 && position.y() < mapHeight;
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

}
