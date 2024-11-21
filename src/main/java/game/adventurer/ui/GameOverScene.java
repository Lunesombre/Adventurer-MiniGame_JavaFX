package game.adventurer.ui;

import game.adventurer.common.Localizable;
import game.adventurer.common.SharedSize;
import game.adventurer.config.AppConfig;
import game.adventurer.model.GameMap;
import game.adventurer.model.base.Wound;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.ui.common.BaseScene;
import game.adventurer.ui.common.CreditsOverlay;
import java.util.List;
import java.util.Locale;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class GameOverScene extends BaseScene implements Localizable {

  private final GameMap gameMap; // retrieve the Adventurer and the Wounds list.
  public static final String SKULL_PATH = AppConfig.getInstance().getImagesPath() + "game_over_skull.png";
  private static final LocalizedMessageService messageService = LocalizedMessageService.getInstance();
  private final Button replayButton = new Button(messageService.getMessage("button.replay"));
  private final Button quitButton = new Button(messageService.getMessage("button.exit"));
  private Label titleLabel;
  private final HostServices hostServices;
  private CreditsOverlay creditsOverlay;


  public GameOverScene(GameMap gameMap, SharedSize sharedSize, HostServices hostServices) {
    super(new StackPane(), sharedSize);
    this.gameMap = gameMap;
    this.hostServices = hostServices;

    initialize();
  }

  @Override
  protected void initialize() {
    Text credits;
    StackPane root = (StackPane) getRoot();
    root.setAlignment(Pos.CENTER);

    //Scene Title
    titleLabel = new Label(messageService.getMessage("gameOver.title", gameMap.getAdventurer().getName()));
    titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");
    titleLabel.getStyleClass().add("medieval-font");

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
    InnerShadow innerShadow = new InnerShadow(15.0, 5, 5, Color.rgb(0, 0, 0, 0.5));
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

    // Credits
    creditsOverlay = new CreditsOverlay(hostServices, sharedSize.getWidth(), sharedSize.getHeight());
    credits = new Text(messageService.getMessage("credits.text"));
    credits.getStyleClass().add("small-text-elem");
    credits.setOnMouseClicked(e -> creditsOverlay.show());

    // Add elements to main VBox
    VBox main = new VBox(20, titleLabel, contentBox, buttonBox, credits);
    main.setAlignment(Pos.CENTER);
    root.getChildren().addAll(main, creditsOverlay);

    // Background style
    root.setStyle("-fx-background-color: #2C3E50;");

    setOnKeyPressed(this::handleKeyPressed);

  }

  @Override
  protected void onSizeChanged(double width, double height) {
    creditsOverlay.updateSize(width, height);
  }

  public void setOnReplayGame(Runnable action) {
    replayButton.setOnAction(e -> action.run());
  }

  public void setOnQuitGame(Runnable action) {
    quitButton.setOnAction(e -> action.run());
  }

  @Override
  public void updateLanguage(Locale newLocale) {
    titleLabel.setText(messageService.getMessage("gameOver.title", gameMap.getAdventurer().getName()));
    quitButton.setText(messageService.getMessage("button.exit"));
    replayButton.setText(messageService.getMessage("button.replay"));
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
        label.setText(messageService.getMessage(wound.getCause().getCauseNameKey()) + ": " + messageService.getMessage(wound.getWoundMessageKey()));

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

  private void handleKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.C) {
      if (creditsOverlay.isVisible()) {
        creditsOverlay.hide();
      } else {
        creditsOverlay.show();
      }
    }
  }
}
