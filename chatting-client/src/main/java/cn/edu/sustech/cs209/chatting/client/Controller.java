package cn.edu.sustech.cs209.chatting.client;

//import cn.edu.sustech.cs209.chatting.common.Message;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<Message> chatList;
    @FXML
    TextArea inputArea;
    @FXML
    Label currentUsername;
    @FXML
    Label currentOnlineCnt;
    String username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        while (true) {
            if (!input.isPresent()) {
                Platform.exit();
                break;
            }else if (!input.get().isEmpty() && Client.login(input.get())) {
                username = input.get();
                currentUsername.setText("Current User: " + username);
                chatContentList.setCellFactory(new MessageCellFactory());
                chatList.setCellFactory(new MessageCellFactory());
                break;
            } else {
                dialog.setContentText("This username [" + input.get() + "] is exiting, please choose other name!");
                input = dialog.showAndWait();
            }
        }
    }

    public String curr_sendTo_user; // AtomicReference<String>
    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        String[] un = Client.getNameList(username);
        userSel.getItems().addAll(un);
        int online_cnt = un.length+1;
        currentOnlineCnt.setText("Online: "+online_cnt);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            curr_sendTo_user = user.get();
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

        chatList.getItems().add(new Message(new Date().getTime(), user.get(), username,""));
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        String input = inputArea.getText();
        if (!input.equals("")) {
            inputArea.clear();
            long curr_time = new Date().getTime();
            Client.sendPost(curr_time, username, curr_sendTo_user, input);
            chatContentList.getItems().add(new Message(curr_time, username, curr_sendTo_user, input));
        }
    }

    public static int pull_count = 0;
    @FXML
    public void doPullMessage() {
        String raw = Client.getMessage(username, pull_count);
        if (!raw.equals("")) {
            String[] raw_mess_List = raw.split("&&");
            pull_count += (raw_mess_List.length);
            for (String s : raw_mess_List) {
                String[] raw_mess = s.split("##");
                chatContentList.getItems().add(
                    new Message(Long.parseLong(raw_mess[0]), raw_mess[1], raw_mess[2], raw_mess[3]));
            }
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}







class Message {
    private Long timestamp;
    private String sentBy;
    private String sendTo;
    private String data;
    public Message(Long timestamp, String sentBy, String sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public String getSentBy() {
        return sentBy;
    }
    public String getSendTo() {
        return sendTo;
    }
    public String getData() {
        return data;
    }
}
