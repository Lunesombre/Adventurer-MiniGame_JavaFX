package game.adventurer.ui.common.option;


import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import lombok.Getter;

public class CheckboxOption implements Option<Boolean> {

  @Getter
  private final String nameKey;
  private final CheckBox checkBox;

  public CheckboxOption(String nameKey, boolean initialValue) {
    this.nameKey = nameKey;
    this.checkBox = new CheckBox();
    this.checkBox.setSelected(initialValue);
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
