package game.adventurer.ui.common;

import game.adventurer.model.enums.Direction;
import javafx.animation.RotateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

/**
 * Represents an equilateral triangle centered at the intersection of its medians.
 */

public class TriangleCreatureRepresentation extends Polygon {


  private final DoubleProperty sideLength = new SimpleDoubleProperty();
  private Direction facingDirection;

  /**
   * Creates an equilateral triangle with the specified side length and direction. The triangle is centered at (0,0), meaning its centroid is at the
   * origin.
   *
   * @param initialSideLength The initial length of each side of the triangle.
   * @param facingDirection   The initial direction the triangle is facing.
   */
  public TriangleCreatureRepresentation(double initialSideLength, Direction facingDirection) {
    this.sideLength.set(initialSideLength);
    updatePoints(); // Initial call

    // Listener that updates points whenever sideLength changes
    sideLength.addListener((obs, oldVal, newVal) -> updatePoints());

    setRotation(facingDirection, false);
  }

  public double getSideLength() {
    return sideLength.get();
  }

  public void setSideLength(double sideLength) {
    this.sideLength.set(sideLength);
  }

  public DoubleProperty sideLengthProperty() {
    return sideLength;
  }

  /**
   * Updates the triangle's points based on the current side length.
   */
  private void updatePoints() {
    double height = (Math.sqrt(3) / 2) * sideLength.get();
    double halfBase = sideLength.get() / 2;

    getPoints().setAll(
        -halfBase, height / 3,    // Bottom-left vertex
        halfBase, height / 3,    // Bottom-right vertex
        0.0, -2 * height / 3     // Top vertex
    );

    // Adjust vertical anchor so that center coincides with barycenter
    double verticalOffset = height / 6;
    setTranslateY(verticalOffset);
  }

  /**
   * Rotates smoothly towards a new facing direction.
   *
   * @param newDirection The new direction to face.
   */
  public void setRotation(Direction newDirection) {
    setRotation(newDirection, true);
  }

  public void setRotation(Direction newDirection, boolean animated) {
    if (this.facingDirection == newDirection) {
      return;
    }

    double targetAngle = switch (newDirection) {
      case NORTH -> 0;
      case EAST -> 90;
      case SOUTH -> 180;
      case WEST -> 270;
    };

    // Apply animated rotation if needed la rotation
    if (animated) {
      // Retrieve current angle
      double currentAngle = this.getRotate();

      final RotateTransition rotateTransition = getRotateTransition(targetAngle, currentAngle);
      rotateTransition.play();
    } else {
      setRotate(targetAngle);
    }

    this.facingDirection = newDirection;
  }

  private RotateTransition getRotateTransition(double targetAngle, double currentAngle) {
    // Clockwise and counter-clockwise angle calculation
    double clockwiseAngle = targetAngle - currentAngle;
    if (clockwiseAngle < 0) {
      clockwiseAngle += 360; // Make the angle positive if necessary
    }
    double counterClockwiseAngle = 360 - clockwiseAngle;

    // Choose the shortest angle (smallest number)
    double angleToRotate = (Math.abs(clockwiseAngle) < Math.abs(counterClockwiseAngle)) ? clockwiseAngle : -counterClockwiseAngle;
    RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), this);
    rotateTransition.setByAngle(angleToRotate);
    return rotateTransition;
  }
}
