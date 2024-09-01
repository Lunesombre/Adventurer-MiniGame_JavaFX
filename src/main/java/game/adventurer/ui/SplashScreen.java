package game.adventurer.ui;

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

public class SplashScreen extends Scene {

  private final Stop[] stops = new Stop[]{
      new Stop(0, Color.web("#40E0D0")),
      new Stop(33, Color.web("#ff8c00")),
      new Stop(66, Color.web("#ff0080"))
  };
  private final LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
      stops); // can use a LinearGradient on TextFill as it extends Paint

  public SplashScreen(String appName) {
    super(new StackPane(), 800, 600);
    StackPane root = (StackPane) getRoot();

    Label nameLabel = new Label(appName);
    nameLabel.setFont(Font.font("Pacifico", FontWeight.BOLD, 80));
    nameLabel.setPadding(new Insets(40, 40, 40, 40));
    nameLabel.setTextFill(lg1);

    root.getChildren().add(nameLabel);
    root.setStyle("-fx-background-color: lightblue;");
  }

}
