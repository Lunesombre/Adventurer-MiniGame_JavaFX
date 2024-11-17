package game.adventurer.controller;

import game.adventurer.common.Localizable;
import game.adventurer.service.LocalizedMessageService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RightPanelController implements Localizable {

  public static final int MESSAGES_NUMBER_LIMIT = 3;
  private final int padding;
  private VBox messagesBox;
  private final double minWidth;
  private final Map<String, List<String>> messageKeyMap; // Holds the references needed to update messages on language changes
  private final ObservableList<Label> messageList;
  private final LocalizedMessageService messageService;

  public RightPanelController(int padding, double minWidth) {
    this.messageService = LocalizedMessageService.getInstance();
    messageList = FXCollections.observableArrayList();
    messageList.clear();
    messageKeyMap = new LinkedHashMap<>() {  // need a LinkedHashMap to keep the insertion order
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
        return size() > MESSAGES_NUMBER_LIMIT;
        // this allows the map to automatically remove the oldest entry when it returns true
      }
    };
    this.minWidth = minWidth;
    this.padding = padding;
  }


  public void addMessage(String newMessageKey) {

    // loop to "dynamically" change the messages' color, font-weight and font size.
    messageFontColorFade();
    messageKeyMap.put(newMessageKey, null);
    Label messageText = new Label(messageService.getMessage(newMessageKey));
    stylizeNewMessage(messageText);

    messageList.add(messageText);

    // Set a limit to how many messages can be displayed
    if (messageList.size() > MESSAGES_NUMBER_LIMIT) {
      messageList.removeFirst(); // Remove the oldest message
      // No need to check on the messageKeyMap as the overridden method removeEldestEntry handles this automatically
    }

    // Update the messageBox
    updateMessagesBox();
  }

  public void addMessage(String newMessageKey, String... args) {

    // loop to "dynamically" change the messages' color, font-weight and font size.
    messageFontColorFade();
    List<String> argsList = new ArrayList<>(Arrays.asList(args));
    messageKeyMap.put(newMessageKey, argsList);
    List<String> updatedArgs = new ArrayList<>();
    for (String arg : argsList) {
      updatedArgs.add(messageService.getMessage(arg)); // fetch the corresponding text if it finds a valid key, pass the arg per se if not.
    }
    Label messageText = new Label(messageService.getMessage(newMessageKey, updatedArgs.toArray()));
    stylizeNewMessage(messageText);

    messageList.add(messageText);

    // Set a limit to how many messages can be displayed
    if (messageList.size() > MESSAGES_NUMBER_LIMIT) {
      messageList.removeFirst(); // Remove the oldest message
      // No need to check on the messageKeyMap as the overridden method removeEldestEntry handles this automatically
    }

    // Update the messageBox
    updateMessagesBox();
  }

  private void messageFontColorFade() {
    int fillColorValue = 230;
    for (int i = messageList.size() - 1; i >= 0; i--) {
      Label message = messageList.get(i);
      message.setTextFill(Color.rgb(fillColorValue, fillColorValue, fillColorValue));
      message.setFont(Font.font("Luciole", FontWeight.NORMAL, 18.0));
      message.setStyle("-fx-background-color: #205c55; -fx-background-radius: 10; -fx-border-radius: 10;");

      fillColorValue -= 60;
    }
  }

  private void stylizeNewMessage(Label message) {
    message.setTextFill(Color.rgb(225, 190, 45));
    message.setPadding(new Insets(12));
    message.setWrapText(true); // Will attempt to wrap the Label. If there's not enough room in the messageBox, will truncate it with "..."
    message.setPrefWidth(Math.max(minWidth, messagesBox.getWidth()) - padding);
    message.setFont(Font.font("Luciole", FontWeight.SEMI_BOLD, 20.0));
    message.setStyle(
        "-fx-background-color: #1b4c46; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1px; -fx-border-color: #c6c4c4;"
    );
  }

  private void updateMessagesBox() {
    messagesBox.getChildren().clear();
    messagesBox.getChildren().addAll(messageList);
  }

  // Init messagesBox
  public void initMessagesBox(VBox messagesBox) {
    this.messagesBox = messagesBox;

    // Listener to automatically re-wrap the messages when the scene's width changes.
    messagesBox.widthProperty().addListener((obs, oldWidth, newWidth) -> {
      double labelWidth = Math.max(minWidth, newWidth.doubleValue()) - padding;
      for (Label message : messageList) {
        message.setPrefWidth(labelWidth);
      }
    });


  }

  @Override
  public void updateLanguage(Locale newLocale) {
    if (messageList.size() != messageKeyMap.size()) {
      throw new IllegalStateException("messageKeyMap and messageList are out of sync");
    }
    for (int i = 0; i < messageList.size(); i++) {
      String localizedMessage;
      String key = (String) messageKeyMap.keySet().toArray()[i];
      List<String> args = messageKeyMap.get(key);
      if (args != null && !args.isEmpty()) {
        List<String> updatedArgs = new ArrayList<>();
        for (String arg : args) {
          updatedArgs.add(messageService.getMessage(arg)); // fetch the corresponding text if it finds a valid key, pass the arg per se if not.
          // Allows getting variable parts that could have changed with the Locale change.
        }
        localizedMessage = messageService.getMessage(key, updatedArgs.toArray());
      } else {
        localizedMessage = messageService.getMessage(key);
      }
      if (localizedMessage != null) {
        messageList.get(i).setText(localizedMessage);
      } else {
        log.warn("No message found on this key : {}", key);
      }
    }
    updateMessagesBox();
  }
}

