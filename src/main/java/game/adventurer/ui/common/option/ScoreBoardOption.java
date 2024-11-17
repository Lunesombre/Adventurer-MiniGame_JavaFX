package game.adventurer.ui.common.option;

import static game.adventurer.util.MiscUtil.alertInitializer;

import game.adventurer.common.Localizable;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.ui.common.ScoreBoard;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

public class ScoreBoardOption implements Option<Void>, Localizable {

  public static final String HIGH_SCORES_RESET_KEY = "option.highScores.reset";
  public static final String HIGH_SCORES_SHOW_KEY = "highScores.show";
  public static final String HIGH_SCORES_HIDE_KEY = "highScores.hide";
  @Getter
  private final String nameKey;
  private final VBox content;
  private final ScoreBoard scoreBoard;
  private final TitledPane titledPane;
  private final HighScoreManager highScoreManager;
  private final LocalizedMessageService messageService = LocalizedMessageService.getInstance();
  @Getter
  @Setter
  private Label showScoresLabel;
  private Label resetScoresLabel;

  public ScoreBoardOption(String nameKey, ScoreBoard scoreBoard, HighScoreManager highScoreManager) {
    this.nameKey = nameKey;
    this.titledPane = new TitledPane();
    this.content = new VBox(10);
    this.scoreBoard = scoreBoard;
    this.highScoreManager = highScoreManager;
    setupLayout();
  }

  private void setupLayout() {
    titledPane.setText(nameKey);
    titledPane.getStyleClass().add("option-titled-pane");
    showScoresLabel = new Label(messageService.getMessage(HIGH_SCORES_SHOW_KEY));
    showScoresLabel.setStyle("-fx-cursor: hand;");
    showScoresLabel.setOnMouseClicked(e -> {
      scoreBoard.toggleDisplay();
      if (scoreBoard.isShowing()) {
        showScoresLabel.setText(messageService.getMessage(HIGH_SCORES_HIDE_KEY));
      } else {
        showScoresLabel.setText(messageService.getMessage(HIGH_SCORES_SHOW_KEY));
      }
    });

    resetScoresLabel = new Label(messageService.getMessage(HIGH_SCORES_RESET_KEY));
    resetScoresLabel.setStyle("-fx-cursor: hand;");
    resetScoresLabel.setOnMouseClicked(e -> showResetConfirmation());

    content.getChildren().addAll(showScoresLabel, resetScoresLabel);
    titledPane.setContent(content);
    titledPane.setExpanded(false);

  }

  private void showResetConfirmation() {
    // Initialize the alert
    Alert alert = alertInitializer(getClass(), AlertType.CONFIRMATION, messageService.getMessage(HIGH_SCORES_RESET_KEY),
        messageService.getMessage("option.highScores.reset.confirm.header"), "reset-high-scores-alert", "/assets/icons/warning.png", true);

    // Defines alert content (buttons are created by the AlertType.CONFIRMATION)
    alert.setContentText(messageService.getMessage("option.highScores.reset.confirm.body"));

    // Buttons text and styles
    alert.getButtonTypes().forEach(buttonType -> {
      Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
      if (buttonType == ButtonType.OK) {
        button.setText(messageService.getMessage("button.validate"));
        button.getStyleClass().add("reset-confirm-button");
      } else if (buttonType == ButtonType.CANCEL) {
        button.setText(messageService.getMessage("button.cancel"));
        button.getStyleClass().add("reset-cancel-button");
      }
    });

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      highScoreManager.resetHighScores();
      scoreBoard.updateScores(highScoreManager.getHighScores());
    }
  }


  @Override
  public Node getNode() {
    return titledPane;
  }

  @Override
  public void onValueChange(Consumer<Void> listener) {
    // Nothing to do here as this option has no value to change
  }

  public void adjustContentLabelsFontSize(double width) {
    double baseFontSize = 16;
    double scaleFactor = Math.min(width / 1240, 1); // doesn't do anything unless width is smaller than 1240

    String fontStyle = String.format("-fx-font-size: %.1fpx;", baseFontSize * scaleFactor);

    for (Node node : content.getChildren()) {
      if (node instanceof Label label) {
        label.setStyle(label.getStyle() + fontStyle);
      }
    }
  }

  @Override
  public void updateLanguage(Locale newLocale) {
    resetScoresLabel.setText(messageService.getMessage(HIGH_SCORES_RESET_KEY));
    scoreBoard.updateLanguage(newLocale);
    if (scoreBoard.isShowing()) {
      showScoresLabel.setText(messageService.getMessage(HIGH_SCORES_HIDE_KEY));
    } else {
      showScoresLabel.setText(messageService.getMessage(HIGH_SCORES_SHOW_KEY));
    }
  }
}
