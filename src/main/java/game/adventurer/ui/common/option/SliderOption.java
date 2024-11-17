package game.adventurer.ui.common.option;

import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import lombok.Getter;

public class SliderOption implements Option<Number> {

  @Getter
  private final String nameKey;
  private final Slider slider;

  public SliderOption(String nameKey, double min, double max, double initialValue) {
    this.nameKey = nameKey;
    this.slider = new Slider(min, max, initialValue);
  }

  @Override
  public Node getNode() {
    return slider;
  }

  @Override
  public void onValueChange(Consumer<Number> listener) {
    slider.valueProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
  }
}
