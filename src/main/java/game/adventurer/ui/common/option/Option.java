package game.adventurer.ui.common.option;

import java.util.function.Consumer;
import javafx.scene.Node;

public interface Option {

  String getName();

  Node getNode();

  void onValueChange(Consumer<Object> listener);
}
