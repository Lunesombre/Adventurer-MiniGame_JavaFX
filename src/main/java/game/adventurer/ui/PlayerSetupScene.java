package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MapSize;
import game.adventurer.service.HighScoreManager;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.ui.common.ScoreBoard;
import game.adventurer.ui.common.TransitionScene;
import game.adventurer.util.TriConsumer;
import java.util.Arrays;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerSetupScene extends BaseScene {

  private TextField adventurerNameField;
  private ToggleGroup mapSizeGroup;
  private ToggleGroup difficultyToggleGroup;
  private Button startButton;
  @Getter
  private String playerName;
  private MapSize selectedMapSize;
  private DifficultyLevel selectedDifficultyLevel;

  private final HighScoreManager highScoreManager;
  private ScoreBoard scoreBoard;
  private Text toggleText;


  public PlayerSetupScene(SharedSize sharedSize, HighScoreManager highScoreManager) {
    super(new StackPane(), sharedSize);
    this.highScoreManager = highScoreManager;
    initialize();
  }

  @Override
  protected void initialize() {
    Label errorLabel;
    StackPane root = (StackPane) getRoot();
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: #403f3f;");
    VBox mainContent = new VBox(20);
    mainContent.setAlignment(Pos.CENTER);

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
    RadioButton smallMap = new RadioButton("Small");
    String radioStyles = "-fx-font-size: 18px; -fx-text-fill: #747777";
    smallMap.setStyle(radioStyles);
    RadioButton mediumMap = new RadioButton("Medium");
    mediumMap.setStyle(radioStyles);
    RadioButton largeMap = new RadioButton("Large");
    largeMap.setStyle(radioStyles);
    smallMap.setToggleGroup(mapSizeGroup);
    mediumMap.setToggleGroup(mapSizeGroup);
    largeMap.setToggleGroup(mapSizeGroup);
    mediumMap.setSelected(true);
    //Tooltip for map sizes
    Tooltip smallMapTooltip = new Tooltip("Petite carte : 10x10 cases");
    Tooltip mediumMapTooltip = new Tooltip("Carte moyenne : 20x20 cases");
    Tooltip largeMapTooltip = new Tooltip("Grande carte : 40x40 cases");
    smallMap.setTooltip(smallMapTooltip);
    mediumMap.setTooltip(mediumMapTooltip);
    largeMap.setTooltip(largeMapTooltip);

    // HBox to put all radios on the same line
    HBox mapSizeBox = new HBox(20); // 20 : spacing between elements
    mapSizeBox.setMinWidth(400);
    mapSizeBox.setMaxWidth(500);
    mapSizeBox.setAlignment(Pos.CENTER);
    Region spacer1 = new Region();
    Region spacer2 = new Region();
    HBox.setHgrow(spacer1, Priority.ALWAYS);
    HBox.setHgrow(spacer2, Priority.ALWAYS);
    mapSizeBox.getChildren().addAll(smallMap, spacer1, mediumMap, spacer2, largeMap);

    // HBox for difficulty modes radios buttons
    HBox difficultyModeBox = new HBox(20);
    difficultyModeBox.setMinWidth(400);
    difficultyModeBox.setMaxWidth(500);
    Label difficultyModeLabel = new Label("Choose difficulty mode:");
    difficultyModeLabel.setStyle(" -fx-font-size: 24px; -fx-text-fill: #747777; -fx-font-weight: bold");
    difficultyToggleGroup = new ToggleGroup();
    RadioButton easyMode = new RadioButton("Easy");
    easyMode.setStyle(radioStyles);
    RadioButton mediumMode = new RadioButton("Normal");
    mediumMode.setStyle(radioStyles);
    RadioButton hardMode = new RadioButton("Hard");
    hardMode.setStyle(radioStyles);
    Tooltip easyModeTooltip = new Tooltip("Position du trésor connue dès le départ, marquée d'une croix rouge.");
    Tooltip normalModeTooltip = new Tooltip("Direction générale du trésor donnée, vous le verrez à proximité.");
    Tooltip hardModeTooltip = new Tooltip("Direction générale du trésor donnée, il sera plus dur à trouver");
    easyMode.setTooltip(easyModeTooltip);
    mediumMode.setTooltip(normalModeTooltip);
    hardMode.setTooltip(hardModeTooltip);
    for (Tooltip tooltip : Arrays.asList(easyModeTooltip, normalModeTooltip, hardModeTooltip,
        smallMapTooltip, mediumMapTooltip, largeMapTooltip)) {
      tooltip.setShowDelay(Duration.millis(70));
      tooltip.setHideDelay(Duration.millis(70));
    }
    easyMode.setToggleGroup(difficultyToggleGroup);
    mediumMode.setToggleGroup(difficultyToggleGroup);
    hardMode.setToggleGroup(difficultyToggleGroup);
    mediumMode.setSelected(true);
    difficultyModeBox.setAlignment(Pos.CENTER);
    Region spacer3 = new Region();
    Region spacer4 = new Region();
    HBox.setHgrow(spacer3, Priority.ALWAYS);
    HBox.setHgrow(spacer4, Priority.ALWAYS);
    difficultyModeBox.getChildren().addAll(easyMode, spacer3, mediumMode, spacer4, hardMode);

    startButton = new Button("Start Adventure");

    //ScoreBoard
    scoreBoard = new ScoreBoard(highScoreManager, sharedSize.getWidth());
    // scoreBoardText
    toggleText = new Text("Afficher les meilleurs scores");
    toggleText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;");
    toggleText.setOnMouseClicked(this::toggleScoreBoard);
    toggleText.setOnMouseEntered(e -> toggleText.setStyle("-fx-fill: #aca29c; -fx-font-size: 14px; -fx-cursor: hand;"));
    toggleText.setOnMouseExited(e -> toggleText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;"));

    mainContent.getChildren()
        .addAll(titleLabel, adventurerNameField, errorLabel, mapSizeLabel, mapSizeBox, difficultyModeLabel, difficultyModeBox, startButton,
            toggleText);
    root.getChildren().addAll(mainContent, scoreBoard);
    scoreBoard.updateSize(sharedSize.getWidth(), sharedSize.getHeight());
    StackPane.setAlignment(scoreBoard, Pos.CENTER_RIGHT);
    // Hide the ScoreBoard by clicking outside it
    root.setOnMouseClicked(e -> {
      if (scoreBoard.isShowing() && !scoreBoard.getBoundsInParent().contains(e.getX(), e.getY())) {
        scoreBoard.toggleDisplay();
        toggleText.setText(scoreBoard.isShowing() ? "Masquer les meilleurs scores" : "Afficher les meilleurs scores");
      }

    });

    // Listener for Adventurer's cause validation
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

  @Override
  protected void onSizeChanged(double width, double height) {

    //If PlayerSetupScene is within a TransitionScene, then use sharedSize from TransitionScene
    Parent root = getRoot();
    Scene scene = root.getScene();
    double newWidth = width;
    double newHeight = height;
    if (scene instanceof TransitionScene<?> transitionScene) {
      newWidth = transitionScene.getWidth();
      newHeight = transitionScene.getHeight();
      // These next two lines give the PlayerSetupScene the correct size and not the one it had when it was created,
      // it solves the problem of creating the mainGameScene with initial size
      this.sharedSize.setHeight(newHeight);
      this.sharedSize.setWidth(newWidth);
    }

    scoreBoard.updateSize(newWidth, newHeight);
    scoreBoard.updateStyles(newWidth);
    scoreBoard.layout();
    scoreBoard.requestLayout();
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

  private DifficultyLevel getSelectedDifficultyLevel() {
    RadioButton selected = (RadioButton) difficultyToggleGroup.getSelectedToggle();
    return switch (selected.getText()) {
      case "Easy" -> DifficultyLevel.EASY;
      case "Hard" -> DifficultyLevel.HARD;
      default -> DifficultyLevel.NORMAL;
    };
  }

  public void setOnStartGame(TriConsumer<String, MapSize, DifficultyLevel> action) {
    // ajouter le paramètre difficultyLevel dans cette méthode et l'utiliser
    startButton.setOnAction(e -> {
      playerName = adventurerNameField.getText().trim();
      playerName = playerName.isEmpty() ? "Michel" : playerName;
      if (isValidAdventurerName(playerName)) {
        selectedMapSize = getSelectedMapSize();
        selectedDifficultyLevel = getSelectedDifficultyLevel();
        action.accept(playerName, selectedMapSize, selectedDifficultyLevel);
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

  private void toggleScoreBoard(MouseEvent event) {
    scoreBoard.setVisible(true);
    scoreBoard.toggleDisplay();
    if (scoreBoard.isShowing()) {
      toggleText.setText("Masquer les meilleurs scores");
    } else {
      toggleText.setText("Afficher les meilleurs scores");
    }
    event.consume();
  }

}
