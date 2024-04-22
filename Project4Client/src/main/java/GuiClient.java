import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;


public class GuiClient extends Application {

	private VBox userContainer = new VBox(10);
	private Timer refreshTimer;

	private ListView<String> userList;
	private Set<String> allUsernames;
	TextField c1;
	Button b1;
	private Stage primaryStage;

	VBox clientBox;
	Client clientConnection;
	HashMap<String, Scene> sceneMap;
	ListView<String> listItems2;
	boolean regenerateList = false;
	private ArrayList<String> chatMessageList = new ArrayList<>();
	private ArrayList<String> chatMessageRaw = new ArrayList<>();
	private ArrayList<String> chatAllList = new ArrayList<>();

	Message message;

	public void updateList(ArrayList<String> list) {
		Platform.runLater(() -> {
			userList.getItems().clear();
			userList.getItems().addAll(list);

			userContainer.getChildren().clear();

			for (String user : list) {
				boolean userExists = false;
				for (Node node : userContainer.getChildren()) {
					if (node instanceof Button && ((Button) node).getText().equals(user)) {
						userExists = true;
						break;
					}
				}
				if (!userExists) {
					Button userButton = createUserButton(user, this.primaryStage, false, user);
					userContainer.getChildren().add(userButton);
				}
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		listItems2 = new ListView<String>();
		userList = new ListView<String>();
		this.primaryStage = primaryStage;

		clientConnection = new Client(data->{
			Platform.runLater(()->{
				message = (Message) data;
				if (data instanceof Message) {
					if (message.getMessageType() == Message.Type.USERNAME_AVAILABLE) {
						sceneMap.put("userlist", createUserListScene(primaryStage,message.getSenderUsername()));
						primaryStage.setScene(sceneMap.get("userlist"));

					} else if (message.getMessageType() == Message.Type.USERNAME_TAKEN) {
						showError("Username Taken");
					} else if (message.getMessageType() == Message.Type.USER_LIST_UPDATE){

					} else if (message.getMessageType() == Message.Type.CHAT_MESSAGE){
						String recipientUsername = message.getRecipientUsername();
						String messageContent = message.getContent();
						String senderUsername = message.getSenderUsername();
						displayMessage(recipientUsername, messageContent, senderUsername);

					} else if (message.getMessageType() == Message.Type.SENT_TO_ALL){
						String recipientUsername = message.getRecipientUsername();
						String messageContent = message.getContent();
						String senderUsername = message.getSenderUsername();
						displayToAllMessage(recipientUsername, messageContent, senderUsername);
					}
					else {
						// other messages
					}
				}
				listItems2.getItems().add(data.toString());
			});
		}, this);


		clientConnection.start();

		sceneMap = new HashMap<>();

		allUsernames = new HashSet<>();

		c1 = new TextField();
		b1 = new Button("Send");

		// make login scene and client scene
		Scene loginScene = createLoginScene(primaryStage);
		Scene clientScene = createClientGui(primaryStage);

		sceneMap.put("login", loginScene);
		sceneMap.put("client", clientScene);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		primaryStage.setScene(sceneMap.get("login"));
		primaryStage.setTitle("Client");
		primaryStage.show();
	}

	private void displayToAllMessage(String recipientUsername, String messageContent, String senderUsername) {
		chatAllList.add(senderUsername + ": " + messageContent);
	}

	private void displayMessage(String recipientUsername, String messageContent, String senderUsername) {
		chatMessageList.add(recipientUsername+ ": " + messageContent);
	}

	public Scene createLoginScene(Stage primaryStage) {
		VBox loginLayout = createLoginGui(primaryStage);
		return new Scene(loginLayout, 400, 300);
	}

	public VBox createLoginGui(Stage primaryStage) {
		// LOGIN label at the top
		Label loginLabel = new Label("LOGIN");
		// username
		TextField usernameField = new TextField();
		usernameField.setPromptText("Enter Your Username");
		usernameField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #CCCCCC; -fx-border-width: 2px; -fx-padding: 5px;");
		usernameField.setOnMousePressed(e -> usernameField.setPromptText(""));

		Label errorLabel = new Label();

		Button loginButton = new Button("Login");
		loginButton.setStyle("-fx-background-radius: 20; -fx-font-size: 16px;");
		loginButton.setOnAction(e -> {
			String username = usernameField.getText().trim();
			if (!username.isEmpty()) {
				Message setUsernameMessage = new Message(Message.Type.SET_USERNAME, username);
				clientConnection.send(setUsernameMessage); // SEND TO SERVER
			} else {
				showError("Please enter a username");
			}
		});

		// layout for the login page
		VBox loginLayout = new VBox(20, loginLabel, usernameField, loginButton, errorLabel);
		loginLayout.setAlignment(Pos.CENTER);
		loginLayout.setStyle("-fx-background-color: lightblue;");

		return loginLayout;
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	public Scene createUserListScene(Stage primaryStage, String myUsername) {
		userContainer.setAlignment(Pos.CENTER_LEFT);
		userContainer.setPadding(new Insets(10));

		TextField searchField = new TextField();
		searchField.setPromptText("Search...");
		searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #CCCCCC; -fx-border-width: 2px; -fx-padding: 5px;");

		// text all users
		Button textAllButton = new Button("Text All Users");
		textAllButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px;");
		textAllButton.setOnAction(e -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to text all users?", ButtonType.YES, ButtonType.NO);
			alert.showAndWait();

			if (alert.getResult() == ButtonType.YES) {
				// chat page
				primaryStage.setScene(createChatWithAllUsersScene(primaryStage, userList.getItems(),myUsername));
			}
		});

		// Listener for search field
		VBox finalUserContainer = userContainer;
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			String searchText = newValue.toLowerCase();

			// Clear previous user entries
			finalUserContainer.getChildren().clear();

			// Filter and add user buttons matching the search text
			for (String user : userList.getItems()) {
				if (user.toLowerCase().contains(searchText)) {
					Button userButton = createUserButton(user, primaryStage, false,myUsername);
					finalUserContainer.getChildren().add(userButton);
				}
			}
		});

		VBox layout = new VBox(10, searchField, textAllButton, userContainer);
		layout.setAlignment(Pos.CENTER);
		return new Scene(layout, 400, 300);
	}

	private Button createUserButton(String userName, Stage primaryStage, boolean isOnline, String myUsername) {
		Button userButton = new Button(userName);
		userButton.setStyle("-fx-background-color: #D3D3D3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 20;");

		// Add a green circle next to the user if they are online
		Circle onlineIndicator = new Circle(5);
		onlineIndicator.setFill(isOnline ? Color.GREEN : Color.TRANSPARENT);

		// Create a HBox to hold the user button and online indicator
		HBox userBox = new HBox(userButton, onlineIndicator);
		userBox.setAlignment(Pos.CENTER_LEFT);
		userBox.setSpacing(10);

		userButton.setOnAction(e -> {
			primaryStage.setScene(createChatSceneWithUser(primaryStage, userName, myUsername));
		});

		return userButton;
	}


	private Scene createChatWithAllUsersScene(Stage primaryStage, ObservableList<String> allUsers, String myUsername) {
		Label chatLabel = new Label("Messaging all users");
		TextField inputField = new TextField();

		// ListView to display text messages
		ListView<String> chatMessages = new ListView<>();
		Button sendButton = new Button("Send");

		ObservableList<String> items = chatMessages.getItems();
		items.addAll(chatMessageList);

		sendButton.setOnAction(e -> {
			String chat = inputField.getText();

			chatAllList.add("You: " + chat);
			items.add("You: " + chat);
			// sends message to each user in list
			for (String user : allUsers) {
				sendMessageToUser(user, chat, myUsername);
			}
			inputField.clear();
		});

		// Go back button
		Button goBackButton = new Button("Go Back");
		goBackButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("userlist")));

