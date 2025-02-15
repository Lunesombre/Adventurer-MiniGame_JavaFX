package game.adventurer.model.creature;

import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.Direction;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public abstract class Creature {

  protected String name;
  protected int tileX; // Position tileX on the game map
  protected int tileY; // Position tileY on the game map
  protected IntegerProperty health; // Number of life points of a Creature, observable
  protected int moveSpeed; // unused for now, but soon
  protected long lastMoveTime = 0;
  protected int previousTileX;
  protected int previousTileY;
  protected Set<Type> allowedTileTypes;
  protected ObjectProperty<Direction> facingDirection = new SimpleObjectProperty<>();
  protected MovementHandler movementHandler;
  protected Set<Position> visibleTiles = new HashSet<>();

  private static final int DEFAULT_HEALTH = 10;
  private static final int DEFAULT_MOVE_SPEED = 1;


  protected Creature(String name, int tileX, int tileY, int health, int moveSpeed) {
    this.name = name;
    this.tileX = tileX;
    this.tileY = tileY;
    this.health = new SimpleIntegerProperty(health);
    this.moveSpeed = moveSpeed;
    this.facingDirection.set(Direction.values()[new Random().nextInt(Direction.values().length)]); // creates a random facing Direction
  }

  protected Creature(String name, int tileX, int tileY) {
    this(name, tileX, tileY, DEFAULT_HEALTH, DEFAULT_MOVE_SPEED);
  }

  // Getter of the observable health property
  public IntegerProperty healthProperty() {
    return health;
  }

  // Regular health getter (returns an int)
  public int getHealth() {
    return health.get();
  }

  // Setter for health
  public void setHealth(int health) {
    this.health.set(health);
  }

  public ObjectProperty<Direction> facingDirectionProperty() {
    return facingDirection;
  }

  public Direction getFacingDirection() {
    return facingDirection.get();
  }

  public void setFacingDirection(Direction direction) {
    this.facingDirection.set(direction);
  }

}
