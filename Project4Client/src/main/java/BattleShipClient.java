import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class BattleshipClient extends Application {

	TextField messageInput;
	Button sendButton;
	ListView<String> messageList;
	Label gameStatusLabel;

	Client clientConnection;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				messageList.getItems().add(data.toString());
			});
		});

		clientConnection.start();

		messageInput = new TextField();
		sendButton = new Button("Send");
		sendButton.setOnAction(e -> {
			clientConnection.send(messageInput.getText());
			messageInput.clear();
		});

		messageList = new ListView<>();
		gameStatusLabel = new Label("Game Status: Waiting for opponent");

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(10));

		// Top: Game status
		borderPane.setTop(gameStatusLabel);

		// Center: Message list
		borderPane.setCenter(messageList);

		// Bottom: Message input and send button
		HBox bottomBox = new HBox(10);
		bottomBox.getChildren().addAll(messageInput, sendButton);
		bottomBox.setPadding(new Insets(10, 0, 0, 0));
		borderPane.setBottom(bottomBox);

		// Set scene
		Scene scene = new Scene(borderPane, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Battleship Client");
		primaryStage.show();

		// Close window event
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});
	}
}
