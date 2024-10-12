package game.adventurer.ui.common.option;

import game.adventurer.config.AppConfig;
import game.adventurer.service.HighScoreManager;
import game.adventurer.ui.common.ScoreBoard;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class ScoreBoardOption implements Option {

  @Getter
  private final String name;
  private final VBox content;
  private final ScoreBoard scoreBoard;
  private final TitledPane titledPane;
  private final HighScoreManager highScoreManager;
  @Getter
  @Setter
  private Label showScoresLabel;

  public ScoreBoardOption(String name, ScoreBoard scoreBoard, HighScoreManager highScoreManager) {
    this.name = name;
    this.scoreBoard = scoreBoard;
    this.titledPane = new TitledPane();
    this.content = new VBox(10);
    this.highScoreManager = highScoreManager;
    setupLayout();
  }

  private void setupLayout() {
    titledPane.setText(name);
    titledPane.getStyleClass().add("option-titled-pane");
    showScoresLabel = new Label("Afficher les high scores");
    showScoresLabel.setStyle("-fx-cursor: hand;");
    showScoresLabel.setOnMouseClicked(e -> {
      scoreBoard.toggleDisplay();
      if (scoreBoard.isShowing()) {
        showScoresLabel.setText("Masquer les scores");
      } else {
        showScoresLabel.setText("Afficher les high scores");
      }
    });

    Label resetScoresLabel = new Label("Réinitialiser les high scores");
    resetScoresLabel.setStyle("-fx-cursor: hand;");
    resetScoresLabel.setOnMouseClicked(e -> showResetConfirmation());

    content.getChildren().addAll(showScoresLabel, resetScoresLabel);
    titledPane.setContent(content);
    titledPane.setExpanded(false);

  }

  private void showResetConfirmation() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Réinitialiser les High Scores");
    alert.setHeaderText("Êtes-vous sûr de vouloir réinitialiser les high scores ?");
    alert.setContentText("Cette action est irréversible !");

    // Stylize the alert
    String cssPath = AppConfig.getInstance().getGlobalStylePath();
    alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
    alert.getDialogPane().getStyleClass().add("reset-high-scores-alert");

    Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/warning.png")));
    ImageView iconView = new ImageView(icon);
    iconView.setPreserveRatio(true);
    iconView.setFitWidth(80);
    alert.getDialogPane().setGraphic(iconView);
    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    stage.getIcons().add(icon);

    // Buttons
    alert.getButtonTypes().forEach(buttonType -> {
      Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
      if (buttonType == ButtonType.OK) {
        button.setText("Valider");
        button.getStyleClass().add("reset-confirm-button");
      } else if (buttonType == ButtonType.CANCEL) {
        button.setText("Annuler");
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
  public void onValueChange(Consumer<Object> listener) {
    // Nothing to do here
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
}
