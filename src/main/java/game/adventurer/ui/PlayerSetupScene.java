package game.adventurer.ui;

import game.adventurer.common.Localizable;
import game.adventurer.common.SharedSize;
import game.adventurer.model.enums.DifficultyLevel;
import game.adventurer.model.enums.MapSize;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.ui.common.OptionsPanel;
import game.adventurer.ui.common.ScoreBoard;
import game.adventurer.ui.common.TransitionScene;
import game.adventurer.ui.common.option.LanguageOption;
import game.adventurer.util.TriConsumer;
import java.util.Arrays;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
public class PlayerSetupScene extends BaseScene implements Localizable {

  public static final String SETUP_DIFFICULTY_HARD = "setup.difficulty.hard";
  public static final String SETUP_DIFFICULTY_EASY = "setup.difficulty.easy";
  public static final String SETUP_MAP_SIZE_SMALL = "setup.mapSize.small";
  public static final String SETUP_MAP_SIZE_LARGE = "setup.mapSize.large";
  private final LocalizedMessageService localizedMessageService = LocalizedMessageService.getInstance();
  private String displayHighScores;
  private String hideHighScores;
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
  private VBox mainContent;
  private OptionsPanel options;

  private final LocalizationService localizationService;
  // localizable texts
  private Label titleLabel;
  private TextField adventurerNameField;
  private Label mapSizeLabel;
  private RadioButton smallMap;
  private RadioButton mediumMap;
  private RadioButton largeMap;
  private Tooltip smallMapTooltip;
  private Tooltip mediumMapTooltip;
  private Tooltip largeMapTooltip;
  private Label difficultyModeLabel;
  private RadioButton easyMode;
  private RadioButton mediumMode;
  private RadioButton hardMode;
  private Tooltip easyModeTooltip;
  private Tooltip normalModeTooltip;
  private Tooltip hardModeTooltip;

  public PlayerSetupScene(SharedSize sharedSize, HighScoreManager highScoreManager,
      LocalizationService localizationService) {
    super(new StackPane(), sharedSize);
    this.highScoreManager = highScoreManager;
    this.localizationService = localizationService;
    this.displayHighScores = localizedMessageService.getMessage("highScores.show");
    this.hideHighScores = localizedMessageService.getMessage("highScores.hide");
    initialize();
  }

