package game.adventurer.model.creature;

public interface MovementHandler {

  boolean isAllowedTile(int x, int y, Creature creature);
}
