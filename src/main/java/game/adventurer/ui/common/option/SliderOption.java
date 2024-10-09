package game.adventurer.ui.common.option;

import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Slider;

public class SliderOption implements Option {

  private final String name;
  private final Slider slider;

  public SliderOption(String name, double min, double max, double initialValue) {
    this.name = name;
    this.slider = new Slider(min, max, initialValue);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Node getNode() {
    return slider;
  }

  @Override
  public void onValueChange(Consumer<Object> listener) {
    slider.valueProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
  }
}
