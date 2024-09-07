package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.config.AppConfig;
import game.adventurer.model.GameMap;
import game.adventurer.model.base.Wound;
import game.adventurer.ui.common.BaseScene;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameOverScene extends BaseScene {

  private final GameMap gameMap; // retrieve the Adventurer and the Wounds list.
  public static final String GAME_OVER_TITLE = "Vous êtes mort, ";
  public static final String SKULL_PATH = AppConfig.getInstance().getImagesPath() + "game_over_skull.png";
  private final Button replayButton = new Button("Rejouer");
  private final Button quitButton = new Button("Quitter");

  public GameOverScene(GameMap gameMap, SharedSize sharedSize) {
    super(new VBox(20), sharedSize);
    this.gameMap = gameMap;

    initialize();
  }

  @Override
  protected void initialize() {
    VBox root = (VBox) getRoot();
    root.setAlignment(Pos.CENTER);

    //Scene Title
    Label titleLabel = new Label(GAME_OVER_TITLE + gameMap.getAdventurer().getName());
    titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");

    // HBox for skull image and the wounds' list.
    HBox contentBox = new HBox(20);
    contentBox.setAlignment(Pos.CENTER);
    // Skull Image
    Image skullImage = new Image(SKULL_PATH);
    ImageView skullImageView = new ImageView(skullImage);
    skullImageView.setFitHeight(450.0);
    skullImageView.setFitWidth(450.0);
    skullImageView.setPreserveRatio(true);
    // Creates a clip to round the image's corners
    Rectangle clip = new Rectangle(skullImageView.getFitWidth(), skullImageView.getFitHeight());
    clip.setArcWidth(20);
    clip.setArcHeight(20);
    skullImageView.setClip(clip);

    // Inner shadow on skull image
    InnerShadow innerShadow = new InnerShadow();
    innerShadow.setRadius(15.0);
    innerShadow.setOffsetX(5);
    innerShadow.setOffsetY(5);
    innerShadow.setColor(Color.rgb(0, 0, 0, 0.5));
    skullImageView.setEffect(innerShadow);

    // Wounds' list
    List<Wound> wounds = gameMap.getWoundsList();

    ListView<Wound> woundsListView = new ListView<>();
    int lastWoundIndex = wounds.size() - 1;
    woundsListView.setCellFactory(lv -> new WoundListCell(lv, lastWoundIndex));
    // ListView configuration
    woundsListView.setFixedCellSize(-1); // Adapting vertical cell-size
    woundsListView.setPrefHeight(200);
    woundsListView.setMaxHeight(Double.MAX_VALUE);
    woundsListView.setPrefWidth(450);
    woundsListView.setMaxWidth(450);
    woundsListView.getItems().addAll(wounds);

    // Style the ListView to hide border and background
    woundsListView.setStyle("-fx-background-color: transparent;-fx-font-size: 16px; -fx-background-insets: 0; -fx-padding: 5;");

    contentBox.getChildren().addAll(skullImageView, woundsListView);

    // Buttons
    quitButton.getStyleClass().add("quit-button");
    HBox buttonBox = new HBox(20);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(replayButton, quitButton);

    // Add elements to main VBox
    root.getChildren().addAll(titleLabel, contentBox, buttonBox);

    // Background style
    root.setStyle("-fx-background-color: #2C3E50;");

    // Adding listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());


  }

  public void setOnReplayGame(Runnable action) {
    replayButton.setOnAction(e -> action.run());
  }

  public void setOnQuitGame(Runnable action) {
    quitButton.setOnAction(e -> action.run());
  }

  // static inner class to get rid of the code smell detected by Sonar
  // (java:S1171 warning) that I had when I used a non-static initializer in my factory.
  private static class WoundListCell extends ListCell<Wound> {

    private final Label label;
    private final VBox container;
    private final int lastWoundIndex;

    public WoundListCell(ListView<Wound> lv, int lastWoundIndex) {
      this.lastWoundIndex = lastWoundIndex;
      label = new Label();
      container = new VBox(label);

      container.setPadding(new Insets(5, 10, 5, 10));
      container.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
      setBackground(Background.EMPTY);

      label.setWrapText(true);
      label.setMaxWidth(400);

      container.prefWidthProperty().bind(lv.widthProperty().subtract(100));
    }

    @Override
    protected void updateItem(Wound wound, boolean empty) {
      super.updateItem(wound, empty);
      if (empty || wound == null) {
        setGraphic(null);
      } else {
        label.setText(wound.getCause().getCauseName() + ": " + wound.getWoundMessage());

        // Styling the last wound cell, then styling each cell with a slightly different background color on even/uneven cells.
        if (getIndex() == lastWoundIndex) {
          container.setStyle("-fx-background-color: #ba5947; -fx-background-radius: 10; -fx-border-radius: 10;");
        } else if (getIndex() % 2 == 0) {
          container.setStyle("-fx-background-color: #bdbcbc; -fx-background-radius: 10; -fx-border-radius: 10;");
        } else {
          container.setStyle("-fx-background-color: #8f8f8f; -fx-background-radius: 10; -fx-border-radius: 10;");
        }

        setGraphic(container);
      }
    }
  }
}
