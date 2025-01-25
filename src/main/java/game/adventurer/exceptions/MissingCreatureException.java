package game.adventurer.exceptions;

public class MissingCreatureException extends InvalidGameStateException {

  public MissingCreatureException(String message) {
    super(message);
  }
}
