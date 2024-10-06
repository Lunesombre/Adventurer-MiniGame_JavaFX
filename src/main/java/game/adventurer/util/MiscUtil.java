package game.adventurer.util;

public class MiscUtil {

  private MiscUtil() {
  }

  public static String sanitizeString(String input, int maxLength) {
    if (input == null || input.length() > maxLength || input.isEmpty()) {
      throw new IllegalArgumentException("Invalid string length");
    }
    return input.replaceAll("[^a-zA-Z0-9_\\- ]", "");
  }
}
