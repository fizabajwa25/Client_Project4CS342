import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class GuiClient extends Application {
	private Client client;
	private TextArea textArea;
	private TextField inputField;
	private Button sendButton;
	private GridPane gridPane;

	@Override
	public void start(Stage primaryStage) {
		// Initialize the client connection
		client = new Client("localhost", 12345); // Example: Connect to localhost on port 12345

		primaryStage.setTitle("Battleship Game");

		// Text area for game messages
		textArea = new TextArea();
		textArea.setEditable(false);

		// User input field
		inputField = new TextField();

		// Send button
		sendButton = new Button("Send");
		sendButton.setOnAction(e -> sendInput());

		// Grid for the battleship game
		gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				Button button = new Button();
				button.setPrefSize(30, 30);
				final int row = i, col = j;
				button.setOnAction(event -> handleCellAction(row, col));
				gridPane.add(button, j, i);
			}
		}

		// Layout setup
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(gridPane);
		borderPane.setTop(textArea);

		// Bottom panel for user input
		HBox inputPanel = new HBox();
		inputPanel.setSpacing(10);  // Space between components
		inputPanel.setAlignment(Pos.CENTER);
		inputPanel.getChildren().addAll(inputField, sendButton);
		borderPane.setBottom(inputPanel);

		Scene scene = new Scene(borderPane, 600, 700);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleCellAction(int row, int col) {
		// Send the clicked cell position as a message
		client.sendMessage(new Message("guess", row + "," + col));
		System.out.println("Cell [" + row + ", " + col + "] clicked");
		// Update GUI or disable button as necessary
	}

	private void sendInput() {
		String text = inputField.getText();
		if (!text.isEmpty()) {
			client.sendMessage(new Message("chat", text));
			inputField.setText("");
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
