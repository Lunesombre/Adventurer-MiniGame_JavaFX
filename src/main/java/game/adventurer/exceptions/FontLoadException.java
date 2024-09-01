package game.adventurer.exceptions;

public class FontLoadException extends Exception {

  public FontLoadException(String message) {
    super(message);
  }

  public FontLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
