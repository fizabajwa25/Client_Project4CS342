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
	private Rectangle selectedSquare;
	private boolean isPlayersTurn = true;
	private int myScore = 0;

	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		StackPane root = new StackPane();
		listItems2 = new ListView<>();
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				listItems2.getItems().add(data.toString());
				message = (Message) data;
				if (message.getType() == Message.MessageType.GET_BOARD) { //player vs ai
					boardState = message.getBoardState();
					primaryStage.setScene(createGamePage(primaryStage,boardState,opponentBoardState));
				} else if (message.getType() == Message.MessageType.GET_OPPONENT_BOARD) { // pvp
					opponentBoardState = message.getBoardState();
				} else if (message.getType() == Message.MessageType.GET_BOARD_PLAYER_VS_PLAYER){
					boardState = message.getBoardState();
					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				} else if (message.getType() == Message.MessageType.MISS || message.getType() == Message.MessageType.HIT){
					handleShotResult(message.getType());
					if (myScore == 17){
						displayAlert("You won!");
						primaryStage.setScene(sceneMap.get("Welcome"));
					}
				} else if (message.getType() == Message.MessageType.NOT_YOUR_TURN){
					displayAlert("Opponents turn...");
					isPlayersTurn = true;
                    return;
				}

				listItems2.getItems().add(data.toString());
			});
		});

		clientConnection.start();

		this.primaryStage = primaryStage;
		sceneMap = new HashMap<String, Scene>();

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
		});

		playWithHumanButton.setOnAction(event -> {
			primaryStage.setScene(sceneMap.get("Set Human"));
		});

		VBox buttonsVBox = new VBox(20, rulesButton, playWithAIButton, playWithHumanButton);
		buttonsVBox.setAlignment(Pos.CENTER);

		StackPane titlePane = new StackPane(title);
		titlePane.setAlignment(Pos.CENTER);

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(50));
		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");
		borderPane.setCenter(titlePane);
		borderPane.setBottom(buttonsVBox);

        return new Scene(borderPane, 800, 550);
	}
	private void initializeScoreboard() {
		player1HitsLabel = new Label("Player 1 Hits: ");
		player1HitsLabel.setLayoutX(10);
		player1HitsLabel.setLayoutY(10);

		player2HitsLabel = new Label("Player 2 Hits: ");
		player2HitsLabel.setLayoutX(10);
		player2HitsLabel.setLayoutY(30);
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
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Set Ships");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		title.setFill(Color.WHITE);

		GridPane gridPane = createGridPane();
		boardState = addRandomShips();

		Button startButton = createButtonInGame("Start", "#76b6c4");
		startButton.setOnAction(e -> {
			// on start, send board to server, have server send board back before switching scene
			sendBoardStateToServer();
			int[][] boardState = this.boardState;
			int[][] opponentBoardState = this.opponentBoardState;
			primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
		});

		Button regenerateButton = createButtonInGame("Regenerate", "#76b6c4");
		regenerateButton.setOnAction(e -> {
			addRandomShips();
		});

		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		VBox buttonsVBox = new VBox(10, regenerateButton, startButton, backButton);
		buttonsVBox.setAlignment(Pos.CENTER);

		borderPane.setTop(title);
		borderPane.setAlignment(title, Pos.CENTER);
		borderPane.setCenter(gridPane);
		BorderPane.setAlignment(gridPane, Pos.CENTER_LEFT);

		borderPane.setRight(buttonsVBox);
		BorderPane.setAlignment(buttonsVBox, Pos.CENTER);

		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

        return new Scene(borderPane, 800, 550);
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
			Color color = getRandomColor();
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
					opponentBoardState[startY][startX + i] = 1;
				} else {
					opponentBoardState[startY + i][startX] = 1;
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
					rectangle.setFill(Color.LIGHTBLUE); // set them all to light blue
				} else {
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

	private void checkWinCondition() {
		if (player1Hits >= 17) {
			displayWinMessage("Player 1");
		} else if (player2Hits >= 17) {
			displayWinMessage("Player 2");
		}
	}


	private Scene createGamePage(Stage primaryStage, int[][] boardState, int[][] opponentBoardState) {
		initializeScoreboard();
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
		printBoats(yourGridPane, boardState);

		this.boardState = boardState;
		printBoats(yourGridPane, boardState);

		GridPane opponentGridPane = createOpponentGridPane();
		this.opponentBoardState = generateRandomOpponentBoard(boardState);
		printBoats(opponentGridPane, opponentBoardState);

		VBox yourGridVBox = new VBox(10, yourGridTitle, yourGridPane);
		yourGridVBox.setAlignment(Pos.CENTER);

		VBox opponentGridVBox = new VBox(10, opponentGridTitle, opponentGridPane);
		opponentGridVBox.setAlignment(Pos.CENTER);

		VBox scoreboardVBox = new VBox(10, player1HitsLabel, player2HitsLabel);
		scoreboardVBox.setAlignment(Pos.CENTER);

		borderPane.setTop(scoreboardVBox);

		borderPane.setLeft(yourGridVBox);
		borderPane.setRight(opponentGridVBox);

		Button backButton = createButtonInGame("Back", "#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		borderPane.setBottom(backButton);
		BorderPane.setAlignment(backButton, Pos.CENTER);
		// Event handler for player's grid
		yourGridPane.setOnMouseClicked(event -> {
			double mouseX = event.getX();
			double mouseY = event.getY();
			double cellWidth = yourGridPane.getWidth() / GRID_SIZE;
			double cellHeight = yourGridPane.getHeight() / GRID_SIZE;
			int clickedRow = (int) (mouseY / cellHeight);
			int clickedCol = (int) (mouseX / cellWidth);
			playerTurn(clickedRow, clickedCol);
		});

		// Event handler for opponent's grid
		opponentGridPane.setOnMouseClicked(event -> {
			if (!isPlayer1Turn) return;
			double mouseX = event.getX();
			double mouseY = event.getY();
			double cellWidth = opponentGridPane.getWidth() / GRID_SIZE;
			double cellHeight = opponentGridPane.getHeight() / GRID_SIZE;
			int clickedRow = (int) (mouseY / cellHeight);
			int clickedCol = (int) (mouseX / cellWidth);
			handleOpponentGridClick(clickedRow, clickedCol);
		});
		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");
        return new Scene(borderPane, 800, 550);
	}


	private GridPane createOpponentGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

		// Initialize opponent's ship placements
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);
			}
		}

		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if (opponentBoardState[row][col] == 1) {
					Rectangle rectangle = gridRectangles[row][col];
					rectangle.setFill(Color.LIGHTBLUE); // cover with blue
				}
			}
		}
		return gridPane;
	}

	// Method to handle the player's turn
	private void playerTurn(int row, int col) {
		boolean isHit = opponentBoardState[row][col] == 1;
		Rectangle targetRectangle = gridRectangles[row][col];
		if (isHit) {
			targetRectangle.setFill(Color.RED);
		} else {
			targetRectangle.setFill(Color.GRAY);
		}

		if (isHit) {
			opponentBoardState[row][col] = 1;
		} else {
			opponentBoardState[row][col] = -1;
		}

		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		checkWinCondition();
	}

	// Method to handle the AI opponent's turn
	private void aiOpponentTurn() {
		Random random = new Random();
		int row, col;

		do {
			// Generate random row and column indices
			row = random.nextInt(GRID_SIZE);
			col = random.nextInt(GRID_SIZE);
		} while (alreadyHit(row, col)); // Keep generating until a spot that hasn't been hit is found

		boolean isHit = boardState[row][col] == 1;

		Rectangle targetRectangle = gridRectangles[row][col];
		if (isHit) {
			targetRectangle.setFill(Color.RED);
		} else {
			targetRectangle.setFill(Color.GRAY);
		}

		// Process AI opponent's move
		if (isHit) {
			boardState[row][col] = 1;
		} else {
			boardState[row][col] = -1;
		}

		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		checkWinCondition();
	}

	private void handleOpponentGridClick(int row, int col) {
		if (!isPlayer1Turn) {
			displayAlert("Wait for your turn!");
			return;
		}

		if (alreadyHit(row, col)) {
			displayAlert("Already hit spot!");
			return;
		}

		boolean isHit = opponentBoardState[row][col] == 1;

		Rectangle targetRectangle = gridRectangles[row][col];
		targetRectangle.setFill(isHit ? Color.RED : Color.GRAY);

		if (isHit) {
			opponentBoardState[row][col] = 1; // Mark as hit
		} else {
			opponentBoardState[row][col] = -1; // Mark as missed
		}

		if (isHit) {
			hitsGrid[row][col] = true;
		} else {
			missesGrid[row][col] = true;
		}

		// Update scoreboard
		if (isHit) {
			player1Hits++;
			if (player1Hits == 17) {
				displayAlert("Player 1 wins!");
			}
		}
		switchTurns();
		aiOpponentTurn();
	}

	private boolean alreadyHit(int row, int col) {
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
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Rules of Battleship");
		title.setFont(Font.font("Arial", FontWeight.BOLD,40));
		title.setFill(Color.WHITE);

		Text rulesText = new Text("1. Each player places their ships on their own grid.\n"
				+ "2. Ships cannot overlap with each other.\n"
				+ "3. Once all ships are placed, players take turns guessing coordinates to attack.\n"
				+ "4. If a guess hits a ship, it's a hit. Otherwise, it's a miss.\n"
				+ "5. The first player to sink all of the opponent's ships wins!");
		rulesText.setFont(Font.font("Arial", 16));
		rulesText.setFill(Color.WHITE);

		Button backButton = createButton("Back","#76b6c4");
		backButton.setOnAction(e -> primaryStage.setScene(sceneMap.get("Welcome")));

		VBox vbox = new VBox(20, title, rulesText, backButton);
		vbox.setAlignment(Pos.CENTER);

		borderPane.setCenter(vbox);
		borderPane.setStyle("-fx-background-color: linear-gradient(to bottom, #003366, #000033);");

        return new Scene(borderPane, 800, 550);
	}


	// player vs player:
	private Scene SetBoatsPageHuman(Stage primaryStage) {
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

        return new Scene(borderPane, 800, 550);
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
		clientConnection.send(gameState);
	}

	private GridPane createOpponentGridPaneHuman() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

		// Initialize opponent's ship placements
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);
				int finalRow = row;
				int finalCol = col;
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
		return gridPane;
	}

	private Scene createGamePageHuman(Stage primaryStage, int[][] boardState, int[][] opponentBoardState) {
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
		printBoats(yourGridPane, boardState);

		GridPane opponentGridPane = createOpponentGridPaneHuman();
		this.opponentBoardState = opponentBoardState;

		printOppBoats(opponentGridPane, opponentBoardState);

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

        return new Scene(borderPane, 800, 550);
	}

	private void addGridClickHandlers(GridPane gridPane) {
		for (Node node : gridPane.getChildren()) {
			if (node instanceof Rectangle) {
				Rectangle square = (Rectangle) node;
				int row = GridPane.getRowIndex(square);
				int col = GridPane.getColumnIndex(square);
				square.setOnMouseClicked(event -> {
					if (isPlayersTurn) {
						handleGridSquareClick(row, col);
					}
				});
			}
		}
	}

	private void handleGridSquareClick(int row, int col) {
			Rectangle square = gridRectangles[row][col];
			selectedSquare = square;
			sendCoordinateToServer(row, col);
	}

	private void sendCoordinateToServer(int row, int col) {
		Message coordinateMessage = new Message(Message.MessageType.PLAYER_TURN, row,col);
		clientConnection.send(coordinateMessage);
	}
	private boolean alreadyHitHuman(int[][] hitsGrid, int row, int col) {
		return hitsGrid[row][col] != 0;
	}
	private void handleShotResult(Message.MessageType messageType) {
		if (messageType == Message.MessageType.HIT) {
			selectedSquare.setFill(Color.RED);
			if (isPlayersTurn){
				myScore++;
			}
		} else if (messageType == Message.MessageType.MISS) {
			selectedSquare.setFill(Color.GREY);
		}
	}
}
