package game.adventurer.util;

import game.adventurer.config.AppConfig;
import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.GameMap;
import game.adventurer.model.Position;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.creature.Adventurer;
import game.adventurer.model.creature.Creature;
import game.adventurer.model.creature.Monster;
import game.adventurer.model.enums.Direction;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class MiscUtil {

  private MiscUtil() {
  }

  public static String sanitizeString(String input, int maxLength) {
    if (input == null || input.length() > maxLength || input.isEmpty()) {
      throw new IllegalArgumentException("Invalid string length");
    }
    return input.replaceAll("[^a-zA-Z0-9_\\- ]", "");
  }

  public static void applyGlobalCss(Parent parent) {
    String cssPath = AppConfig.getInstance().getGlobalStylePath();
    parent.getStylesheets().add(Objects.requireNonNull(parent.getClass().getResource(cssPath)).toExternalForm());
  }

  public static void applyGlobalCss(Scene scene) {
    String cssPath = AppConfig.getInstance().getGlobalStylePath();
    scene.getStylesheets().add(Objects.requireNonNull(scene.getClass().getResource(cssPath)).toExternalForm());
  }


  /**
   * Initializes and configures an Alert dialog with customizable properties.
   *
   * @param clazz         The Class object used to load resources (e.g., icons)
   * @param alertType     The type of the alert (e.g., INFORMATION, WARNING, ERROR). If null, NONE is used.
   * @param title         The title of the alert dialog. If null or blank, an empty string is used.
   * @param headerText    The header text of the alert dialog. If null or blank, an empty string is used.
   * @param cssClass      Additional CSS class to be applied to the alert dialog. If null or blank, no additional class is added.
   * @param iconPath      The path to the icon resource. If null or blank, no icon is set.
   * @param iconInContent If true and an icon is provided, the icon will also be displayed in the alert's content area.
   * @return A configured Alert object ready to be shown.
   * @throws NullPointerException if the icon resource cannot be found at the specified path.
   */
  public static Alert alertInitializer(
      Class<?> clazz, AlertType alertType, String title, String headerText,
      String cssClass, String iconPath, boolean iconInContent) {
    if (alertType == null) {
      alertType = AlertType.NONE;
    }
    Alert alert = new Alert(alertType);
    if (title == null || title.isBlank()) {
      title = "";
    }
    alert.setTitle(title);
    if (headerText == null || headerText.isBlank()) {
      headerText = "";
    }
    alert.setHeaderText(headerText);
    applyGlobalCss(alert.getDialogPane());
    if (cssClass != null && !cssClass.isBlank()) {
      alert.getDialogPane().getStyleClass().add(cssClass);
    }

    if (iconPath != null && !iconPath.isBlank()) {
      Image icon = new Image(Objects.requireNonNull(clazz.getResourceAsStream(iconPath)));
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(icon);
      if (iconInContent) {
        ImageView iconView = new ImageView(icon);
        iconView.setPreserveRatio(true);
        iconView.setFitWidth(80);
        alert.getDialogPane().setGraphic(iconView);
      }
    }

    return alert;
  }

  public static Node copyNode(Node original) {
    if (original instanceof ImageView originalImageView) {
      return new ImageView(originalImageView.getImage());
    } else if (original instanceof Label originalLabel) {
      Label newLabel = new Label(originalLabel.getText());
      newLabel.setGraphic(originalLabel.getGraphic()); // Keep the graphic
      newLabel.setStyle(originalLabel.getStyle()); // Keep the style
      return newLabel;
    }
    // Add other Node types as necessary
    throw new RuntimeException("Type of Node not yet supported");
  }

  public static void handleInvalidGameState(Class<?> clazz, InvalidGameStateException e) {
    Platform.runLater(() -> {
      // Initialize the alert
      Alert alert = alertInitializer(clazz, AlertType.ERROR, "Critical Error", "Error: " + e.getMessage(), "alert",
          "/assets/icons/crane-et-os.png", true);

      // Defines alert content (OK button is created by the AlertType.ERROR)
      alert.setContentText("The game cannot continue due to an invalid state. Game will close upon closing this alert.");

      alert.showAndWait();
      Platform.exit(); // Clean exit
      System.exit(0);
    });
  }


  /**
   * Calculates the field of view for a creature on the game map using a modified Bresenham's line algorithm. This method determines which tiles are
   * visible to the creature based on its position and the map layout.
   *
   * @param creature The creature for which to calculate the field of view.
   * @param gameMap  The game map containing the tiles and obstacles.
   * @return A set of Position objects representing the visible tiles.
   */
  public static Set<Position> calculateFieldOfView(Creature creature, GameMap gameMap) {
    Set<Position> visibleTiles = new HashSet<>();
    Position origin = new Position(creature.getTileX(), creature.getTileY());
    visibleTiles.add(origin); // The creature's position is always "visible" to her

    // TODO: update when new Creatures are ... created, hehe
    int maxDistance = switch (creature) {
      case Adventurer ignored -> 5; // 5 as 5 is the max an Adventurer can "see" in the best direction (frontward)
      case Monster ignored -> 8;
      default -> throw new IllegalStateException("Unexpected value: " + creature);
    };

    // Iterate through all tiles within the maximum view distance
    for (int dx = -maxDistance; dx <= maxDistance; dx++) {
      for (int dy = -maxDistance; dy <= maxDistance; dy++) {
        if (dx == 0 && dy == 0) {
          continue; // Skip the creature's own position
        }

        Position targetPosition = new Position(origin.x() + dx, origin.y() + dy);
        // Check if the target position is within the map boundaries
        if (isOutOfMapBounds(gameMap, targetPosition.x(), targetPosition.y())) {
          continue;
        }
        // Check if the target is visible and within the creature's view distance
        if (isVisible(origin, targetPosition, gameMap) &&
            getDistance(origin, targetPosition) <= getMaxVisibleDistanceForCreature(targetPosition, origin, creature)) {
          visibleTiles.add(targetPosition);
        }
      }
    }

    return visibleTiles;
  }

  /**
   * Determines if a target position is visible from an origin position using a modified Bresenham's line algorithm. This method takes into account
   * obstacles (WOOD tiles) and checks for diagonal visibility.
   *
   * @param origin  The starting position.
   * @param target  The target position to check for visibility.
   * @param gameMap The game map containing the tiles and obstacles.
   * @return true if the target is visible from the origin, false otherwise.
   */
  private static boolean isVisible(Position origin, Position target, GameMap gameMap) {
    int originX = origin.x();
    int originY = origin.y();
    int targetX = target.x();
    int targetY = target.y();
    int deltaX = Math.abs(targetX - originX);
    int deltaY = Math.abs(targetY - originY);
    int stepX = originX < targetX ? 1 : -1;
    int stepY = originY < targetY ? 1 : -1;
    int error = deltaX - deltaY;

    // keep going until we reached target from origin, unless we return a boolean on the way
    while (originX != targetX || originY != targetY) {
      // Check if the current position is within map boundaries, also protects usage of getTileTypeAt() from a potential out of bounds exception
      if (isOutOfMapBounds(gameMap, originX, originY)) {
        return false;
      }
      // Check if the current tile is blocking (WOOD)
      if (gameMap.getTileTypeAt(originX, originY) == Type.WOOD) {
        return false;
      }

      // Check for diagonal visibility
      if (originX != targetX && originY != targetY) {
        // We're moving diagonally
        boolean horizontalBlocked = gameMap.getTileTypeAt(originX + stepX, originY) == Type.WOOD;
        boolean verticalBlocked = gameMap.getTileTypeAt(originX, originY + stepY) == Type.WOOD;

        // If both adjacent directions are blocked, block the diagonal
        if (horizontalBlocked && verticalBlocked) {
          return false;
        }
      }

      // Bresenham's line algorithm core
      int doubledError = 2 * error; // this way we never use float numbers
      if (doubledError > -deltaY) { // should "pixel" be "moved" horizontally?
        error -= deltaY; // then adjust error
        originX += stepX; // and increment originX in the appropriate direction
      }
      if (doubledError < deltaX) { // should "pixel" be "moved" vertically?
        error += deltaX;
        originY += stepY;
      }
    }

    return true; // The target is visible
  }


  /**
   * Calculates the Manhattan distance between two positions.
   *
   * @param p1 The first position.
   * @param p2 The second position.
   * @return The Manhattan distance between the two positions.
   */
  private static int getDistance(Position p1, Position p2) {
    return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
  }

  /**
   * Determines the maximum visible distance for a creature based on its facing direction and the direction to the target.
   *
   * @param currentlyCheckedPosition The position being checked for visibility.
   * @param startPosition            The starting position of the creature.
   * @param creature                 The creature whose field of view is being calculated.
   * @return The maximum distance the creature can see in the direction of the checked position.
   */
  private static int getMaxVisibleDistanceForCreature(Position currentlyCheckedPosition, Position startPosition, Creature creature) {
    Direction facingDirection = creature.getFacingDirection();

    Direction currentDirection = getDirectionBetween(startPosition, currentlyCheckedPosition);
    switch (creature) {
      case Adventurer ignored -> {
        if (currentDirection == facingDirection) {
          return 5; // Frontward
        } else if (currentDirection == facingDirection.getOpposite()) {
          return 3; // Backward
        } else {
          return 4; // Sides
        }
      }
      default -> {
        return -1;
      }
    }
  }

  /**
   * Determines the primary direction between two positions.
   *
   * @param start The starting position.
   * @param end   The ending position.
   * @return The primary direction (NORTH, SOUTH, EAST, or WEST) from start to end.
   */
  private static Direction getDirectionBetween(Position start, Position end) {
    int dx = end.x() - start.x();
    int dy = end.y() - start.y();

    if (Math.abs(dx) > Math.abs(dy)) {
      return dx > 0 ? Direction.EAST : Direction.WEST;
    } else {
      return dy > 0 ? Direction.SOUTH : Direction.NORTH;
    }
  }

  /**
   * Checks if a given coordinate is outside the boundaries of the game map.
   *
   * @param gameMap The game map to check against.
   * @param x       The x-coordinate to check.
   * @param y       The y-coordinate to check.
   * @return true if the coordinate is out of bounds, false otherwise.
   */
  public static boolean isOutOfMapBounds(GameMap gameMap, int x, int y) {
    return x < 0 || x >= gameMap.getMapWidth() || y < 0 || y >= gameMap.getMapHeight();
  }
}
