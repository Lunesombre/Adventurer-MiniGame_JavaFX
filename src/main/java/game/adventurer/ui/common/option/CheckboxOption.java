package game.adventurer.ui.common.option;


import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class CheckboxOption implements Option<Boolean> {

  private final String name;
  private final CheckBox checkBox;

  public CheckboxOption(String name, boolean initialValue) {
    this.name = name;
    this.checkBox = new CheckBox();
    this.checkBox.setSelected(initialValue);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Node getNode() {
    return checkBox;
  }

  @Override
  public void onValueChange(Consumer<Boolean> listener) {
    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
  }
}
