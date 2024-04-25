import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.io.OutputStream;
import java.net.Socket;

import java.io.IOException;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GuiClient extends Application {

	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	private int[][] myGridState;

	Client clientConnection;

	// Define member variables to store ship placements
	private int[][] playerShipPlacements;
	private int[][] opponentShipPlacements;

	ListView<String> listItems2;
	Stage primaryStage;
	Message message;

	private final int GRID_SIZE = 10;
	private Rectangle[][] gridRectangles;
	// Inside your class where you establish the network connection
	private OutputStream outputStream;

	int[][] boardState = new int[GRID_SIZE][GRID_SIZE];
	int[][] opponentBoardState = new int[GRID_SIZE][GRID_SIZE];

	private boolean[][] hitsGrid = new boolean[GRID_SIZE][GRID_SIZE];
	private boolean[][] missesGrid = new boolean[GRID_SIZE][GRID_SIZE];

	ArrayList<Color> shipColors = new ArrayList<>();



	public static void main(String[] args) {
		launch(args);
	}

	// Method to set the grid state
	private void setGridState(int[][] gridState) {
		this.myGridState = gridState;
	}

	// Method to establish the network connection
//	private void establishConnectionToServer() {
//		try {
//			// Assuming 'socket' is your established socket connection to the server
//			//outputStream = socket.getOutputStream();
//		} catch (IOException e) {
//			e.printStackTrace();
//			// Handle connection errors
//		}
//	}


	@Override
	public void start(Stage primaryStage) {
		listItems2 = new ListView<>();
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				listItems2.getItems().add(data.toString());
				message = (Message) data;
				System.out.println("server sent: " + message.getType());
				if (message.getType() == Message.MessageType.GET_BOARD) { //player vs ai
					boardState = message.getBoardState();
					System.out.println("server sent client board: " + Arrays.deepToString(boardState));
				} else if (message.getType() == Message.MessageType.GET_OPPONENT_BOARD) { // pvp
					opponentBoardState = message.getBoardState();
					System.out.println("opponent board lenngth: "+opponentBoardState.length);
					System.out.println("server sent opponent board: " + Arrays.deepToString(opponentBoardState));
				} else if (message.getType() == Message.MessageType.GET_BOARD_PLAYER_VS_PLAYER){
					boardState = message.getBoardState();
					System.out.println("server sent client board: " + Arrays.deepToString(boardState));
					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				}

				listItems2.getItems().add(data.toString());
			});
		});

		clientConnection.start();

		this.primaryStage = primaryStage;
		sceneMap = new HashMap<String, Scene>();

		setupClient();

		Scene welcomeScene = WelcomePage(primaryStage);
		Scene setBoatsScene = SetBoatsPage(primaryStage);
		Scene rulesScene = RulesPage(primaryStage);

		sceneMap.put("Welcome", WelcomePage(primaryStage));
		sceneMap.put("Set", SetBoatsPage(primaryStage));
		sceneMap.put("Rules", RulesPage(primaryStage));
		sceneMap.put("Set Human", SetBoatsPageHuman(primaryStage));

		primaryStage.setTitle("Welcome to Battleship");
		primaryStage.centerOnScreen();
		primaryStage.setScene(sceneMap.get("Welcome"));
		primaryStage.show();


	}

	private Scene WelcomePage(Stage primaryStage) {
		Text title = new Text("Battleship");
		title.setFont(Font.font("Garamond", FontWeight.BOLD, 90));
		title.setFill(Color.CORNSILK);

		Button rulesButton = createButton("Rules", "#76b6c4");
		Button playWithAIButton = createButton("Play with AI", "#76b6c4");
		Button playWithHumanButton = createButton("Play with Human", "#76b6c4");

		rulesButton.setOnAction(event -> {
			primaryStage.setScene(sceneMap.get("Rules"));
		});

		playWithAIButton.setOnAction(event -> {
			primaryStage.setScene(sceneMap.get("Set"));
//			sendOpponentBoardStateToServer();
//			clientConnection.send(new Message(Message.MessageType.SET_OPPONENT_BOARD));
		});

		playWithHumanButton.setOnAction(event -> {
//			System.out.println("Play with Human");
			primaryStage.setScene(sceneMap.get("Set Human"));
		});

		VBox buttonsVBox = new VBox(20, rulesButton, playWithAIButton, playWithHumanButton);
		buttonsVBox.setAlignment(Pos.CENTER);

		StackPane titlePane = new StackPane(title);
		titlePane.setAlignment(Pos.CENTER);

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(50));
//		borderPane.setBackground(new Background(new BackgroundFill(Color.rgb(52, 73, 94), null, null)));
		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");
		borderPane.setCenter(titlePane);
		borderPane.setBottom(buttonsVBox);

		// Create the scene
		Scene scene = new Scene(borderPane, 800, 500);

		return scene;

	}

	private void setupClient() {
		// Initialize ship placements arrays
		playerShipPlacements = new int[GRID_SIZE][GRID_SIZE];
		opponentShipPlacements = new int[GRID_SIZE][GRID_SIZE];

		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				processServerMessage(data);
			});
		});
		clientConnection.start();
	}


	private void processServerMessage(Object data) {
		message = (Message) data;
		switch (message.getType()) {
			case GET_BOARD:
				boardState = message.getBoardState();
				// Assume createGamePage initializes the Board based on boardState
				primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
				break;
			case GET_OPPONENT_BOARD:
				opponentBoardState = message.getBoardState();
				primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				break;
			case GET_BOARD_PLAYER_VS_PLAYER:
				boardState = message.getBoardState();
				primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				break;
			default:
				System.out.println("Unhandled message type: " + message.getType());
				break;
		}
	}


	private Button createButton(String text, String color) {
		Button button = new Button(text);
		button.setFont(Font.font("Arial Narrow", FontWeight.BOLD, 23));
		button.setTextFill(Color.CORNSILK);
		button.setStyle("-fx-background-color: " + color + "; -fx-padding: 10 20;");
		return button;
	}

	private Button createButtonInGame(String text, String color) {
		Button button = new Button(text);
		button.setFont(Font.font("Arial Narrow", FontWeight.BOLD, 15));
		button.setTextFill(Color.WHITE);
		button.setStyle("-fx-background-color: " + color + "; " +
				"-fx-padding: 10 20; " +
				"-fx-border-color: transparent; " +
				"-fx-background-radius: 5; " +
				"-fx-border-radius: 5; " +
				"-fx-cursor: hand;");
		return button;
	}

	private Scene SetBoatsPage(Stage primaryStage) {
		System.out.println("when do i reach set boats page?");
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Set Ships");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		title.setFill(Color.WHITE);

		GridPane gridPane = createGridPane();
		boardState = addRandomShips();

//		Button startButton = createButtonInGame("Start", "#76b6c4");
//		startButton.setOnAction(e -> {
//			// Store the grid state
//			setGridState(boardState);
//			// Send board state to server
//			sendBoardStateToServer();
//			// Switch scene
//			primaryStage.setScene(createGamePage(primaryStage, playerShipPlacements, opponentShipPlacements));
//		});


		Button startButton = createButtonInGame("Start", " #76b6c4");
		startButton.setOnAction(e -> {
			// Store the grid state
			setGridState(boardState);
			// Send board state to server
			sendBoardStateToServer();
			// Switch scene
			primaryStage.setScene(createGamePage(primaryStage, playerShipPlacements, opponentShipPlacements));
		});


//		System.out.println("printing board: " + gridPane);

		Button regenerateButton = createButtonInGame("Regenerate", "#76b6c4");
		regenerateButton.setOnAction(e -> {
//			gridPane.getChildren().clear();
//			gridPane = createGridPane();
			addRandomShips();
		});

		// Generate random ships
		int[][] ships = addRandomShips();

//		// Mark the cells on the grid with the generated ships
//		for (int[] ship : ships) {
//			int size = ship[0];
//			int startX = ship[1];
//			int startY = ship[2];
//			// Mark the cells starting from startX, startY for the ship size
//			for (int i = 0; i < size; i++) {
//				Rectangle rectangle = gridRectangles[startY][startX + i];
//				rectangle.setFill(Color.BLACK); // Or any other color to represent a ship
//			}
//		}

		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		VBox buttonsVBox = new VBox(10, regenerateButton, startButton, backButton);
		buttonsVBox.setAlignment(Pos.CENTER); // Align to the right center
		borderPane.setRight(buttonsVBox);

		borderPane.setTop(title);
		borderPane.setAlignment(title, Pos.CENTER);
		borderPane.setCenter(gridPane);

		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		Scene scene = new Scene(borderPane, 800, 500);
		return scene;
	}


	private int[][] addRandomShips() {
		Random random = new Random();
		int[][] ships = new int[4][2]; // 4 boats with length and position

		// Generate 4 boats with lengths 2, 3, 4, and 5
		for (int i = 0; i < 4; i++) {
			int length = i + 2; // Lengths: 2, 3, 4, 5
			int position = random.nextInt(GRID_SIZE - length + 1); // Random position
			ships[i][0] = length;
			ships[i][1] = position;
		}

		// Clear existing ship placements
		//clearGridPane(gridPane);

		for (int[] ship : ships) {
			int size = ship[0];
			int startX = ship[1];
			int startY = random.nextInt(GRID_SIZE);

			Color color = getRandomColor(); // Generate a unique random color for each ship
			shipColors.add(color);

			boolean canPlaceShip;
			do {
				canPlaceShip = true;
				for (int i = 0; i < size; i++) {
					if (startX + i >= GRID_SIZE || gridRectangles[startY][startX + i].getFill() != Color.LIGHTBLUE) {
						// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
						canPlaceShip = false;
						startX = random.nextInt(GRID_SIZE - size + 1);
						startY = random.nextInt(GRID_SIZE);
						break;
					}
				}
			} while (!canPlaceShip);

			for (int i = 0; i < size; i++) {
				Rectangle rectangle = gridRectangles[startY][startX + i];
				rectangle.setFill(color);
			}
		}
		return ships;
	}



	private void clearGridPane(GridPane gridPane) {
		// Clear all ship placements (rectangles filled with colors)
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = gridRectangles[row][col];
				rectangle.setFill(Color.LIGHTBLUE); // Reset cell color
			}
		}
	}

	private Color getRandomColor() {
		Random random = new Random();
		return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}


	private void printBoats(GridPane gridPane, int[][] boardState) {
		clearGridPane(gridPane);
		for (int row = 0; row < GRID_SIZE; row++) {
			Color color = shipColors.get(row);
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = gridRectangles[row][col];
				if (boardState[row][col] == 1) {
					rectangle.setFill(color);
				} else {
					// Otherwise, set the rectangle color to indicate an empty cell
					rectangle.setFill(Color.LIGHTBLUE);
				}
			}
		}
	}

	private void printOppBoats(GridPane gridPane, int[][] boardState) {
		clearGridPane(gridPane);
		for (int row = 0; row < GRID_SIZE; row++) {
			Color color = shipColors.get(row);
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = gridRectangles[row][col];
				if (boardState[row][col] == 1) {
					rectangle.setFill(Color.LIGHTBLUE);
				} else {
					// Otherwise, set the rectangle color to indicate an empty cell
					rectangle.setFill(Color.LIGHTBLUE);
				}
			}
		}
	}



	public Scene createGamePage(Stage primaryStage, int[][] playerBoard, int[][] opponentBoard) {
		// Initialize boards with event handling as necessary
		Board player = new Board(false, null); // Player's board doesn't need to handle clicks
		Board enemy = new Board(true, event -> {
			Board.Cell cell = (Board.Cell) event.getSource(); // Cast to Board.Cell
			if (!cell.wasShot) { // Correct method call without parentheses
				boolean hit = cell.shoot(); // Correct method call without parentheses
				System.out.println(hit ? "Hit" : "Miss");
				// Possibly send shot info to server here
				// Send shot information to the server
			//	sendShotInfoToServer(cell.x, cell.y);
			}
		});
		// Reset the grid to the initial state
		for (int i = 0; i < GRID_SIZE; i++) {
			playerBoard[i] = Arrays.copyOf(myGridState[i], myGridState[i].length);
		}
		//printBoats(playerGrid, playerBoard);
		// Setup boards based on the provided state
		// Setup boards based on the provided state
		GridPane playerGrid = createGridPane();
		GridPane opponentGrid = createOpponentGridPane();

		// Reset the grid to the initial state
		for (int i = 0; i < GRID_SIZE; i++) {
			playerBoard[i] = Arrays.copyOf(myGridState[i], myGridState[i].length);
		}

//		setupBoard(enemy, opponentBoard);

		// Titles for the boards
		Text yourGridTitle = new Text("Your Grid");
		yourGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		yourGridTitle.setFill(Color.WHITE);
		Text opponentGridTitle = new Text("Opponent's Grid");
		opponentGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		opponentGridTitle.setFill(Color.WHITE);

		// Layout setup
		VBox yourGridVBox = new VBox(10, yourGridTitle, player);
		yourGridVBox.setAlignment(Pos.CENTER);
		VBox opponentGridVBox = new VBox(10, opponentGridTitle, enemy);
		opponentGridVBox.setAlignment(Pos.CENTER);

		// BorderPane for overall layout
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));
		borderPane.setLeft(yourGridVBox);
		borderPane.setRight(opponentGridVBox);

		// Back button to go back to the welcome scene
		Button backButton = new Button("Back");
		backButton.setStyle("-fx-background-color: #76b6c4;");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));
		borderPane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);

		// Styling
		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		// Update the UI to reflect stored ship placements
		printBoats(playerGrid, playerShipPlacements);
		printOppBoats(opponentGrid, opponentShipPlacements);

		// Creating the scene
		Scene scene = new Scene(borderPane, 800, 600); // Increased height for better layout
		return scene;
	}


	// personal grid
	private GridPane createGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

		gridRectangles = new Rectangle[GRID_SIZE][GRID_SIZE];

		// Add labels to the grid
		for (int row = 0; row < GRID_SIZE; row++) {
			Text rowLabel = new Text(String.valueOf(row + 1));
			rowLabel.setFill(Color.WHITE);
			gridPane.add(rowLabel, 0, row + 1);
			for (int col = 0; col < GRID_SIZE; col++) {
				if (row == 0) {
					Text colLabel = new Text(String.valueOf((char) ('A' + col)));
					colLabel.setFill(Color.WHITE);
//					colLabel.setWrappingWidth(new Insets(0, 5, 0, 0));
					gridPane.add(colLabel, col + 1, 0);
				}
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col + 1, row + 1);
			}
		}
		return gridPane;
	}



	private GridPane createOpponentGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

		// Initialize opponent's ship placements
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
//				opponentBoardState[row][col] = 0; // Ensuring all cells are initially set to no ship
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);

				int finalRow = row;
				int finalCol = col;
				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
			}
		}

		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if (opponentBoardState[row][col] == 1) {
					// If there is a ship at this location, cover it with blue
					Rectangle rectangle = gridRectangles[row][col];
					rectangle.setFill(Color.LIGHTBLUE);
				}
			}
		}

