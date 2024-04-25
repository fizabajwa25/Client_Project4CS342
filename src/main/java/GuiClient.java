import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

	private int player1Hits = 0;
	private int player2Hits = 0;


	// Declare labels for displaying hits count for each player
	public Label player1HitsLabel;
	public Label player2HitsLabel;

	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;

	ListView<String> listItems2;
	private TextArea textArea;
	private TextField inputField;
	private Button sendButton;
	private GridPane gridPane;
	private Ship selectedShip;
	Stage primaryStage;
	Message message;

	private final int GRID_SIZE = 10;
	private Rectangle[][] gridRectangles;
	int[][] boardState = new int[GRID_SIZE][GRID_SIZE];
	int[][] opponentBoardState = new int[GRID_SIZE][GRID_SIZE];

	private boolean[][] hitsGrid = new boolean[GRID_SIZE][GRID_SIZE];
	private boolean[][] missesGrid = new boolean[GRID_SIZE][GRID_SIZE];

	ArrayList<Color> shipColors = new ArrayList<>();
	private boolean isPlayer1Turn = true;
	Button sendMoveButton = createButtonInGame("Send Move", "#76b6c4");
	private Rectangle selectedSquare;
	private boolean isPlayer1TurnHuman= true;
	private boolean playersTurn = true;
	private boolean oppsTurn = false;
	private int myScore = 0;





	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		// Initialize scoreboard
		//initializeScoreboard();

		StackPane root = new StackPane(); // Create a root node
		listItems2 = new ListView<>();
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				listItems2.getItems().add(data.toString());
				message = (Message) data;
				System.out.println("server sent: " + message.getType());
				if (message.getType() == Message.MessageType.GET_BOARD) { //player vs ai
					boardState = message.getBoardState();
					System.out.println("server sent client board: " + Arrays.deepToString(boardState));
					primaryStage.setScene(createGamePage(primaryStage,boardState,opponentBoardState));
				} else if (message.getType() == Message.MessageType.GET_OPPONENT_BOARD) { // pvp
					opponentBoardState = message.getBoardState();
					System.out.println("opponent board lenngth: "+opponentBoardState.length);
					System.out.println("server sent opponent board: " + Arrays.deepToString(opponentBoardState));
				} else if (message.getType() == Message.MessageType.GET_BOARD_PLAYER_VS_PLAYER){
					boardState = message.getBoardState();
					System.out.println("server sent client board: " + Arrays.deepToString(boardState));
					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				} else if (message.getType() == Message.MessageType.MISS || message.getType() == Message.MessageType.HIT){
//					sendMoveButton.setDisable(false);
					handleShotResult(message.getType());
					System.out.println("my score: "+myScore);
					if (myScore == 17){
						displayAlert("You won!");
						primaryStage.setScene(sceneMap.get("Welcome"));
					}

				}

				listItems2.getItems().add(data.toString());
			});
		});

		clientConnection.start();

//		gameBoard = new int[10][10]; // Initialize the game board
		this.primaryStage = primaryStage;
		sceneMap = new HashMap<String, Scene>();

