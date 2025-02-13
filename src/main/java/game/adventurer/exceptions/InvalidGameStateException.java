package game.adventurer.exceptions;

public class InvalidGameStateException extends Exception {

  public InvalidGameStateException(String message) {
    super(message);
  }

  public InvalidGameStateException(String message, Throwable cause) {
    super(message, cause);
  }
}