//		// Place ships randomly
//		addRandomShips(opponentBoardState);

		return gridPane;
	}

	private void handleOpponentGridClick(int row, int col) {
		// Check if the spot has already been hit
		if (alreadyHit(row, col)) {
			displayAlert("Already hit spot!");
			return;
		}

		// Check if it's a hit or a miss
		boolean isHit = opponentBoardState[row][col] == 1;

		// Mark the spot as hit or miss
		Rectangle targetRectangle = gridRectangles[row][col];
		targetRectangle.setFill(isHit ? Color.RED : Color.GRAY);

		// Process the player's move
		if (opponentBoardState[row][col] == 1) {
			// Hit a ship
			gridRectangles[row][col].setFill(Color.RED);
			opponentBoardState[row][col] = 1; // Mark as hit
			// Add logic to keep track of ships if needed
		} else {
			// Missed
			gridRectangles[row][col].setFill(Color.GRAY);
			opponentBoardState[row][col] = -1; // Mark as missed
		}

		// Add the clicked coordinates to the corresponding list
		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}
	}

	private boolean alreadyHit(int row, int col) {
		// Check if the clicked coordinates are in the hitsGrid or missesGrid
		return hitsGrid[row][col] || missesGrid[row][col];
	}

	private void displayAlert(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}


	private void sendBoardStateToServer() {
		int[][] boardState = new int[GRID_SIZE][GRID_SIZE];
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Color cellColor = (Color) gridRectangles[row][col].getFill();
				if (cellColor.equals(Color.LIGHTBLUE)) {
					// Cell is empty
					boardState[row][col] = 0;
				} else {
					// Cell contains a ship
					boardState[row][col] = 1;
				}
			}
		}
		Message gameState = new Message(Message.MessageType.SET_BOARD, boardState);

		// Send the GameState object to the server
		clientConnection.send(gameState);
	}


	// player vs player:
	private Scene SetBoatsPageHuman(Stage primaryStage) {
		System.out.println("when do i reach set boats human page?");
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Set Ships");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		title.setFill(Color.WHITE);

		GridPane gridPane = createGridPane();
		boardState = addRandomShips();

		Button startButton = createButtonInGame("Start", " #76b6c4");
		startButton.setOnAction(e -> {
			sendBoardStateToServerHuman();
		});


		Button regenerateButton = createButtonInGame("Regenerate", "#76b6c4");
		regenerateButton.setOnAction(e -> {
//			gridPane.getChildren().clear();
//			gridPane = createGridPane();
			addRandomShips();
		});

		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		VBox buttonsVBox = new VBox(10, regenerateButton, startButton, backButton);
		buttonsVBox.setAlignment(Pos.CENTER); // Align to the right center
		borderPane.setRight(buttonsVBox);

		borderPane.setTop(title);
		borderPane.setAlignment(title, Pos.CENTER);
		borderPane.setCenter(gridPane);

		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		Scene scene = new Scene(borderPane, 800, 500);
		return scene;
	}

	private void sendBoardStateToServerHuman() {
		int[][] boardState = new int[GRID_SIZE][GRID_SIZE];
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Color cellColor = (Color) gridRectangles[row][col].getFill();
				if (cellColor.equals(Color.LIGHTBLUE)) {
					// Cell is empty
					boardState[row][col] = 0;
				} else {
					// Cell contains a ship
					boardState[row][col] = 1;
				}
			}
		}
		Message gameState = new Message(Message.MessageType.SET_BOARD_PLAYER_VS_PLAYER, boardState);

		// Send the GameState object to the server
		clientConnection.send(gameState);
	}

	private Scene createGamePageHuman(Stage primaryStage, int[][] boardState, int[][] opponentBoardState) {
		System.out.println("when do i reach create game page human?");
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text yourGridTitle = new Text("Your Grid");
		yourGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		yourGridTitle.setFill(Color.WHITE);

		Text opponentGridTitle = new Text("Opponent's Grid");
		opponentGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		opponentGridTitle.setFill(Color.WHITE);

		GridPane yourGridPane = createGridPane();


		this.boardState = boardState;
		System.out.println("what is the board state before add print to screen" + Arrays.deepToString(boardState));
		printBoats(yourGridPane, boardState);
//		addRandomShips();

		GridPane opponentGridPane = createOpponentGridPane();
		this.opponentBoardState = opponentBoardState;
		System.out.println("opponent board: "+ Arrays.deepToString(opponentBoardState));
//		int count = 0;
//		for (int[] row : opponentBoardState) {
//			for (int cell : row) {
//				if (cell != 0) {
//					count++;
//				}
//			}
//		}
//		if (count == 0){
//			displayAlert("Waiting for another opponent...");
//		}

		printOppBoats(opponentGridPane, opponentBoardState);
//		addOpponentGridClickHandlers(opponentGridPane); // Add event handlers to opponent's grid

		VBox yourGridVBox = new VBox(10, yourGridTitle, yourGridPane);
		yourGridVBox.setAlignment(Pos.CENTER);

		VBox opponentGridVBox = new VBox(10, opponentGridTitle, opponentGridPane);
		opponentGridVBox.setAlignment(Pos.CENTER);

		borderPane.setLeft(yourGridVBox);
		borderPane.setRight(opponentGridVBox);

		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		borderPane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);

		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		Scene scene = new Scene(borderPane, 800, 400);
		return scene;
	}


	private Scene RulesPage(Stage primaryStage) {
		// TEMP: copied from chat
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Rules of Battleship");
		title.setFont(Font.font("Arial", 24));

		Text rulesText = new Text("this is temp i just had chat do something random \n\n 1. Each player places their ships on their own grid.\n"
				+ "2. Ships cannot overlap with each other.\n"
				+ "3. Once all ships are placed, players take turns guessing coordinates to attack.\n"
				+ "4. If a guess hits a ship, it's a hit. Otherwise, it's a miss.\n"
				+ "5. The first player to sink all of the opponent's ships wins!");
		rulesText.setFont(Font.font("Arial", 16));

		Button backButton = new Button("Back");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		VBox vbox = new VBox(20, title, rulesText, backButton);
		vbox.setAlignment(Pos.CENTER);

		borderPane.setCenter(vbox);

		Scene scene = new Scene(borderPane, 600, 400);
		return scene;
	}

}