//		client = new Client("localhost", 5555);

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

	// Add this method to initialize the labels
	private void initializeScoreboard() {
		player1HitsLabel = new Label("Player 1 Hits: ");
		player1HitsLabel.setLayoutX(10);
		player1HitsLabel.setLayoutY(10);

		player2HitsLabel = new Label("Player 2 Hits: ");
		player2HitsLabel.setLayoutX(10);
		player2HitsLabel.setLayoutY(30);

		// Add labels to the root of your scene
		//root.getChildren().addAll(player1HitsLabel, player2HitsLabel);
	}

	// Update Player 1 hits count on the scoreboard
	private void updatePlayer1Hits(int hits) {
		player1HitsLabel.setText("Player 1 Hits: " + hits);
	}

	// Update Player 2 hits count on the scoreboard
	private void updatePlayer2Hits(int hits) {
		player2HitsLabel.setText("Player 2 Hits: " + hits);
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

		Button startButton = createButtonInGame("Start", " #76b6c4");
		startButton.setOnAction(e -> {
			// on start, send board to server, have server send board back before switching scene
			sendBoardStateToServer();

			// Get the board state and opponent board state from the SetBoatsPage
			int[][] boardState = this.boardState;
			int[][] opponentBoardState = this.opponentBoardState;

			//  Switch to the game page and pass the board states
			primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
		});


//		System.out.println("printing board: " + gridPane);

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


	private int[][] addRandomShips() {
		Random random = new Random();
		// Define the ships
		int[][] ships = {
				{5, random.nextInt(GRID_SIZE - 4)},  // Carrier (5 holes)
				{4, random.nextInt(GRID_SIZE - 3)},  // Battleship (4 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Cruiser (3 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{2, random.nextInt(GRID_SIZE - 1)}   // Destroyer (2 holes)
		};

		// Clear existing ship placements
		clearGridPane(gridPane);

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

	private int[][] generateRandomOpponentBoard(int[][] playerBoardState) {
		int[][] opponentBoardState = new int[GRID_SIZE][GRID_SIZE];
		Random random = new Random();
		// Define the ships with lengths 2, 3, 4, and 5
		int[][] ships = {
				{2, random.nextInt(GRID_SIZE - 1)},  // Destroyer (2 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{4, random.nextInt(GRID_SIZE - 3)},  // Cruiser (4 holes)
				{5, random.nextInt(GRID_SIZE - 4)}   // Carrier (5 holes)
		};

		// Place ships randomly for the opponent
		for (int[] ship : ships) {
			int size = ship[0];
			int startX = ship[1];
			int startY = random.nextInt(GRID_SIZE);
			boolean horizontal = random.nextBoolean();

			boolean canPlaceShip;
			do {
				canPlaceShip = true;
				for (int i = 0; i < size; i++) {
					if (horizontal) {
						if (startX + i >= GRID_SIZE || opponentBoardState[startY][startX + i] != 0 || playerBoardState[startY][startX + i] != 0) {
							// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
							canPlaceShip = false;
							startX = random.nextInt(GRID_SIZE - size + 1);
							startY = random.nextInt(GRID_SIZE);
							break;
						}
					} else {
						if (startY + i >= GRID_SIZE || opponentBoardState[startY + i][startX] != 0 || playerBoardState[startY + i][startX] != 0) {
							// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
							canPlaceShip = false;
							startX = random.nextInt(GRID_SIZE);
							startY = random.nextInt(GRID_SIZE - size + 1);
							break;
						}
					}
				}
			} while (!canPlaceShip);

			for (int i = 0; i < size; i++) {
				if (horizontal) {
					opponentBoardState[startY][startX + i] = 1;  // Place ship part
				} else {
					opponentBoardState[startY + i][startX] = 1;  // Place ship part
				}
			}
		}
		return opponentBoardState;
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

	// Display win message
	private void displayWinMessage(String winner) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Game Over");
		alert.setHeaderText(null);
		alert.setContentText(winner + " wins!");
		alert.showAndWait();
	}

	// Check if a player has won
	private void checkWinCondition() {
		if (player1Hits >= 17) {
			// Player 1 wins
			displayWinMessage("Player 1");
		} else if (player2Hits >= 17) {
			// Player 2 wins
			displayWinMessage("Player 2");
		}
	}

	// Call this method whenever a player hits a ship
	private void playerHitShip(int playerNumber) {
		if (playerNumber == 1) {
			player1Hits++;
			updatePlayer1Hits(player1Hits);
		} else if (playerNumber == 2) {
			player2Hits++;
			updatePlayer2Hits(player2Hits);
		}
		checkWinCondition();
	}

	private Scene createGamePage(Stage primaryStage, int[][] boardState, int[][] opponentBoardState) {
		initializeScoreboard();
		System.out.println("when do i reach create game page?");
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text yourGridTitle = new Text("Your Grid");
		yourGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		yourGridTitle.setFill(Color.WHITE);

		Text opponentGridTitle = new Text("Opponent's Grid");
		opponentGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		opponentGridTitle.setFill(Color.WHITE);

		GridPane yourGridPane = createGridPane();

		this.boardState = boardState;//
		System.out.println("what is the board state before add print to screen" + Arrays.deepToString(boardState));
		printBoats(yourGridPane, boardState);
//		addRandomShips();

		// Print player's ship placements
		this.boardState = boardState;
		printBoats(yourGridPane, boardState);

		GridPane opponentGridPane = createOpponentGridPane(); // Create opponent's grid pane
		// Generate and print opponent's ship placements
		this.opponentBoardState = generateRandomOpponentBoard(boardState); // Assuming boardState is the player's ship placement
		printBoats(opponentGridPane, opponentBoardState);
//		addOpponentGridClickHandlers(opponentGridPane); // Add event handlers to opponent's grid

		VBox yourGridVBox = new VBox(10, yourGridTitle, yourGridPane);
		yourGridVBox.setAlignment(Pos.CENTER);

		VBox opponentGridVBox = new VBox(10, opponentGridTitle, opponentGridPane);
		opponentGridVBox.setAlignment(Pos.CENTER);

		// Add the scoreboard to the layout
		VBox scoreboardVBox = new VBox(10, player1HitsLabel, player2HitsLabel);
		scoreboardVBox.setAlignment(Pos.CENTER);

		borderPane.setTop(scoreboardVBox);

		borderPane.setLeft(yourGridVBox);
		borderPane.setRight(opponentGridVBox);


		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		borderPane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);
		opponentGridPane.setOnMouseClicked(event -> {
			// Calculate the row and column based on the mouse event
			double mouseX = event.getX();
			double mouseY = event.getY();

			// Calculate the cell width and height
			double cellWidth = opponentGridPane.getWidth() / GRID_SIZE; // Assuming GRID_SIZE is the number of rows/columns in the grid
			double cellHeight = opponentGridPane.getHeight() / GRID_SIZE;

			// Calculate the clicked row and column
			int clickedRow = (int) (mouseY / cellHeight);
			int clickedCol = (int) (mouseX / cellWidth);

			// Now you have the clicked row and column

			// Process the player's turn
			playerTurn(clickedRow, clickedCol);

			// After the player's turn, let the AI opponent take its turn
			aiOpponentTurn();
		});


		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		Scene scene = new Scene(borderPane, 800, 400);
		return scene;
	}


	private GridPane createOpponentGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

//		if (!isPlayer1Turn) {
//			// Disable grid clicks if it's not Player 1's turn
//			for (Node node : gridPane.getChildren()) {
//				node.setDisable(true);
//				displayAlert("Waiting for opponent");
//			}
//		}

		// Initialize opponent's ship placements
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
//				opponentBoardState[row][col] = 0; // Ensuring all cells are initially set to no ship
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);

