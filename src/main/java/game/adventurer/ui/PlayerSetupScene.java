package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.MapSize;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class PlayerSetupScene extends Scene {

  private final SharedSize sharedSize;
  private TextField adventurerNameField;
  private ToggleGroup mapSizeGroup;
  private Button startButton;
  @Getter
  private String playerName;
  private MapSize selectedMapSize;

  public PlayerSetupScene(SharedSize sharedSize) {
    super(new VBox(), sharedSize.getWidth(), sharedSize.getHeight());
    this.sharedSize = sharedSize;
    initialize();
  }

  private void initialize() {
    VBox root = (VBox) getRoot();
    root.setAlignment(Pos.CENTER);
    root.setSpacing(20);
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: #403f3f;");

    Label titleLabel = new Label("Adventurer Setup");
    titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #46a7b3");

    adventurerNameField = new TextField();
    adventurerNameField.setPromptText("Enter Adventurer Name");
    adventurerNameField.setStyle("-fx-font-size: 24px; -fx-text-fill: #7a8181; -fx-prompt-text-fill: lightgray; -fx-max-width: 400");

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
    startButton.setStyle("-fx-max-width: 400; -fx-font-size:24px; -fx-font-weight: bold; -fx-background-color: #699521");

    root.getChildren().addAll(titleLabel, adventurerNameField, mapSizeLabel, mapSizeBox, startButton);

    // Adding listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());
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
      playerName = adventurerNameField.getText().isEmpty() ? "Michel" : adventurerNameField.getText();
      selectedMapSize = getSelectedMapSize();
      action.accept(playerName, selectedMapSize);
    });
  }

  private void updateSize() {
    sharedSize.setWidth(getWidth());
    sharedSize.setHeight((getHeight()));
  }

}
