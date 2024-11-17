package game.adventurer.util;

import game.adventurer.config.AppConfig;
import java.util.Objects;
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
}