//				int finalRow = row;
//				int finalCol = col;
//				 rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
//				rectangle.setOnMouseClicked(event -> addGridClickHandlers(gridPane));
			//	gridPane.setOnMouseClicked(event -> handleOpponentGridClick);

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

	// Method to handle the player's turn
	private void playerTurn(int row, int col) {
		// Process the player's move
		// For example:
		// Determine if the player's shot hits or misses
		boolean isHit = opponentBoardState[row][col] == 1;

		// Update opponent's grid based on the result
		Rectangle targetRectangle = gridRectangles[row][col];
		if (isHit) {
			// If it's a hit, mark the spot red on the opponent's grid
			targetRectangle.setFill(Color.RED);
		} else {
			// If it's a miss, mark the spot grey on the opponent's grid
			targetRectangle.setFill(Color.GRAY);
		}

		// Process the opponent's move
		if (isHit) {
			// Player hit the AI opponent's ship
			opponentBoardState[row][col] = 1; // Mark as hit
		} else {
			// Player missed
			opponentBoardState[row][col] = -1; // Mark as missed
		}

		// Add the clicked coordinates to the corresponding list
		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		// Check if the game is over (e.g., if the player has won)
		checkWinCondition();

		// After the player's turn, it's the AI opponent's turn
		aiOpponentTurn();
	}

	// Method to handle the AI opponent's turn
	private void aiOpponentTurn() {
		Random random = new Random();
		int row, col;

		do {
			// Generate random row and column indices
			row = random.nextInt(GRID_SIZE);
			col = random.nextInt(GRID_SIZE);
		} while (alreadyHit(row, col)); // Keep generating new indices until a spot that hasn't been hit is found

		// Determine if the shot hits or misses
		boolean isHit = boardState[row][col] == 1;

		// Update your board based on the result
		Rectangle targetRectangle = gridRectangles[row][col];
		if (isHit) {
			// If it's a hit, mark the spot red on your board
			targetRectangle.setFill(Color.RED);
		} else {
			// If it's a miss, mark the spot grey on your board
			targetRectangle.setFill(Color.GRAY);
		}

		// Process the AI opponent's move
		if (isHit) {
			// AI opponent hit a ship
			boardState[row][col] = 1; // Mark as hit
		} else {
			// AI opponent missed
			boardState[row][col] = -1; // Mark as missed
		}

		// Add the clicked coordinates to the corresponding list
		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		// Check if the game is over (e.g., if the AI opponent has won)
		checkWinCondition();
	}



	private void handleOpponentGridClick(int row, int col) {
		if (!isPlayer1Turn) {
			// It's not this player's turn, display message and return
			displayAlert("Wait for your turn!");
			return;
		}

		// Check if the spot has already been hit
		if (alreadyHit(row, col)) {
			displayAlert("Already hit spot!");
			return;
		}

		// Determine if it's a hit or a miss
		boolean isHit = opponentBoardState[row][col] == 1;

		// Mark the spot on the UI
		Rectangle targetRectangle = gridRectangles[row][col];
		targetRectangle.setFill(isHit ? Color.RED : Color.GRAY);

		//Process the player's move
		if (isHit) {
			// Hit a ship
			opponentBoardState[row][col] = 1; // Mark as hit
		} else {
			// Missed
			opponentBoardState[row][col] = -1; // Mark as missed
		}

		//Add the clicked coordinates to the corresponding list
		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		// Update the scoreboard
		if (isHit) {
			// Increment hit count for Player 1
			player1Hits++;
			// Check if Player 1 has reached 17 hits
			if (player1Hits == 17) {
				displayAlert("Player 1 wins!");
			}
		}

		// After processing the move, switch turns
		switchTurns();
	}


