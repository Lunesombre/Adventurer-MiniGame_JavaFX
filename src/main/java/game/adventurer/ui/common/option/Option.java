package game.adventurer.ui.common.option;

import java.util.function.Consumer;
import javafx.scene.Node;

public interface Option<T> {

  String getNameKey();

  Node getNode();

  void onValueChange(Consumer<T> listener);
}
