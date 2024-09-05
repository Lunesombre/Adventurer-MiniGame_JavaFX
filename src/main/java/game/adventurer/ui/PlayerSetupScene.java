package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.MapSize;
import game.adventurer.ui.common.BaseScene;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class PlayerSetupScene extends BaseScene {

  private TextField adventurerNameField;
  private ToggleGroup mapSizeGroup;
  private Button startButton;
  @Getter
  private String playerName;
  private MapSize selectedMapSize;


  public PlayerSetupScene(SharedSize sharedSize) {
    super(new VBox(), sharedSize);
    initialize();
  }

  @Override
  protected void initialize() {
    Label errorLabel;
    VBox root = (VBox) getRoot();
    root.setAlignment(Pos.CENTER);
    root.setSpacing(20);
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: #403f3f;");

    Label titleLabel = new Label("Adventurer Setup");
    titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #46a7b3;");
    titleLabel.getStyleClass().add("bold-text");

    adventurerNameField = new TextField();
    adventurerNameField.setPromptText("Enter Adventurer Name");
    adventurerNameField.setStyle(
        "-fx-font-size: 24px; -fx-text-fill: #7a8181; -fx-prompt-text-fill: lightgray; -fx-max-width: 400; -fx-background-radius: 15px;");

    errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: red;");
    errorLabel.setVisible(false); // Default : hidden

    Label mapSizeLabel = new Label("Choose Map Size:");
    mapSizeLabel.setStyle(" -fx-font-size: 24px; -fx-text-fill: #747777; -fx-font-weight: bold");
    mapSizeGroup = new ToggleGroup();
    RadioButton smallMap = new RadioButton("Small (10x10)");
    String radioStyles = "-fx-font-size: 18px; -fx-text-fill: #747777";
    smallMap.setStyle(radioStyles);
    RadioButton mediumMap = new RadioButton("Medium (20x20)");
    mediumMap.setStyle(radioStyles);
    RadioButton largeMap = new RadioButton("Large (40x40)");
    largeMap.setStyle(radioStyles);
    smallMap.setToggleGroup(mapSizeGroup);
    mediumMap.setToggleGroup(mapSizeGroup);
    largeMap.setToggleGroup(mapSizeGroup);
    mediumMap.setSelected(true);

    // HBox to put all radios on the same line
    HBox mapSizeBox = new HBox(20); // 20 : spacing between elements
    mapSizeBox.setAlignment(Pos.CENTER);
    mapSizeBox.getChildren().addAll(smallMap, mediumMap, largeMap);

    startButton = new Button("Start Adventure");

    root.getChildren().addAll(titleLabel, adventurerNameField, errorLabel, mapSizeLabel, mapSizeBox, startButton);

    // Adding listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());
    // Listener for Adventurer's name validation
    adventurerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      boolean isValid = isValidAdventurerName(newValue);
      startButton.setDisable(!isValid);
      if (!isValid) {
        adventurerNameField.setStyle(
            "-fx-font-size: 24px; -fx-text-fill: #7a8181; -fx-prompt-text-fill: lightgray; -fx-max-width: 400; -fx-border-color: red; -fx-border-width: 2px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        errorLabel.setText(getValidationMessage(newValue));
        errorLabel.setVisible(true);
      } else {
        adventurerNameField.setStyle(
            "-fx-font-size: 24px; -fx-text-fill: #7a8181; -fx-prompt-text-fill: lightgray; -fx-max-width: 400; -fx-border-color: #2ef32e; -fx-border-width: 2px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        errorLabel.setVisible(false);
      }
    });
  }


  private MapSize getSelectedMapSize() {
    RadioButton selectedButton = (RadioButton) mapSizeGroup.getSelectedToggle();
    if (selectedButton.getText().contains("Small")) {
      return MapSize.SMALL;
    } else if (selectedButton.getText().contains("Large")) {
      return MapSize.LARGE;
    } else {
      return MapSize.MEDIUM;
    }
  }

  public void setOnStartGame(BiConsumer<String, MapSize> action) {
    startButton.setOnAction(e -> {
      playerName = adventurerNameField.getText().trim();
      playerName = playerName.isEmpty() ? "Michel" : playerName;
      if (isValidAdventurerName(playerName)) {
        selectedMapSize = getSelectedMapSize();
        action.accept(playerName, selectedMapSize);
      } else {
        showAlert("Veuillez saisir un nom valide (1-20 caractères, lettres, chiffres, espaces et traits d'union uniquement).");
      }

    });
  }


  private boolean isValidAdventurerName(String name) {
    if (name.isEmpty() || name.length() > 20) {
      return false;
    }
    return name.matches("^[a-zA-Z0-9 -]+$");
  }

  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Nom non-autorisé");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private String getValidationMessage(String name) {
    if (name.isEmpty()) {
      return "Name cannot be empty.";
    }
    if (name.length() > 20) {
      return "Name cannot exceed 20 characters.";
    }
    if (!name.matches("^[a-zA-Z0-9 -]+$")) {
      return "Name can only contain letters, numbers, spaces, and hyphens.";
    }
    return ""; // No errors
  }

}