//		boolean isHit = opponentBoardState[row][col] == 1;
//		// Mark the spot as hit or miss
//		Rectangle targetRectangle = gridRectangles[row][col];
//		targetRectangle.setFill(isHit ? Color.RED : Color.GRAY);
//
//		if (isPlayer1Turn) {
//			// Check if the spot has already been hit
//			if (alreadyHit(row, col)) {
//				displayAlert("Already hit spot!");
//				return;
//			}
//
//			// Check if it's a hit or a miss
//			boolean hit = opponentBoardState[row][col] == 1;
//
//			// Process the player's move
//			if (hit) {
//				// Hit a ship
//				opponentBoardState[row][col] = 1; // Mark as hit
//				// Add logic to keep track of ships if needed
//			} else {
//				// Missed
//				opponentBoardState[row][col] = -1; // Mark as missed
//			}
//
//			// Add the clicked coordinates to the corresponding list
//			if (hit) {
//				hitsGrid[row][col] = true;
//			} else {
//				missesGrid[row][col] = true;
//			}
//
//			// Update the scoreboard
//			if (hit) {
//				// Increment hit count for Player 1
//				player1Hits++;
//				// Check if Player 1 has reached 17 hits
//				if (player1Hits == 17) {
//					displayAlert("Player 1 wins!");
//					// Optionally, reset the game or take other actions
//				}
//			}
//
//			// After processing the move, switch turns
//			switchTurns(); // Implement switchTurns method to switch turns
//
//			// Optionally, send a message to the client indicating player's turn
//			// clientConnection.send(new Message(Message.MessageType.PLAYER_TURN));
//		} else {
//			// It's not this player's turn, handle accordingly (e.g., display message)
//			displayAlert("Wait for your turn!");
//		}



