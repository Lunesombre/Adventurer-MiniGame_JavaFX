package game.adventurer.ui.common;

import game.adventurer.ui.common.option.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OptionsPanel extends StackPane {

  private final ImageView iconView;
  private final Image gearIcon;
  private final Image crossIcon;
  private final VBox optionsContent;
  private final TranslateTransition slideIn;
  private final TranslateTransition slideOut;
  public static final int PADDING_VALUE = 10;
  @Getter
  private boolean isShowing = false;
  private final List<Consumer<Boolean>> toggleListeners = new ArrayList<>();

  public OptionsPanel(double sceneWidth, double sceneHeight, Option<?>... options) {
    // Icon
    Circle iconBackground = new Circle(20);
    iconBackground.setCursor(Cursor.HAND);
    iconBackground.getStyleClass().add("icon-background");

    gearIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/param-gear.png")));
    crossIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/cross-mark.png")));

    iconView = new ImageView(gearIcon);
    iconView.setFitHeight(25);
    iconView.setFitWidth(25);

    StackPane iconContainer = new StackPane(iconView, iconBackground);
    iconContainer.setMaxSize(40, 40);

    // Options content
    optionsContent = new VBox();
    optionsContent.setStyle("-fx-background-color: rgba(50,50,50,0.8); -fx-padding: 20;");
    optionsContent.setPrefWidth(sceneWidth * 0.20); // 20% of the scene width
    optionsContent.setMaxHeight(sceneHeight);

    // Options placeholders at first
    Label optionsLabel = new Label("Options");
    optionsLabel.setStyle("-fx-padding: 0 0 10px 0;");
    optionsLabel.setTextFill(Color.WHITE);
    optionsContent.getChildren().add(optionsLabel);
    if (options.length == 0) {
      Label noOption = new Label("Aucune option disponible");
      optionsContent.getChildren().add(noOption);
    } else {
      for (Option<?> option : options) {
        HBox optionLine;
        if (option.getNode() instanceof TitledPane titledPane) {
          // don't add the Label in the HBox, the name's already on the titledPane
          log.debug(titledPane.toString());
          titledPane.setMaxHeight(100);
          optionLine = new HBox(15, titledPane);
        } else {
          Label optionLabel = new Label(option.getName());
          optionLabel.setStyle("-fx-font-weight: bold;");
          optionLabel.setMaxHeight(100);
          log.debug(option.getNode().toString());
          optionLine = new HBox(15, optionLabel, option.getNode());
        }
        optionLine.setFillHeight(true); // in combination of setting a big enough maxHeight for the Labels,
        // renders the Labels vertically centered in the HBox
        optionLine.getStyleClass().add("option-line");

        optionsContent.getChildren().add(optionLine);

        option.onValueChange(value -> log.info("Option '{}' changed to: {}", option.getName(), value));
      }
    }

    // Hiding the panel out of the scene
    optionsContent.setTranslateX(sceneWidth);

    // Set the animations
    slideIn = new TranslateTransition(Duration.seconds(0.3), optionsContent);
    slideOut = new TranslateTransition(Duration.seconds(0.3), optionsContent);

    // This StackPane covers all the Scene to more easily position the iconContainer, but shall allow clicking through it
    StackPane iconOverlay = new StackPane(iconContainer);
    iconOverlay.setAlignment(Pos.TOP_RIGHT);
    iconOverlay.setPadding(new Insets(PADDING_VALUE));
    iconOverlay.setPickOnBounds(false); // Allows click-through

    // Add everything to the StackPane
    getChildren().addAll(optionsContent, iconOverlay);

    // Handle click on icon
    iconBackground.setOnMouseClicked(e -> {
      toggleOptions();
      e.consume();
    });

    setPickOnBounds(false); // Allows clicking through the OptionsPanel's root (StackPane)

    setOnKeyPressed(this::handleOptionKeyPress);
  }

  public void toggleOptions() {
    if (isShowing) {
      iconView.setImage(gearIcon);
      slideOut.setToX(getWidth() + PADDING_VALUE * 2);
      slideOut.play();
    } else {
      slideIn.setToX(0);
      slideIn.play();
      requestFocus();
    }
    isShowing = !isShowing;
    log.debug("OptionsPanel toggled. isShowing: {}", isShowing);
    if (isShowing) {
      slideIn.setOnFinished(event -> iconView.setImage(crossIcon));
    }

    for (Consumer<Boolean> listener : toggleListeners) {
      listener.accept(isShowing);
    }
  }

  public void updateSize(double width, double height) {
    optionsContent.setPrefWidth(width * 0.20);
    optionsContent.setMaxHeight(height);
    if (!isShowing) {
      optionsContent.setTranslateX(width + PADDING_VALUE * 2);
    } else {
      optionsContent.setTranslateX(0);
    }
    log.debug("optionsWidth au resize: {}", optionsContent.getWidth());
  }

  private void handleOptionKeyPress(KeyEvent event) {
    log.info("Key pressed on OptionsPanel: {}", event.getCode());
    if (isShowing) {
      switch (event.getCode()) {
        case UP, DOWN, LEFT, RIGHT, ENTER -> event.consume(); // Prevents propagation
        case SPACE, ESCAPE -> {
          toggleOptions();
          event.consume();
        }
        default -> log.info("Unhandled key : {}", event.getCode());
      }
    }
  }


  public void addToggleListener(Consumer<Boolean> listener) {
    toggleListeners.add(listener);
  }

  public void removeToggleListener(Consumer<Boolean> listener) {
    toggleListeners.remove(listener);
  }


}