  @Override
  protected void initialize() {
    Label errorLabel;
    StackPane root = (StackPane) getRoot();
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: #403f3f;");
    mainContent = new VBox(20);
    mainContent.setAlignment(Pos.CENTER);

    titleLabel = new Label(localizedMessageService.getMessage("setup.title"));
    titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #46a7b3;");
    titleLabel.getStyleClass().add("bold-text");

    adventurerNameField = new TextField();
    adventurerNameField.setPromptText(localizedMessageService.getMessage("setup.adventurer.prompt"));
    adventurerNameField.setStyle(
        "-fx-font-size: 24px; -fx-text-fill: #7a8181; -fx-prompt-text-fill: lightgray; -fx-max-width: 400; -fx-background-radius: 15px;");

    errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: red;");
    errorLabel.setVisible(false); // Default : hidden

    mapSizeLabel = new Label(localizedMessageService.getMessage("setup.mapSize.title"));
    mapSizeLabel.setStyle(" -fx-font-size: 24px; -fx-text-fill: #747777; -fx-font-weight: bold");
    mapSizeGroup = new ToggleGroup();
    smallMap = new RadioButton(localizedMessageService.getMessage(SETUP_MAP_SIZE_SMALL));
    String radioStyles = "-fx-font-size: 18px; -fx-text-fill: #747777";
    smallMap.setStyle(radioStyles);
    mediumMap = new RadioButton(localizedMessageService.getMessage("setup.mapSize.medium"));
    mediumMap.setStyle(radioStyles);
    largeMap = new RadioButton(localizedMessageService.getMessage(SETUP_MAP_SIZE_LARGE));
    largeMap.setStyle(radioStyles);
    smallMap.setToggleGroup(mapSizeGroup);
    mediumMap.setToggleGroup(mapSizeGroup);
    largeMap.setToggleGroup(mapSizeGroup);
    mediumMap.setSelected(true);
    //Tooltip for map sizes
    smallMapTooltip = new Tooltip(localizedMessageService.getMessage("setup.mapSize.small.tooltip"));
    mediumMapTooltip = new Tooltip(localizedMessageService.getMessage("setup.mapSize.medium.tooltip"));
    largeMapTooltip = new Tooltip(localizedMessageService.getMessage("setup.mapSize.large.tooltip"));
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
    difficultyModeLabel = new Label(localizedMessageService.getMessage("setup.difficulty.title"));
    difficultyModeLabel.setStyle(" -fx-font-size: 24px; -fx-text-fill: #747777; -fx-font-weight: bold");
    difficultyToggleGroup = new ToggleGroup();
    easyMode = new RadioButton(localizedMessageService.getMessage(SETUP_DIFFICULTY_EASY));
    easyMode.setStyle(radioStyles);
    mediumMode = new RadioButton(localizedMessageService.getMessage("setup.difficulty.normal"));
    mediumMode.setStyle(radioStyles);
    hardMode = new RadioButton(localizedMessageService.getMessage(SETUP_DIFFICULTY_HARD));
    hardMode.setStyle(radioStyles);
    easyModeTooltip = new Tooltip(localizedMessageService.getMessage("setup.difficulty.easy.tooltip"));
    normalModeTooltip = new Tooltip(localizedMessageService.getMessage("setup.difficulty.normal.tooltip"));
    hardModeTooltip = new Tooltip(localizedMessageService.getMessage("setup.difficulty.hard.tooltip"));
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

    startButton = new Button(localizedMessageService.getMessage("setup.start.button"));

    //ScoreBoard
    scoreBoard = new ScoreBoard(highScoreManager, sharedSize.getWidth());
    // scoreBoardText
    toggleText = new Text(displayHighScores);
    toggleText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;");
    toggleText.setOnMouseClicked(this::toggleScoreBoard);
    toggleText.setOnMouseEntered(e -> toggleText.setStyle("-fx-fill: #aca29c; -fx-font-size: 14px; -fx-cursor: hand;"));
    toggleText.setOnMouseExited(e -> toggleText.setStyle("-fx-fill: #958275; -fx-font-size: 14px;"));

    // Options
    LanguageOption languageOption = new LanguageOption(localizedMessageService, localizationService, 24);
    options = new OptionsPanel(sharedSize.getWidth(), sharedSize.getHeight(), languageOption);

    mainContent.getChildren()
        .addAll(titleLabel, adventurerNameField, errorLabel, mapSizeLabel, mapSizeBox, difficultyModeLabel, difficultyModeBox, startButton,
            toggleText);
    root.getChildren().addAll(mainContent, scoreBoard, options);
    scoreBoard.updateSize(sharedSize.getWidth(), sharedSize.getHeight());
    StackPane.setAlignment(scoreBoard, Pos.CENTER_RIGHT);
    // Hide the ScoreBoard by clicking outside it
    root.setOnMouseClicked(e -> {
      if (scoreBoard.isShowing() && !scoreBoard.getBoundsInParent().contains(e.getX(), e.getY())) {
        scoreBoard.toggleDisplay();
        toggleText.setText(scoreBoard.isShowing() ? hideHighScores : displayHighScores);
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

    // Handle focus
    options.addToggleListener(isShowing -> {
      if (Boolean.TRUE.equals(isShowing)) {
        options.requestFocus();
      } else {
        restoreDefaultFocus();
      }
    });

    // Register this class as Localizable - do it after the localizable texts are set to avoid Null Pointer Exception.
    localizationService.registerLocalizable(this);
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
    options.getChildren().getFirst().setTranslateX(newWidth); // keeps the options "optionsContent" hidden on resize
  }


  private MapSize getSelectedMapSize() {
    RadioButton selectedButton = (RadioButton) mapSizeGroup.getSelectedToggle();
    if (selectedButton.getText().contains(localizedMessageService.getMessage(SETUP_MAP_SIZE_SMALL))) {
      return MapSize.SMALL;
    } else if (selectedButton.getText().contains(localizedMessageService.getMessage((SETUP_MAP_SIZE_LARGE)))) {
      return MapSize.LARGE;
    } else {
      return MapSize.MEDIUM;
    }
  }

  private DifficultyLevel getSelectedDifficultyLevel() {
    RadioButton selected = (RadioButton) difficultyToggleGroup.getSelectedToggle();
    if (selected.getText().equals(localizedMessageService.getMessage(SETUP_DIFFICULTY_EASY))) {
      return DifficultyLevel.EASY;
    } else if (selected.getText().equals(localizedMessageService.getMessage(SETUP_DIFFICULTY_HARD))) {
      return DifficultyLevel.HARD;
    } else {
      return DifficultyLevel.NORMAL;
    }
  }

  public void setOnStartGame(TriConsumer<String, MapSize, DifficultyLevel> action) {
    startButton.setOnAction(e -> {
      playerName = adventurerNameField.getText().trim();
      playerName = playerName.isEmpty() ? "Michel" : playerName;
      if (isValidAdventurerName(playerName)) {
        selectedMapSize = getSelectedMapSize();
        selectedDifficultyLevel = getSelectedDifficultyLevel();
        action.accept(playerName, selectedMapSize, selectedDifficultyLevel);
      } else {
        showAlert(localizedMessageService.getMessage("setup.error.validation.alert"));
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
    alert.setTitle(localizedMessageService.getMessage("setup.error.validation.alert.title"));
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private String getValidationMessage(String name) {
    if (name.isEmpty()) {
      return localizedMessageService.getMessage("setup.validation.notEmpty");
    }
    if (name.length() > 20) {
      return localizedMessageService.getMessage("setup.validation.maxLengthExceeded");
    }
    if (!name.matches("^[a-zA-Z0-9 -]+$")) {
      return localizedMessageService.getMessage("setup.validation.regex");
    }
    return ""; // No errors
  }

  private void toggleScoreBoard(MouseEvent event) {
    scoreBoard.setVisible(true);
    scoreBoard.toggleDisplay();
    if (scoreBoard.isShowing()) {
      toggleText.setText(hideHighScores);
    } else {
      toggleText.setText(displayHighScores);
    }
    event.consume();
  }

  private void restoreDefaultFocus() {
    if (adventurerNameField.getText().isEmpty()) {
      adventurerNameField.requestFocus();
    } else if (!startButton.isDisabled()) {
      startButton.requestFocus();
    } else {
      // Loop though focusable elements in logical order
      for (Node node : mainContent.getChildren()) {
        if (node.isFocusTraversable() && !node.isDisabled()) {
          node.requestFocus();
          break;
        }
      }
    }
  }


  @Override
  public void updateLanguage(Locale newLocale) {
    scoreBoard.updateLanguage(newLocale);
    updateAllTexts();
  }

  private void updateAllTexts() {
    titleLabel.setText(localizedMessageService.getMessage("setup.title"));
    adventurerNameField.setPromptText(localizedMessageService.getMessage("setup.adventurer.prompt"));
    startButton.setText(localizedMessageService.getMessage("setup.start.button"));
    mapSizeLabel.setText((localizedMessageService.getMessage("setup.mapSize.title")));
    smallMap.setText((localizedMessageService.getMessage(SETUP_MAP_SIZE_SMALL)));
    mediumMap.setText((localizedMessageService.getMessage("setup.mapSize.medium")));
    largeMap.setText((localizedMessageService.getMessage(SETUP_MAP_SIZE_LARGE)));
    smallMapTooltip.setText(localizedMessageService.getMessage("setup.mapSize.small.tooltip"));
    mediumMapTooltip.setText(localizedMessageService.getMessage("setup.mapSize.medium.tooltip"));
    largeMapTooltip.setText(localizedMessageService.getMessage("setup.mapSize.large.tooltip"));
    difficultyModeLabel.setText(localizedMessageService.getMessage("setup.difficulty.title"));
    easyMode.setText(localizedMessageService.getMessage(SETUP_DIFFICULTY_EASY));
    mediumMode.setText(localizedMessageService.getMessage("setup.difficulty.normal"));
    hardMode.setText(localizedMessageService.getMessage(SETUP_DIFFICULTY_HARD));
    easyModeTooltip.setText(localizedMessageService.getMessage("setup.difficulty.easy.tooltip"));
    normalModeTooltip.setText(localizedMessageService.getMessage("setup.difficulty.normal.tooltip"));
    hardModeTooltip.setText(localizedMessageService.getMessage("setup.difficulty.hard.tooltip"));
    displayHighScores = localizedMessageService.getMessage("highScores.show");
    hideHighScores = localizedMessageService.getMessage("highScores.hide");
    toggleText.setText(scoreBoard.isShowing() ? hideHighScores : displayHighScores);
  }
}
