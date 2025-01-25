package game.adventurer.model;

import static game.adventurer.util.MiscUtil.isOutOfMapBounds;

import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.MovementHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class CreatureMovementHandler implements MovementHandler {

  private final GameMap gameMap;

  public CreatureMovementHandler(GameMap gameMap) {
    this.gameMap = gameMap;
  }

  @Override
  public boolean isAllowedTile(int x, int y, Creature creature) {
    // returns true when the tile is not out of the map boundaries and the Tile.Type is allowed for this creature
    boolean result;
    if (isOutOfMapBounds(gameMap, x, y)) {
      result = false;
      log.debug("test isAllowedTile pout testMugger: {}, raison : Out of bounds", false);
    } else {
      result = creature.getAllowedTileTypes().contains(gameMap.getTileTypeAt(x, y));
      log.debug("test isAllowedTile pout testMugger: {}, type de tile : {}", result, getGameMap().getTileTypeAt(x, y));
    }

    return result;
  }

}
