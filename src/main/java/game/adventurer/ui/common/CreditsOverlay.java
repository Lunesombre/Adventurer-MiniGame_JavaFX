package game.adventurer.ui.common;

import game.adventurer.common.Localizable;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import java.util.Locale;
import java.util.Objects;
import javafx.application.HostServices;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreditsOverlay extends StackPane implements Localizable {

  private final HostServices hostServices;

  private static final String APP_NAME = "Adventurer Game";
  private static final String LINKS = "links";
  private static final String TEXT_WHITE = "text-white";
  private static final double INNER_WIDTH = 700;

  private static final String COPYRIGHT_STRING = "© 2024 by Romain Touchet, licensed under CC BY-NC-SA 4.0";

  private static final String VERSION_NUMBER = "1.0.0";
  private static final LocalizedMessageService messageService = LocalizedMessageService.getInstance();
  //Elements to localize
  private Text infos1;
  private Text infos2;
  private Text infos3;
  private Text infos4;
  private Text gitHubText;
  private Hyperlink githubProjectLink;
  private Text thanks;

  /**
   * Constructor of CreditsOverlay for use within Scenes WITH an inbound language change option.
   */
  public CreditsOverlay(HostServices hostServices, LocalizationService localizationService, double windowWidth, double windowHeight) {
    super();
    this.hostServices = hostServices;
    localizationService.registerLocalizable(this);
    initialize(windowWidth, windowHeight);
  }

  /**
   * Constructor of CreditsOverlay for use within Scenes without an inbound language change option.
   */
  public CreditsOverlay(HostServices hostServices, double windowWidth, double windowHeight) {
    super();
    this.hostServices = hostServices;
    initialize(windowWidth, windowHeight);
  }

  private void initialize(double windowWidth, double windowHeight) {
    setAlignment(Pos.CENTER);
    setPrefSize(windowWidth, windowHeight);
    setMinSize(windowWidth, windowHeight);
    setMaxSize(windowWidth, windowHeight);
    Rectangle background = new Rectangle(windowWidth, windowHeight);
    background.setFill(Color.rgb(0, 0, 0, 0.8));
    getChildren().addFirst(background);
    VBox content = new VBox(40);
    content.setAlignment(Pos.CENTER);
    setPrefSize(windowWidth, windowHeight);
    setMinSize(windowWidth, windowHeight);
    setMaxSize(windowWidth, windowHeight);
    setLayoutX(windowWidth / 2); // centers it horizontally
    setLayoutY(windowHeight / 2); // vertical positioning

    // TITLE
    Text appName = new Text(APP_NAME);
    appName.getStyleClass().addAll(TEXT_WHITE, "text-50");

    // COPYRIGHT STRING
    Text copyrightText = new Text(COPYRIGHT_STRING);
    copyrightText.getStyleClass().add(TEXT_WHITE);
    Text versionText = new Text("v" + VERSION_NUMBER);
    versionText.getStyleClass().add(TEXT_WHITE);

    // GITHUB - PROJECT LINK
    githubProjectLink = new Hyperlink(messageService.getMessage("credits.github.project"));
    githubProjectLink.setTextAlignment(TextAlignment.CENTER);
    githubProjectLink.getStyleClass().add(LINKS);
    githubProjectLink.setOnAction(e -> this.hostServices.showDocument("https://github.com/Lunesombre/Adventurer-MiniGame_JavaFX"));

    // GITHUB PROFILE LINK
    Hyperlink githubLink = new Hyperlink("Lunesombre");
    githubLink.getStyleClass().add(LINKS);
    githubLink.setOnAction(e -> this.hostServices.showDocument("https://github.com/Lunesombre"));
    gitHubText = new Text(messageService.getMessage("credits.github.profile"));
    gitHubText.getStyleClass().add(TEXT_WHITE);
    TextFlow gitHubTextFlow = new TextFlow(gitHubText, githubLink);
    gitHubTextFlow.setTextAlignment(TextAlignment.CENTER);

    // HBOX FOR GITHUB LINKS
    HBox githubLinks = new HBox();
    githubLinks.setMaxWidth(INNER_WIDTH);
    HBox.setHgrow(gitHubTextFlow, Priority.ALWAYS);
    HBox.setHgrow(githubProjectLink, Priority.ALWAYS);
    gitHubTextFlow.setTextAlignment(TextAlignment.LEFT);
    githubProjectLink.setTextAlignment(TextAlignment.RIGHT);
    githubLinks.getChildren().addAll(gitHubTextFlow, githubProjectLink);

    // EMAIL LINK
    Hyperlink emailLink = new Hyperlink("email");
    emailLink.setTextAlignment(TextAlignment.CENTER);
    emailLink.getStyleClass().add(LINKS);
    emailLink.setOnAction(e -> this.hostServices.showDocument("mailto:romain.touchet+adventurer.game@gmail.com"));
    Text emailText = new Text("Contact:");
    emailText.getStyleClass().add(TEXT_WHITE);
    TextFlow emailTextFlow = new TextFlow(emailText, emailLink);
    emailTextFlow.setTextAlignment(TextAlignment.CENTER);

    // THANKS
    thanks = new Text(messageService.getMessage("credits.thanks"));
    thanks.getStyleClass().addAll(TEXT_WHITE, "text-16");
    thanks.setTextAlignment(TextAlignment.CENTER);

    // INFOS
    infos1 = new Text(messageService.getMessage("credits.infos1"));
    infos2 = new Text(messageService.getMessage("credits.infos2"));
    infos3 = new Text(messageService.getMessage("credits.infos3"));
    infos4 = new Text(messageService.getMessage("credits.infos4"));

    // Creates and sets the containers for each Text, to get the wanted "left/right" effect
    StackPane container1 = createTextContainer(infos1, TextAlignment.LEFT);
    StackPane container2 = createTextContainer(infos2, TextAlignment.RIGHT);
    StackPane container3 = createTextContainer(infos3, TextAlignment.LEFT);
    StackPane container4 = createTextContainer(infos4, TextAlignment.RIGHT);

    VBox infos = new VBox(20); // 20 pixels of spacing between elements
    infos.setAlignment(Pos.CENTER);
    infos.getChildren().addAll(container1, container2, container3, container4);

    // DISMISS OVERLAY
    StackPane closeIcon = createCloseIcon();
    // position the closeIcon top-right
    // NB : StackPane.setAlignment(closeIcon, Pos.TOP_RIGHT); doesn't work here
    closeIcon.setTranslateX(windowWidth / 2 - 40);
    closeIcon.setTranslateY(-windowHeight / 2 + 40);

    // ADD CHILDREN TO THE ROOT
    content.getChildren().addAll(appName, versionText, copyrightText, infos, githubLinks, emailTextFlow, thanks);
    getChildren().addAll(content, closeIcon);

    setVisible(false);
  }

  public void show() {
    setVisible(true);
    toFront();
  }

  public void hide() {
    setVisible(false);
    toBack();
  }

  public void updateSize(double newWidth, double newHeight) {
    setPrefSize(newWidth, newHeight);
    setMinSize(newWidth, newHeight);
    setMaxSize(newWidth, newHeight);
    ((Rectangle) getChildren().getFirst()).setWidth(newWidth);
    ((Rectangle) getChildren().getFirst()).setHeight(newHeight);

    setLayoutX(newWidth / 2);
    setLayoutY(newHeight / 2);
    getChildren().getLast().setTranslateX(newWidth / 2 - 40);
    getChildren().getLast().setTranslateY(-newHeight / 2 + 40);
  }

  private StackPane createCloseIcon() {
    Image crossIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/cross-mark.png")));
    ImageView iconView = new ImageView(crossIcon);
    iconView.setFitHeight(25);
    iconView.setFitWidth(25);

    Circle iconBackground = new Circle(20);
    iconBackground.setCursor(Cursor.HAND);
    iconBackground.getStyleClass().add("icon-background");

    StackPane closeIcon = new StackPane(iconView, iconBackground);

    iconBackground.setOnMouseClicked(e -> {
      hide();
      e.consume();
    });

    return closeIcon;
  }

  private StackPane createTextContainer(Text text, TextAlignment alignment) {
    text.getStyleClass().addAll(TEXT_WHITE, "text-16");
    text.setTextAlignment(alignment);
    text.setWrappingWidth(INNER_WIDTH);

    StackPane container = new StackPane(text);
    container.setPrefWidth(INNER_WIDTH);
    container.setMinWidth(INNER_WIDTH);
    container.setMaxWidth(INNER_WIDTH);
    StackPane.setAlignment(text, alignment == TextAlignment.LEFT ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

    return container;
  }

  @Override
  public void updateLanguage(Locale newLocale) {
    infos1.setText(messageService.getMessage("credits.infos1"));
    infos2.setText(messageService.getMessage("credits.infos2"));
    infos3.setText(messageService.getMessage("credits.infos3"));
    infos4.setText(messageService.getMessage("credits.infos4"));
    thanks.setText(messageService.getMessage("credits.thanks"));
    gitHubText.setText(messageService.getMessage("credits.github.profile"));
    githubProjectLink.setText(messageService.getMessage("credits.github.project"));
  }
}
