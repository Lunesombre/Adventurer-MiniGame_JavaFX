package game.adventurer.ui.common.option;

import static game.adventurer.util.MiscUtil.alertInitializer;
import static game.adventurer.util.MiscUtil.applyGlobalCss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class KeyBindingOption implements Option<Map<String, KeyCode>> {

  private final String name;
  private final Map<String, KeyCode> bindings;
  private final Button openDialogButton;
  private Consumer<Map<String, KeyCode>> changeListener;
  private final List<KeyCode> forbiddenKeys = List.of(KeyCode.SPACE, KeyCode.ESCAPE, KeyCode.ENTER);
  private Map<String, KeyCode> tempBindings;


  public KeyBindingOption(String name, Map<String, KeyCode> defaultBindings) {
    this.name = name;
    this.bindings = new HashMap<>(defaultBindings);
    this.openDialogButton = new Button("Rebind");
    setupLayout();
  }

  private void setupLayout() {
    openDialogButton.getStyleClass().add("key-binding-button");
    openDialogButton.setOnAction(e -> showKeyBindingDialog());
  }

  private void showKeyBindingDialog() {
    tempBindings = new HashMap<>(bindings);

    Alert alert = alertInitializer(getClass(), null, "Configuration des touches", "Modifiez les touches de jeu", "keybinding-alert",
        "/assets/icons/cycle.png", false);

    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 40, 10, 40));

    int row = 0;
    for (Map.Entry<String, KeyCode> entry : tempBindings.entrySet()) {
      Label actionLabel = new Label(entry.getKey() + " :");
      TextField keyField = new TextField(entry.getValue().getName());
      keyField.setEditable(false);
      final Button changeButton = getChangeButton(entry, keyField);

      grid.add(actionLabel, 0, row);
      grid.add(keyField, 1, row);
      grid.add(changeButton, 2, row);
      row++;
    }

    alert.getDialogPane().setContent(grid);
    alert.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    alert.getButtonTypes().forEach(buttonType -> {
      Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
      if (buttonType == ButtonType.OK) {
        button.setText("Valider");
        button.getStyleClass().add("confirm-button");
      } else if (buttonType == ButtonType.CANCEL) {
        button.setText("Annuler");
        button.getStyleClass().add("cancel-button");
      }
    });

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      bindings.clear();
      bindings.putAll(tempBindings);
      if (changeListener != null) {
        changeListener.accept(bindings);
      }
    }

  }

  private Button getChangeButton(Entry<String, KeyCode> entry, TextField keyField) {
    Button changeButton = new Button("Changer");
    changeButton.setOnAction(e -> {
      Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setTitle("Appuyez sur une touche");
      dialog.setResizable(false);
      Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/cycle.png")));
      dialog.getIcons().add(icon);
      Text promptText = new Text("Appuyez sur la nouvelle touche pour " + entry.getKey());
      VBox textBox = new VBox(20, promptText);
      textBox.getStyleClass().add("keybinding-prompt");
      textBox.setAlignment(Pos.CENTER);
      Scene dialogScene = new Scene(textBox, 600, 150);
      applyGlobalCss(dialogScene);

      dialogScene.setOnKeyPressed(event -> {
        KeyCode newKey = event.getCode();
        if (forbiddenKeys.contains(newKey)) {
          showErrorAlert("Touche interdite", "Cette touche est assignée à une autre action, non modifiable.");
        } else if (isKeyAlreadyUsed(newKey, entry.getKey(), tempBindings)) {
          showErrorAlert("Touche déjà utilisée", "Cette touche est déjà assignée à une autre action.");
        } else {
          keyField.setText(newKey.getName());
          tempBindings.put(entry.getKey(), newKey);
          dialog.close();
        }
      });

      dialog.setScene(dialogScene);
      dialog.showAndWait();
    });
    return changeButton;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Node getNode() {
    return openDialogButton;
  }

  @Override
  public void onValueChange(Consumer<Map<String, KeyCode>> listener) {
    this.changeListener = listener;
  }

  public Map<String, KeyCode> getBindings() {
    return new HashMap<>(bindings);
  }

  private boolean isKeyAlreadyUsed(KeyCode key, String currentAction, Map<String, KeyCode> currentBindings) {
    for (Map.Entry<String, KeyCode> entry : currentBindings.entrySet()) {
      if (!entry.getKey().equals(currentAction) && entry.getValue() == key) {
        return true;
      }
    }
    return false;
  }

  private void showErrorAlert(String title, String content) {
    Alert errorAlert = alertInitializer(getClass(), AlertType.ERROR, title, null, "alert", "/assets/icons/crane-et-os.png", true);
    errorAlert.setContentText(content);
    errorAlert.showAndWait();
  }


}
