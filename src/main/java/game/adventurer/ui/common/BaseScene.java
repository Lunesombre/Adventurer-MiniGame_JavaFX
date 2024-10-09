package game.adventurer.ui.common;

import static game.adventurer.util.MiscUtil.applyGlobalCss;

import game.adventurer.common.SharedSize;
import game.adventurer.exceptions.InvalidGameStateException;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class BaseScene extends Scene {

  protected final SharedSize sharedSize;

  protected BaseScene(Parent root, SharedSize sharedSize) {
    super(root, sharedSize.getWidth(), sharedSize.getHeight());
    this.sharedSize = sharedSize;
    applyGlobalCss(this);
    // don't put initialize() here if some variable are being instantiated in the initialize() of classes inheriting BaseScene.

    widthProperty().addListener((obs, oldVal, newVal) -> updateSize());
    heightProperty().addListener((obs, oldVal, newVal) -> updateSize());
  }

  protected abstract void initialize() throws InvalidGameStateException;

  protected void updateSize() {
    sharedSize.setWidth(getWidth());
    sharedSize.setHeight(getHeight());
    onSizeChanged(getWidth(), getHeight());
  }

  protected abstract void onSizeChanged(double width, double height);
}
