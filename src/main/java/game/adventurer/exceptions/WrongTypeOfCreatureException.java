package game.adventurer.exceptions;

public class WrongTypeOfCreatureException extends InvalidGameStateException {

  public WrongTypeOfCreatureException(String message) {
    super(message);
  }

  public WrongTypeOfCreatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
