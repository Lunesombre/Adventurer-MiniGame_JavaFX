package game.adventurer.ui;

import game.adventurer.common.SharedSize;
import game.adventurer.config.AppConfig;
import game.adventurer.exceptions.FontLoadException;
import game.adventurer.ui.common.BaseScene;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends BaseScene {

  public static final Logger LOG = LoggerFactory.getLogger(SplashScreen.class);
  private final String titleFontPath;
  private static final double FONT_SIZE_RATIO = 0.13;

  private String appName;

  private final Stop[] stops = new Stop[]{
      new Stop(0, Color.web("#40E0D0")),
      new Stop(33, Color.web("#ff8c00")),
      new Stop(66, Color.web("#ff0080"))
  };
  private final LinearGradient titleGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
      stops); // can use a LinearGradient on TextFill as it extends Paint


  private SplashScreen(StackPane root, String titleFontPath, SharedSize sharedSize) {
    super(root, sharedSize);
    this.titleFontPath = titleFontPath;

  }

  //factory
  public static SplashScreen create(String appName, SharedSize sharedSize) {
    AppConfig config = AppConfig.getInstance();
    StackPane root = new StackPane();
    SplashScreen scene = new SplashScreen(root, config.getTitleFontPath(), sharedSize);
    scene.appName = appName;
    scene.initialize();
    return scene;
  }

  @Override
  protected void initialize() {
    StackPane root = (StackPane) getRoot();

    Font titleFont = loadCustomFont(root.getHeight());

    Label nameLabel = new Label(appName);
    nameLabel.setFont(titleFont);
    nameLabel.setPadding(new Insets(40, 40, 40, 40));
    nameLabel.setTextFill(titleGradient);
    //Adding a bloom effect on the title
    Bloom bloom = new Bloom();
    bloom.setThreshold(0.1);
    nameLabel.setEffect(bloom);

    root.getChildren().add(nameLabel);
    root.setStyle("-fx-background-color: #403f3f;");

    nameLabel.fontProperty().bind(Bindings.createObjectBinding(() ->
        loadCustomFont(getHeight()), heightProperty()
    ));
  }

  @Override
  protected void onSizeChanged(double width, double height) {
    // nothing to add here for now
  }

  private Font loadCustomFont(double initialSize) {
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
