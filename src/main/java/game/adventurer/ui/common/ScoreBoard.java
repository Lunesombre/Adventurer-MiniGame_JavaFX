package game.adventurer.ui.common;

import game.adventurer.config.AppConfig;
import game.adventurer.model.Score;
import game.adventurer.service.HighScoreManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScoreBoard extends VBox {


  @Getter
  private boolean isShowing = false;
  private final TranslateTransition slideIn;
  private final TranslateTransition slideOut;
  private final VBox otherHighScores;
  public static final String SCORE_LINE_NAME = "score-line-name";
  public static final String SCORE_LINE_NAME_SMALL = "score-line-name-small";
  public static final String SCORE_LINE_ELEMENT = "score-line-element";
  public static final String SCORE_LINE_ELEMENT_SMALL = "score-line-element-small";
  public static final String PODIUM_ITEM = "podium-item";

  public ScoreBoard(HighScoreManager highScoreManager, double initialWidth) {
    this.getStylesheets().add(Objects.requireNonNull(getClass().getResource(AppConfig.getInstance().getGlobalStylePath())).toExternalForm());

    List<Score> highScores = highScoreManager.getTopScores(10);
    // ScoreBoard title
    Label highScoreLabel = new Label("HighScores");
    highScoreLabel.getStyleClass().add("scoreBoardTitle");
    HBox title = new HBox(highScoreLabel);
    title.setAlignment(Pos.CENTER);

    // ScoreBoard podium
    HBox podium = new HBox(20);
    podium.setAlignment(Pos.CENTER);
    podium.getStyleClass().add("podium");
    //1st
    VBox first = new VBox(10);
    StackPane numberOne = drawNumberedCircle(30.0, 1, Color.web("gold"), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label topName = new Label(highScores.getFirst().getAdventurerName());
    topName.setStyle("-fx-font-weight: bold; -fx-text-fill: #40bd01");
    Text topScore = new Text(highScores.getFirst().getScoreValue() + " points");
    topScore.getStyleClass().add("firstScore");
    topScore.setEffect(new Bloom(0.6));
    Label topDate = new Label(getFormattedStringFromLocalDateTime(highScores.getFirst().getDate()));
    Label topDifficulty = new Label("mode " + highScores.getFirst().getDifficultyLevel().toString());

    first.getChildren().addAll(numberOne, topName, topScore, topDate, topDifficulty);
    //2nd
    VBox second = new VBox(10);
    second.setPadding(new Insets(40, 0, 0, 0));
    StackPane numberTwo = drawNumberedCircle(30.0, 2, Color.web("silver"), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label secondName = new Label(highScores.get(1).getAdventurerName());
    secondName.setStyle("-fx-font-weight: bold; ; -fx-text-fill: #498e27");
    Text secondScore = new Text(highScores.get(1).getScoreValue() + " points");
    secondScore.getStyleClass().add("second");
    secondScore.setEffect(new Bloom(0.5));
    Label secondDate = new Label(getFormattedStringFromLocalDateTime(highScores.get(1).getDate()));
    Label secondDifficulty = new Label("mode " + highScores.get(1).getDifficultyLevel().toString());

    second.getChildren().addAll(numberTwo, secondName, secondScore, secondDate, secondDifficulty);
    //3rd
    VBox third = new VBox(10);
    third.setPadding(new Insets(40, 0, 0, 0));
    StackPane numberThree = drawNumberedCircle(30.0, 3, Color.rgb(178, 109, 85), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label thirdName = new Label(highScores.get(2).getAdventurerName());
    thirdName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b650d");
    Text thirdScore = new Text(highScores.get(2).getScoreValue() + " points");
    thirdScore.getStyleClass().add("third");
    thirdScore.setEffect(new Bloom(0.3));
    Label thirdDate = new Label(getFormattedStringFromLocalDateTime(highScores.get(2).getDate()));
    Label thirdDifficulty = new Label("mode " + highScores.get(2).getDifficultyLevel().toString());

    third.getChildren().addAll(numberThree, thirdName, thirdScore, thirdDate, thirdDifficulty);

    first.getStyleClass().addAll(PODIUM_ITEM, "first");
    first.setAlignment(Pos.CENTER);
    second.getStyleClass().add(PODIUM_ITEM);
    second.setAlignment(Pos.CENTER);
    third.getStyleClass().add(PODIUM_ITEM);
    third.setAlignment(Pos.CENTER);

    podium.getChildren().addAll(second, first, third);

    // ScoreBoard 4th to 10th place
    StackPane centeringPane = new StackPane();
    centeringPane.getStyleClass().add("centering-pane");
    otherHighScores = new VBox(20);
    otherHighScores.getStyleClass().add("other-scores");
    otherHighScores.setAlignment(Pos.CENTER);
    for (int i = 3; i < highScores.size(); i++) {
      HBox scoreBox = new HBox(20);
      scoreBox.setAlignment(Pos.CENTER_LEFT);
      scoreBox.getStyleClass().add("score-line");
      Label position = new Label(String.valueOf(i + 1));
      position.getStyleClass().add("score-rank");
      Label name = new Label(highScores.get(i).getAdventurerName());
      name.getStyleClass().add(SCORE_LINE_NAME);

      Text scoreValueText = new Text(String.valueOf(highScores.get(i).getScoreValue()));
      scoreValueText.getStyleClass().add("score-value");
      Text pointsText = new Text(" points");
      HBox scoreValue = new HBox(scoreValueText, pointsText); // using a HBox to vertically align the Texts, as TextFlow is a pain to center
      scoreValue.setAlignment(Pos.BASELINE_CENTER);
      Label scoreLabel = new Label();
      scoreLabel.setGraphic(scoreValue);
      scoreLabel.getStyleClass().add(SCORE_LINE_ELEMENT);

      Label date = new Label(getFormattedStringFromLocalDateTime(highScores.get(i).getDate()));
      date.getStyleClass().add(SCORE_LINE_ELEMENT);

      Label difficulty = new Label("mode " + highScores.get(2).getDifficultyLevel().toString());
      difficulty.getStyleClass().add(SCORE_LINE_ELEMENT);

      scoreBox.getChildren().addAll(position, name, scoreLabel, date, difficulty);
      otherHighScores.getChildren().add(scoreBox);
    }
    centeringPane.getChildren().add(otherHighScores);
    this.getChildren().addAll(title, podium, centeringPane);

    this.setStyle(
        "-fx-background-color: rgba(250,200,80,0.9); -fx-padding:20; -fx-text-fill: #404040; -fx-border-color: gold; -fx-border-width: 3px; -fx-border-radius: 15px; -fx-background-radius: 15px");

    // Initializing animations
    slideIn = new TranslateTransition(Duration.seconds(0.8), this);
    slideOut = new TranslateTransition(Duration.seconds(0.8), this);

    // Hides it outside the screen
    setTranslateX(initialWidth);
    updateStyles(initialWidth);
  }

  public void toggleDisplay() {
    if (isShowing()) {
      slideOut();
    } else {
      slideIn();
    }
  }

  private void slideIn() {
    // Adjust the ScoreBoard dimensions
    double parentWidth = getParent().getLayoutBounds().getWidth();
    double parentHeight = getParent().getLayoutBounds().getHeight();
    updateSize(parentWidth, parentHeight);
    updateStyles(parentWidth);
    double targetX = (parentWidth - getWidth()) / 2;
    slideIn.setToX(0 - targetX);
    slideIn.play();
    isShowing = true;
  }

  private void slideOut() {
    double parentWidth = getParent().getLayoutBounds().getWidth();
    double targetX = parentWidth - getLayoutX();
    slideOut.setToX(targetX);
    slideOut.play();
    isShowing = false;
  }

  public void updateSize(double width, double height) {

    // New size calculation
    double newWidth = Math.min(width * 0.7, 1000);
    double newHeight = Math.min(height, 800);
    setMaxWidth(newWidth);
    setMaxHeight(newHeight);
    setPrefWidth(newWidth);
    setPrefHeight(newHeight);
    // Centers horizontally
    double layoutX = (width - newWidth) / 2;

    if (isShowing) {
      setTranslateX(0 - layoutX); // correctly keeps it horizontally centered
    }
    if (height < 820) {
      otherHighScores.setSpacing(12);
      if (height < 750) {
        otherHighScores.setSpacing(5);
      }
    } else {
      otherHighScores.setSpacing(20);
    }
  }

  private String getFormattedStringFromLocalDateTime(LocalDateTime date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    return date.format(formatter);
  }

  public StackPane drawNumberedCircle(double radius, int number, Color fillColor, Color strokeColor, Color numberColor) {
    Circle circle = new Circle(radius, fillColor);
    circle.setStroke(strokeColor);
    circle.setStrokeWidth(2.0);
    Text text = new Text(String.valueOf(number));
    text.setFont(Font.font(radius * 1.6));
    text.setStyle("-fx-font-weight: bold");
    text.setFill(numberColor);
    //Adjusting vertical position
    text.setTranslateY(-radius * 0.1);

    StackPane medal = new StackPane();
    medal.getChildren().addAll(circle, text);
    return medal;
  }

  public void updateStyles(double width) {
    boolean isWide = width >= 1200;
    String nameClass = (isWide) ? SCORE_LINE_NAME : SCORE_LINE_NAME_SMALL;
    String nameClass2 = (isWide) ? SCORE_LINE_ELEMENT : SCORE_LINE_ELEMENT_SMALL;

    for (Node node : otherHighScores.getChildren()) {
      if (node instanceof HBox scoreBox) {
        for (Node child : scoreBox.getChildren()) {
          if (child instanceof Label label) {
            if (label.getStyleClass().contains(SCORE_LINE_NAME) || label.getStyleClass().contains(SCORE_LINE_NAME_SMALL)) {
              label.getStyleClass().removeAll(SCORE_LINE_NAME, SCORE_LINE_NAME_SMALL);
              label.getStyleClass().add(nameClass);
            } else if (label.getStyleClass().contains(SCORE_LINE_ELEMENT) || label.getStyleClass()
                .contains(SCORE_LINE_ELEMENT_SMALL)) {
              label.getStyleClass().removeAll(SCORE_LINE_ELEMENT, SCORE_LINE_ELEMENT_SMALL);
              label.getStyleClass().add(nameClass2);
            }
          }
        }
      }
    }
  }

}