////		if (isPlayer1Turn) {
//			// Check if the spot has already been hit
//			if (alreadyHit(row, col)) {
//				displayAlert("Already hit spot!");
//				return;
//			}
//
//			// Check if it's a hit or a miss
//			boolean isHit = opponentBoardState[row][col] == 1;
//
//			// Mark the spot as hit or miss
//			Rectangle targetRectangle = gridRectangles[row][col];
//			targetRectangle.setFill(isHit ? Color.RED : Color.GRAY);
//
//			// Process the player's move
//			if (opponentBoardState[row][col] == 1) {
//				// Hit a ship
//				gridRectangles[row][col].setFill(Color.RED);
//				opponentBoardState[row][col] = 1; // Mark as hit
//				// Add logic to keep track of ships if needed
//			} else {
//				// Missed
//				gridRectangles[row][col].setFill(Color.GRAY);
//				opponentBoardState[row][col] = -1; // Mark as missed
//			}
//
//			// Add the clicked coordinates to the corresponding list
//			if (isHit) {
//				hitsGrid[row][col] = true;
//			} else {
//				missesGrid[row][col] = true;
//			}
////			switchTurns();
////		clientConnection.send(new Message(Message.MessageType.PLAYER_TURN));
////		}


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

	private void switchTurns() {

		isPlayer1Turn = !isPlayer1Turn; // Toggle between true and false
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

	private GridPane createOpponentGridPaneHuman() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

//		if (!isPlayer1Turn) {
//			// Disable grid clicks if it's not Player 1's turn
//			for (Node node : gridPane.getChildren()) {
//				node.setDisable(true);
//				displayAlert("Waiting for opponent");
//			}
//		}

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
//				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
				rectangle.setOnMouseClicked(event -> addGridClickHandlers(gridPane));
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

		GridPane opponentGridPane = createOpponentGridPaneHuman();
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
//		Button sendMoveButton = createButtonInGame("Send Move", "#76b6c4");
//		sendMoveButton.setOnAction(event -> handleSendMove());

		HBox buttonBox = new HBox(backButton,sendMoveButton);
		borderPane.setTop(buttonBox);
		BorderPane.setAlignment(buttonBox, Pos.CENTER);



		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

		Scene scene = new Scene(borderPane, 800, 400);
		return scene;
	}
//	private void handleSendMove() {
//		// Disable the button to prevent multiple clicks
//		sendMoveButton.setDisable(true);
////
////		// Code to get the selected coordinate
////		int row = ...; // Get the selected row
////		int col = ...; // Get the selected column
//
//		// Send the player's move to the server
//		clientConnection.send(new Message(Message.MessageType.PLAYER_TURN));
//
//		// Wait for the server response (OPPONENT_MOVE)
//		// Handle the server response in the client's message handler
//
//		// After receiving the server response, re-enable the button
//		// This can be done in the message handler when receiving the OPPONENT_MOVE message
//	}

	private void addGridClickHandlers(GridPane gridPane) {
		if (playersTurn == true) {

			for (Node node : gridPane.getChildren()) {
				if (node instanceof Rectangle) {
					Rectangle square = (Rectangle) node;
					int row = GridPane.getRowIndex(square);
					int col = GridPane.getColumnIndex(square);

					square.setOnMouseClicked(event -> handleGridSquareClick(row, col));

				}
			}
		} else {
			displayAlert("Waiting for opponents turn...");
		}
	}

	private void handleGridSquareClick(int row, int col) {
//		// Clear previous selection if exists
//		if (selectedSquare != null) {
//			selectedSquare.setFill(Color.LIGHTBLUE);
//		}
		if (alreadyHit(row, col)) {
			displayAlert("Already hit spot!");
			return;
		}
		// Highlight the selected square
		Rectangle square = gridRectangles[row][col];
		square.setFill(Color.YELLOW);
		selectedSquare = square;

		// Send the selected coordinate to the server
		sendCoordinateToServer(row, col);
		displayAlert("wait for your turn!");
	}

	private void sendCoordinateToServer(int row, int col) {
		sendMoveButton.setDisable(true);
		playersTurn = false;
		oppsTurn = true;
		// Create a message containing the coordinate information
		Message coordinateMessage = new Message(Message.MessageType.PLAYER_TURN, row,col, playersTurn, oppsTurn);

		// Send the message to the server
		clientConnection.send(coordinateMessage);
	}
	private void handleShotResult(Message.MessageType messageType) {
		if (messageType == Message.MessageType.HIT) {
			// Mark the square as red (hit)
			selectedSquare.setFill(Color.RED);
			myScore++;
		} else if (messageType == Message.MessageType.MISS) {
			// Mark the square as grey (miss)
			selectedSquare.setFill(Color.GREY);
		}

		// Enable the send button
		sendMoveButton.setDisable(false);
	}


}


