package game.adventurer.ui.common;

import game.adventurer.common.SharedSize;
import game.adventurer.config.AppConfig;
import game.adventurer.exceptions.InvalidGameStateException;
import java.util.Objects;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;

@Getter
public abstract class BaseScene extends Scene {

  protected final SharedSize sharedSize;

  protected BaseScene(Parent root, SharedSize sharedSize) {
    super(root, sharedSize.getWidth(), sharedSize.getHeight());
    this.sharedSize = sharedSize;
    applyGlobalStyles();
    // don't put initialize() here if some variable are being instantiated in the initialize() of classes inheriting BaseScene.
  }

  protected abstract void initialize() throws InvalidGameStateException;

  protected void applyGlobalStyles() {
    String cssPath = AppConfig.getInstance().getGlobalStylePath();

    getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
  }

  protected void updateSize() {
    sharedSize.setWidth(getWidth());
    sharedSize.setHeight(getHeight());
  }

}
