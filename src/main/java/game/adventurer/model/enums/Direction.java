package game.adventurer.model.enums;

public enum Direction {
  NORTH {
    @Override
    public Direction getOpposite() {
      return SOUTH;
    }
  },
  SOUTH {
    @Override
    public Direction getOpposite() {
      return NORTH;
    }
  },
  EAST {
    @Override
    public Direction getOpposite() {
      return WEST;
    }
  },
  WEST {
    @Override
    public Direction getOpposite() {
      return EAST;
    }
  };

  public abstract Direction getOpposite();

  public Direction turnQuarterClockwise() {
    return values()[(this.ordinal() + 1) % values().length];
  }

  public Direction turnQuarterCounterClockwise() {
    return values()[(this.ordinal() - 1 + values().length) % values().length];
  }
}