		VBox chatLayout = new VBox(10, chatMessages, inputField, sendButton, goBackButton);
		chatLayout.setPadding(new Insets(10));

		return new Scene(chatLayout, 400, 300);
	}

	private StackPane createUserPane(String userName) {
		Label userLabel = new Label(userName);
		userLabel.setStyle("-fx-text-fill: white;");
		Rectangle rectangle = new Rectangle(200, 50);

		//  rounded corners
		rectangle.setArcWidth(20);
		rectangle.setArcHeight(20);

		rectangle.setFill(Color.LIGHTGRAY);

		StackPane stackPane = new StackPane(rectangle, userLabel);
		stackPane.setAlignment(Pos.CENTER_LEFT);
		stackPane.setPadding(new Insets(10));
		stackPane.setOnMouseClicked(e -> {

		});
		return stackPane;
	}

	public Scene createChatSceneWithUser(Stage primaryStage, String userName, String myUsername) {
		Label userLabel = new Label("Chat with " + userName);
		ListView<String> chatMessages = new ListView<>();
		TextField inputField = new TextField();
		Button sendButton = new Button("Send");

		ObservableList<String> items = chatMessages.getItems();
		items.addAll(chatMessageList);

		sendButton.setOnAction(e -> {
			String chat = inputField.getText();
			chatMessageList.add("You: " + chat);
			items.add("You: " + chat);
			chatMessageRaw.add(chat);
			inputField.clear();

			// send to server:
			message.setSenderUsername(myUsername);
			Message message = new Message(Message.Type.SEND_MESSAGE, userName, chat, myUsername);
			clientConnection.send(message);
		});

		// Go back button
		Button goBackButton = new Button("Go Back");
		goBackButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("userlist")));

		VBox chatLayout = new VBox(10, userLabel, chatMessages, inputField, sendButton, goBackButton);
		chatLayout.setPadding(new Insets(10));

		return new Scene(chatLayout, 400, 300);
	}

	public Scene createClientGui(Stage primaryStage) {
		clientBox = new VBox(10, c1, b1, listItems2);
		clientBox.setStyle("-fx-background-color: blue;" + "-fx-font-family: 'serif';");
		return new Scene(clientBox, 400, 300);
	}

	private void sendMessageToUser(String user, String chat, String myUsername) {
		message.setSenderUsername(myUsername);
		Message message = new Message(Message.Type.SEND_TO_ALL, user, chat, myUsername);
		clientConnection.send(message);
	}
}
