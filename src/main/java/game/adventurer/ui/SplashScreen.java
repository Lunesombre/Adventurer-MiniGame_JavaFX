package game.adventurer.ui;

import game.adventurer.config.SceneConfig;
import game.adventurer.exceptions.FontLoadException;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends Scene {

  public static final Logger LOG = LoggerFactory.getLogger(SplashScreen.class);
  private final String titleFontPath;
  private static final double FONT_SIZE_RATIO = 0.13;

  private String appName;

  private final Stop[] stops = new Stop[]{
      new Stop(0, Color.web("#40E0D0")),
      new Stop(33, Color.web("#ff8c00")),
      new Stop(66, Color.web("#ff0080"))
  };
  private final LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
      stops); // can use a LinearGradient on TextFill as it extends Paint


  private SplashScreen(StackPane root, int width, int height, String titleFontPath) {
    super(root, width, height);
    this.titleFontPath = titleFontPath;
  }

  //factory
  public static SplashScreen create(String appName) throws FontLoadException {
    SceneConfig config = SceneConfig.getInstance();
    StackPane root = new StackPane();
    SplashScreen scene = new SplashScreen(root, config.getWidth(), config.getHeight(), config.getTitleFontPath());
    scene.appName = appName;
    scene.initialize();
    return scene;
  }

  private void initialize() throws FontLoadException {
    SceneConfig config = SceneConfig.getInstance();
    int windowWidth = config.getWidth();
    int windowHeight = config.getHeight();

    StackPane root = (StackPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);

    Font titleFont = loadCustomFont(windowHeight);

    Label nameLabel = new Label(appName);
    nameLabel.setFont(titleFont);
    nameLabel.setPadding(new Insets(40, 40, 40, 40));
    nameLabel.setTextFill(lg1);

    root.getChildren().add(nameLabel);
    root.setStyle("-fx-background-color: #403f3f;");

    nameLabel.fontProperty().bind(Bindings.createObjectBinding(() ->
        loadCustomFont(getHeight()), heightProperty()
    ));

    // Adding listeners for resizing
    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());
  }

  private void updateSize() {
    SceneConfig config = SceneConfig.getInstance();
    int windowWidth = config.getWidth();
    int windowHeight = config.getHeight();

    StackPane root = (StackPane) getRoot();
    root.setPrefSize(windowWidth, windowHeight);
  }

  private Font loadCustomFont(double initialSize) throws FontLoadException {
    Font titleFont;
    try {
      titleFont = Font.loadFont(getClass().getResourceAsStream(titleFontPath), initialSize * FONT_SIZE_RATIO);
      if (titleFont == null) {
        throw new FontLoadException("Font not loaded");
      }
    } catch (FontLoadException e) {
      LOG.warn("Custom title font not loaded, using fallback font");
      titleFont = Font.font("Arial", FontWeight.BOLD, initialSize * FONT_SIZE_RATIO);
    }
    return titleFont;
  }

}
