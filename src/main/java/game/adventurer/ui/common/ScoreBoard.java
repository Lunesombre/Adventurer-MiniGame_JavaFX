package game.adventurer.ui.common;

import static game.adventurer.AdventurerGameApp.highScoreManager;
import static game.adventurer.util.MiscUtil.applyGlobalCss;

import game.adventurer.model.Score;
import game.adventurer.service.HighScoreManager;
import game.adventurer.service.LocalizedMessageService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.BorderPane;
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

  public static final String MODE_LABEL = "scoreBoard.modeLabel";
  public static final String SCORE_LABEL = "scoreBoard.scoreLabel";
  public static final String POINTS_LABEL = "scoreBoard.points";
  public static final int MAX_NUMBER_OF_SCORES = 10;
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
  private final LocalizedMessageService messageService = LocalizedMessageService.getInstance();
  private final HBox podium = new HBox(20);


  public ScoreBoard(HighScoreManager highScoreManager, double initialWidth) {
    applyGlobalCss(this);

    List<Score> highScores = highScoreManager.getTopScores(MAX_NUMBER_OF_SCORES);
    // ScoreBoard title
    Label highScoreLabel = new Label(messageService.getMessage("scoreBoard.label"));
    highScoreLabel.getStyleClass().add("scoreboard-title");
    HBox title = new HBox(highScoreLabel);
    title.setAlignment(Pos.CENTER);

    // ScoreBoard podium
    podium.setAlignment(Pos.CENTER);
    podium.getStyleClass().add("podium");
    //1st

    StackPane numberOne = drawNumberedCircle(30.0, 1, Color.web("gold"), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label topName = new Label(highScores.getFirst().getAdventurerName());
    topName.setStyle("-fx-font-weight: bold; -fx-text-fill: #40bd01");
    Text topScore = new Text(messageService.getMessage(SCORE_LABEL, highScores.getFirst().getScoreValue()));
    topScore.getStyleClass().add("firstScore");
    topScore.setEffect(new Bloom(0.6));
    Label topDate = new Label(getFormattedStringFromLocalDateTime(highScores.getFirst().getDate()));
    Label topDifficulty = new Label(messageService.getMessage(MODE_LABEL, highScores.getFirst().getDifficultyLevel().toString()));

    VBox first = new VBox(10);
    first.getChildren().addAll(numberOne, topName, topScore, topDate, topDifficulty);
    //2nd
    VBox second = new VBox(10);
    second.setPadding(new Insets(40, 0, 0, 0));
    StackPane numberTwo = drawNumberedCircle(30.0, 2, Color.web("silver"), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label secondName = new Label(highScores.get(1).getAdventurerName());
    secondName.setStyle("-fx-font-weight: bold; ; -fx-text-fill: #498e27");
    Text secondScore = new Text(messageService.getMessage(SCORE_LABEL, highScores.get(1).getScoreValue()));
    secondScore.getStyleClass().add("second");
    secondScore.setEffect(new Bloom(0.5));
    Label secondDate = new Label(getFormattedStringFromLocalDateTime(highScores.get(1).getDate()));
    Label secondDifficulty = new Label(messageService.getMessage(MODE_LABEL, highScores.get(1).getDifficultyLevel().toString()));

    second.getChildren().addAll(numberTwo, secondName, secondScore, secondDate, secondDifficulty);
    //3rd
    VBox third = new VBox(10);
    third.setPadding(new Insets(40, 0, 0, 0));
    StackPane numberThree = drawNumberedCircle(30.0, 3, Color.rgb(178, 109, 85), Color.rgb(0, 0, 0), Color.rgb(0, 0, 0));
    Label thirdName = new Label(highScores.get(2).getAdventurerName());
    thirdName.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b650d");
    Text thirdScore = new Text(messageService.getMessage(SCORE_LABEL, highScores.get(2).getScoreValue()));
    thirdScore.getStyleClass().add("third");
    thirdScore.setEffect(new Bloom(0.3));
    Label thirdDate = new Label(getFormattedStringFromLocalDateTime(highScores.get(2).getDate()));
    Label thirdDifficulty = new Label(messageService.getMessage(MODE_LABEL, highScores.get(2).getDifficultyLevel().toString()));

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
      Text pointsText = new Text(messageService.getMessage(POINTS_LABEL));
      HBox scoreValue = new HBox(scoreValueText, pointsText); // using a HBox to vertically align the Texts, as TextFlow is a pain to center
      scoreValue.setAlignment(Pos.BASELINE_CENTER);
      Label scoreLabel = new Label();
      scoreLabel.setGraphic(scoreValue);
      scoreLabel.getStyleClass().add(SCORE_LINE_ELEMENT);

      Label date = new Label(getFormattedStringFromLocalDateTime(highScores.get(i).getDate()));
      date.getStyleClass().add(SCORE_LINE_ELEMENT);

      Label difficulty = new Label(messageService.getMessage(MODE_LABEL, highScores.get(i).getDifficultyLevel().toString()));
      difficulty.getStyleClass().add(SCORE_LINE_ELEMENT);

      scoreBox.getChildren().addAll(position, name, scoreLabel, date, difficulty);
      otherHighScores.getChildren().add(scoreBox);
    }
    centeringPane.getChildren().add(otherHighScores);
    this.getChildren().addAll(title, podium, centeringPane);

    getStyleClass().add("scoreboard");

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
    log.debug("parentWidth: {}", parentWidth);
    log.debug("this.getWidth: {}", getWidth());
    updateSize(parentWidth, parentHeight);
    updateStyles(parentWidth);
    log.debug("this.getWidth after updateSize: {}", getWidth());
    double targetX = (parentWidth - getWidth()) / 2;
    log.debug("targetX : {}", targetX);
    if (getParent() instanceof BorderPane borderPane) {
      slideIn.setToX(targetX);
      setTranslateY((borderPane.getHeight() - getHeight()) / 2);
    } else {
      slideIn.setToX(0 - targetX);
    }
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
    setMaxSize(newWidth, newHeight);
    setPrefSize(newWidth, newHeight);
    setMinSize(newWidth, newHeight);
    // Strange fix for when it's used from ScoreBoardOption in a BorderPane, I don't know why it works
    // and why it doesn't break things
    setWidth(newWidth);
    setHeight(newHeight);

    // Centers horizontally
    double layoutX = (width - newWidth) / 2;

    if (isShowing) {
      if (getParent() instanceof BorderPane borderPane) {
        setTranslateX(layoutX); // centers horizontally
        setTranslateY((borderPane.getHeight() - getHeight()) / 2); // centers vertically
      } else {
        setTranslateX(0 - layoutX); // correctly keeps it horizontally centered
      }
    }
    if (height < 820) {
      otherHighScores.setSpacing(14);
      if (height < 750) {
        otherHighScores.setSpacing(8);
      }
    } else {
      otherHighScores.setSpacing(20);
    }
  }

  private String getFormattedStringFromLocalDateTime(LocalDateTime date) {
    Locale currentLocale = messageService.getCurrentLocale();
    DateTimeFormatter formatter;
    switch (currentLocale.getLanguage()) {
      case "en" -> formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
      case "fr" -> formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
      default -> formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }
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

  public void updateScores(List<Score> newScoresList) {
    // Podium update
    updatePodiumScore(newScoresList.get(0), 0);
    updatePodiumScore(newScoresList.get(1), 1);
    updatePodiumScore(newScoresList.get(2), 2);

    // Other top 10 scores update
    for (int i = 3; i < Math.min(newScoresList.size(), MAX_NUMBER_OF_SCORES); i++) {
      updateOtherScore(newScoresList.get(i), i);
    }
  }

  private void updatePodiumScore(Score score, int position) {
    int place;
    switch (position) {
      case 0 -> place = 1;
      case 1 -> place = 0;
      default -> place = 2;
    }
    VBox podiumItem = (VBox) ((HBox) getChildren().get(1)).getChildren().get(place);

    Label nameLabel = (Label) podiumItem.getChildren().get(1);
    nameLabel.setText(score.getAdventurerName());

    Text scoreText = (Text) podiumItem.getChildren().get(2);
    scoreText.setText(messageService.getMessage(SCORE_LABEL, score.getScoreValue()));

    Label dateLabel = (Label) podiumItem.getChildren().get(3);
    dateLabel.setText(getFormattedStringFromLocalDateTime(score.getDate()));

    Label difficultyLabel = (Label) podiumItem.getChildren().get(4);
    difficultyLabel.setText(messageService.getMessage(MODE_LABEL, score.getDifficultyLevel().toString()));
  }

  private void updateOtherScore(Score score, int position) {
    HBox scoreBox = (HBox) otherHighScores.getChildren().get(position - 3);

    Label nameLabel = (Label) scoreBox.getChildren().get(1);
    nameLabel.setText(score.getAdventurerName());

    HBox scoreValueBox = (HBox) ((Label) scoreBox.getChildren().get(2)).getGraphic();
    Text scoreValueText = (Text) scoreValueBox.getChildren().getFirst();
    scoreValueText.setText(String.valueOf(score.getScoreValue()));

    Label dateLabel = (Label) scoreBox.getChildren().get(3);
    dateLabel.setText(getFormattedStringFromLocalDateTime(score.getDate()));

    Label difficultyLabel = (Label) scoreBox.getChildren().get(4);
    difficultyLabel.setText(messageService.getMessage(MODE_LABEL, score.getDifficultyLevel().toString()));
  }

  /**
   * Updates the language of all score elements in the ScoreBoard. This method is called when the application language changes.
   *
   * @param newLocale The new Locale to be used for language updates.
   */

  public void updateLanguage(Locale newLocale) {
    for (int i = 0; i < Math.min(highScoreManager.getHighScores().size(), MAX_NUMBER_OF_SCORES); i++) {
      updateScoreLanguage(highScoreManager.getHighScores().get(i), i);
    }
  }

  /**
   * Updates the language-specific elements of a single score entry. This method handles both podium scores (top 3) and other scores differently.
   *
   * @param score The Score object containing the score data to be updated.
   * @param index The index of the score in the highScores list, determining its position in the ScoreBoard.
   */
  private void updateScoreLanguage(Score score, int index) {
    if (index >= 0 && index < 3) {
      // Podium update
      int place;
      switch (index) {
        case 0 -> place = 1;
        case 1 -> place = 0;
        default -> place = 2;
      }
      VBox podiumScoreCard = (VBox) podium.getChildren().get(place);
      // Score text update
      Text pointsText = (Text) podiumScoreCard.getChildren().get(2);
      pointsText.setText(messageService.getMessage(POINTS_LABEL));

      // Difficulty text update
      Label difficultyLabel = (Label) podiumScoreCard.getChildren().get(4);
      difficultyLabel.setText(messageService.getMessage(MODE_LABEL, score.getDifficultyLevel().toString()));

      // Date format update
      Label dateLabel = (Label) podiumScoreCard.getChildren().get(3);
      dateLabel.setText(getFormattedStringFromLocalDateTime(score.getDate()));

    } else if (index < Math.min(highScoreManager.getHighScores().size(), MAX_NUMBER_OF_SCORES)) {
      // Updates the other scores
      // Score text update
      HBox scoreBox = (HBox) otherHighScores.getChildren().get(index - 3);
      HBox scoreValueBox = (HBox) ((Label) scoreBox.getChildren().get(2)).getGraphic();
      Text pointsText = (Text) scoreValueBox.getChildren().get(1);
      pointsText.setText(messageService.getMessage(POINTS_LABEL));

      // Difficulty text update
      Label difficultyLabel = (Label) scoreBox.getChildren().get(4);
      difficultyLabel.setText(messageService.getMessage(MODE_LABEL, score.getDifficultyLevel().toString()));

      // Date format update
      Label dateLabel = (Label) scoreBox.getChildren().get(3);
      dateLabel.setText(getFormattedStringFromLocalDateTime(score.getDate()));
    }
  }
}
