import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    // Define different types of messages/actions
    public enum Type {
        SET_USERNAME, CREATE_GROUP, SEND_MESSAGE, USER_LIST_UPDATE, GROUP_LIST_UPDATE, ERROR, SUCCESS,
        CHECK_USERNAME, USERNAME_AVAILABLE, USERNAME_TAKEN, SET_USERNAME_RESPONSE, NEW_USER, CHAT_MESSAGE, UPDATE_LIST, SEND_TO_ALL,
        RECIEVE_MESSAGE, SENT_TO_ALL
    }

    private Type messageType;
    private String senderUsername;
    private String recipientUsername;
    private ArrayList<String> recipients; // For groups or individual recipient
    private String content; // For messages
    private String groupName; // For creating groups
    private ArrayList<String> userList;
//    private ObservableList<String> userList = FXCollections.observableArrayList();

    // Constructor for different types of messages
    public Message(Type messageType, String username) {
        this.messageType = messageType;
        this.senderUsername = username;
//        this.recipients = new ArrayList<>(); // Initialize recipients list
    }

    public Message(Type messageType) {
        this.messageType = messageType;
    }

    public Message(Type type, String userName, String chat, String myUsername) {
        this.messageType = type;
        this.recipientUsername = userName;
        this.content = chat;
        this.senderUsername = myUsername;
    }

    public Message(Type messageType, ArrayList<String> list) {
        this.messageType = messageType;
        this.userList = list;
    }


    // Getters and setters
    public Type getMessageType() {
        return messageType;
    }

    public void setMessageType(Type messageType) {
        this.messageType = messageType;
    }

    public String getSenderUsername() {
        return senderUsername;
    }
    public String getRecipientUsername(){return recipientUsername;}

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public ArrayList<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(ArrayList<String> recipients) {
        this.recipients = recipients;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // Method to add a single recipient (useful for individual messages)
    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }

    public ArrayList<String> getUserList() {

        return new ArrayList<>(userList);
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }
}
