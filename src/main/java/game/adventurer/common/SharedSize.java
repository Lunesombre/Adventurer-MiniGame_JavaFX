package game.adventurer.common;


import lombok.Getter;
import lombok.Setter;

/**
 * Represents a shared size object used to maintain consistent dimensions across different scenes in the application.
 *
 * <p>This class is designed to store and update width and height values
 * that can be shared between multiple components, particularly useful for maintaining consistent sizing between different JavaFX scenes.</p>
 *
 * <p>The dimensions are stored as double values to allow for precise
 * sizing and compatibility with JavaFX's dimension system.</p>
 */
@Getter
@Setter
public class SharedSize {

  // doubles as JavaFX uses doubles for Scene dimensions too
  private double width;
  private double height;

  public SharedSize(double width, double height) {
    this.width = width;
    this.height = height;
  }
}
