package game.adventurer.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


public class RightPanelController {

  public static final int MESSAGES_NUMBER_LIMIT = 3;
  private final int padding;
  private VBox messagesBox;
  private final double minWidth;
  private final ObservableList<Label> messageList;

  public RightPanelController(int padding, double minWidth) {
    messageList = FXCollections.observableArrayList();
    this.minWidth = minWidth;
    this.padding = padding;
  }


  public void addMessage(String newMessage) {

    // loop to "dynamically" change the messages' color, font-weight and font size.
    int fillColorValue = 230;
    for (int i = messageList.size() - 1; i >= 0; i--) {
      Label message = messageList.get(i);
      message.setTextFill(Color.rgb(fillColorValue, fillColorValue, fillColorValue));
      message.setFont(Font.font("Luciole", FontWeight.NORMAL, 18.0));
      message.setStyle("-fx-background-color: #205c55; -fx-background-radius: 10; -fx-border-radius: 10;");

      fillColorValue -= 60;
    }
    Label messageText = new Label(newMessage);
    messageText.setTextFill(Color.rgb(225, 190, 45));
    messageText.setPadding(new Insets(12));
    messageText.setWrapText(true); // Will attempt to wrap the Label. If there's not enough room in the messageBox, will truncate it with "..."
    messageText.setPrefWidth(Math.max(minWidth, messagesBox.getWidth()) - padding);
    messageText.setFont(Font.font("Luciole", FontWeight.SEMI_BOLD, 20.0));
    messageText.setStyle(
        "-fx-background-color: #1b4c46; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1px; -fx-border-color: #c6c4c4;"
    );

    messageList.add(messageText);

    // Set a limit to how many messages can be displayed
    if (messageList.size() > MESSAGES_NUMBER_LIMIT) {
      messageList.removeFirst(); // Remove the oldest message
    }

    // Update the display
    updateMessagesBox();
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
}