///	private void addRandomShips(int[][] boardState) {
////		// Your existing logic to randomly generate opponent's ship placements goes here
////	}


//	private void addOpponentGridClickHandlers(GridPane opponentGridPane) {
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle rectangle = gridRectangles[row][col];
//				int finalRow = row;
//				int finalCol = col;
//				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
//			}
//		}
//	}

//	private void handleOpponentGridClick(int row, int col) {
//		Random random = new Random();
//		boolean isHit = random.nextBoolean();
//
//		Rectangle targetRectangle = gridRectangles[row][col];
//		if (boardState[row][col] == 1) { // Check if there is a ship in the clicked position
//			if (isHit) {
//				targetRectangle.setFill(Color.RED); // If hit, change color to red
//			} else {
//				targetRectangle.setFill(Color.GRAY); // If miss, change color to gray
//			}
//		} else {
//			targetRectangle.setFill(Color.GRAY); // If no ship, change color to gray (indicating a miss)
//		}
//	}
//
//	public void placeShipsForAI() {
//		int[] shipSizes = {5, 4, 3, 2};  // Sizes of the ships to place
//		Random random = new Random();
//
//		for (int size : shipSizes) {
//			boolean placed = false;
//			while (!placed) {
//				int x = random.nextInt(GRID_SIZE);
//				int y = random.nextInt(GRID_SIZE);
//				boolean horizontal = random.nextBoolean();  // Randomly placing the ship horizontally or vertically
//
//				if (canPlaceShip(x, y, size, horizontal)) {
//					for (int i = 0; i < size; i++) {
//						if (horizontal) {
//							opponentBoardState[x][y + i] = 1;  // Place ship part
//						} else {
//							opponentBoardState[x + i][y] = 1;  // Place ship part
//						}
//					}
//					placed = true;
//				}
//			}
//		}
//	}
//
//	private boolean canPlaceShip(int x, int y, int size, boolean horizontal) {
//		if (horizontal) {
//			if (y + size > GRID_SIZE) return false;  // Check if ship goes out of bounds
//			for (int i = 0; i < size; i++) {
//				if (opponentBoardState[x][y + i] != 0) return false;  // Check if the spot is already taken
//			}
//		} else {
//			if (x + size > GRID_SIZE) return false;  // Check if ship goes out of bounds
//			for (int i = 0; i < size; i++) {
//				if (opponentBoardState[x + i][y] != 0) return false;  // Check if the spot is already taken
//			}
//		}
//		return true;
//	}
//
//
//	private void switchTurns() {
//		isPlayer1Turn = !isPlayer1Turn;
//	}

//	private void sendOpponentBoardStateToServer() {
//		int[][] boardState = new int[GRID_SIZE][GRID_SIZE];
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Color cellColor = (Color) gridRectangles[row][col].getFill();
//				if (cellColor.equals(Color.LIGHTBLUE)) {
//					// Cell is empty
//					boardState[row][col] = 0;
//				} else {
//					// Cell contains a ship
//					boardState[row][col] = 1;
//				}
//			}
//		}
//		Message gameState = new Message(Message.MessageType.SET_OPPONENT_BOARD, boardState);
//
//		// Send the GameState object to the server
//		clientConnection.send(gameState);
//	}



