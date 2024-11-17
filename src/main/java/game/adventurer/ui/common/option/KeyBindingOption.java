package game.adventurer.ui.common.option;

import static game.adventurer.util.MiscUtil.alertInitializer;
import static game.adventurer.util.MiscUtil.applyGlobalCss;

import game.adventurer.service.LocalizedMessageService;
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
import lombok.Setter;

public class KeyBindingOption implements Option<Map<String, KeyCode>> {

  @Setter
  private String nameKey;
  private final Map<String, KeyCode> bindings;
  private final Button openDialogButton;
  private Consumer<Map<String, KeyCode>> changeListener;
  private final List<KeyCode> forbiddenKeys = List.of(KeyCode.SPACE, KeyCode.ESCAPE, KeyCode.ENTER);
  private Map<String, KeyCode> tempBindings;
  private final LocalizedMessageService messageService = LocalizedMessageService.getInstance();


  public KeyBindingOption(String nameKey, Map<String, KeyCode> defaultBindings) {
    this.nameKey = !nameKey.isBlank() ? nameKey : "option.keybinding.label";
    this.bindings = new HashMap<>(defaultBindings);
    this.openDialogButton = new Button(messageService.getMessage("option.keybinding.button.open"));
    setupLayout();
  }

  private void setupLayout() {
    openDialogButton.getStyleClass().add("key-binding-button");
    openDialogButton.setOnAction(e -> showKeyBindingDialog());
  }

  private void showKeyBindingDialog() {
    tempBindings = new HashMap<>(bindings);
    String title = messageService.getMessage("option.keybinding.alert.title");
    String headerText = messageService.getMessage("option.keybinding.alert.header");
    Alert alert = alertInitializer(getClass(), null, title, headerText, "keybinding-alert",
        "/assets/icons/cycle.png", false);

    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 40, 10, 40));

    int row = 0;
    for (Map.Entry<String, KeyCode> entry : tempBindings.entrySet()) {
      String entryKeyLabel = messageService.getMessage(entry.getKey());
      Label actionLabel = new Label(messageService.getMessage("option.kb.alert.action.label", entryKeyLabel));
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
        button.setText(messageService.getMessage("button.validate"));
        button.getStyleClass().add("confirm-button");
      } else if (buttonType == ButtonType.CANCEL) {
        button.setText(messageService.getMessage("button.cancel"));
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
    Button changeButton = new Button(messageService.getMessage("option.keybinding.alert.button"));
    changeButton.setOnAction(e -> {
      Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setTitle(messageService.getMessage("option.keybinding.alert.dialog.title"));
      dialog.setResizable(false);
      Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/cycle.png")));
      dialog.getIcons().add(icon);
      String entryKeyLabel = messageService.getMessage(entry.getKey());
      Text promptText = new Text(messageService.getMessage("option.kb.alert.dialog.prompt", entryKeyLabel));
      VBox textBox = new VBox(20, promptText);
      textBox.getStyleClass().add("keybinding-prompt");
      textBox.setAlignment(Pos.CENTER);
      Scene dialogScene = new Scene(textBox, 600, 150);
      applyGlobalCss(dialogScene);

      dialogScene.setOnKeyPressed(event -> {
        KeyCode newKey = event.getCode();
        if (forbiddenKeys.contains(newKey)) {
          showErrorAlert(messageService.getMessage("option.kb.alert.error.forbidden.title"),
              messageService.getMessage("option.kb.alert.error.forbidden.content"));
        } else if (isKeyAlreadyUsed(newKey, entry.getKey(), tempBindings)) {
          showErrorAlert(messageService.getMessage("option.kb.alert.error.alreadyUsed.title"),
              messageService.getMessage("option.kb.alert.error.alreadyUsed.content"));
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
  public String getNameKey() {
    return nameKey;
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
