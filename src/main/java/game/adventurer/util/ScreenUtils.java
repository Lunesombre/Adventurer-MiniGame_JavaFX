package game.adventurer.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenUtils {

  private ScreenUtils() {
  }

  public static Rectangle2D getScreenSize() {
    Screen primaryScreen = Screen.getPrimary();
    return primaryScreen.getVisualBounds();
  }

  public static double getScreenWidth() {
    return getScreenSize().getWidth();
  }

  public static double getScreenHeight() {
    return getScreenSize().getHeight();
  }
}