//	private void addOpponentGridClickHandlers(GridPane opponentGridPane) {
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle rectangle = gridRectangles[row][col];
//				int finalRow = row;
//				int finalCol = col;
//				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
//			}
//		}
//	}
//	private void addGridClickHandlers(GridPane gridPane) {
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle square = gridRectangles[row][col];
//				int finalRow = row;
//				int finalCol = col;
//				square.setOnMouseClicked(event -> handleGridSquareClick(finalRow, finalCol));
//			}
//		}
//	}
//
//	private void handleGridSquareClick(Rectangle square) {
//		// Clear previous selection if exists
//		if (selectedSquare != null) {
//			selectedSquare.setFill(Color.LIGHTBLUE);
//		}
//		// Highlight the selected square
//		square.setFill(Color.YELLOW);
//		selectedSquare = square;
//	}


//	private int[][] generateRandomPlayerBoard() {
//		int[][] playerBoardState = new int[GRID_SIZE][GRID_SIZE];
//		Random random = new Random();
//
//		// Define the ships
//		int[][] ships = {
//				{5, random.nextInt(GRID_SIZE - 4)},  // Carrier (5 holes)
//				{4, random.nextInt(GRID_SIZE - 3)},  // Battleship (4 holes)
//				{3, random.nextInt(GRID_SIZE - 2)},  // Cruiser (3 holes)
//				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
//				{2, random.nextInt(GRID_SIZE - 1)}   // Destroyer (2 holes)
//		};
//
//		// Place ships randomly
//		for (int[] ship : ships) {
//			int size = ship[0];
//			int startX = ship[1];
//			int startY = random.nextInt(GRID_SIZE);
//			boolean horizontal = random.nextBoolean();
//
//			boolean canPlaceShip;
//			do {
//				canPlaceShip = true;
//				for (int i = 0; i < size; i++) {
//					if (horizontal) {
//						if (startX + i >= GRID_SIZE || playerBoardState[startY][startX + i] != 0) {
//							// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
//							canPlaceShip = false;
//							startX = random.nextInt(GRID_SIZE - size + 1);
//							startY = random.nextInt(GRID_SIZE);
//							break;
//						}
//					} else {
//						if (startY + i >= GRID_SIZE || playerBoardState[startY + i][startX] != 0) {
//							// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
//							canPlaceShip = false;
//							startX = random.nextInt(GRID_SIZE);
//							startY = random.nextInt(GRID_SIZE - size + 1);
//							break;
//						}
//					}
//				}
//			} while (!canPlaceShip);
//
//			for (int i = 0; i < size; i++) {
//				if (horizontal) {
//					playerBoardState[startY][startX + i] = 1;  // Place ship part
//				} else {
//					playerBoardState[startY + i][startX] = 1;  // Place ship part
//				}
//			}
//		}
//
//		return playerBoardState;
//	}
//

//	public void start(Stage primaryStage) {
//		listItems2 = new ListView<>();
//		clientConnection = new Client(data -> {
//			Platform.runLater(() -> {
//				listItems2.getItems().add(data.toString());
//				message = (Message) data;
//				System.out.println("server sent: " + message.getType());
//				if (message.getType() == Message.MessageType.GET_BOARD) {
//					boardState = message.getBoardState();
//					System.out.println("server sent: " + Arrays.deepToString(message.getBoardState()));
//					primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
////					primaryStage.setScene(sceneMap.get("userlist"));
//				} else if (message.getType() == Message.MessageType.GET_OPPONENT_BOARD) {
//					opponentBoardState = message.getBoardState();
//					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
//				} else if (message.getType() == Message.MessageType.GET_BOARD_PLAYER_VS_PLAYER){
//					boardState = message.getBoardState();
//					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
//				}
//			});
//		});