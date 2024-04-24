import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
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



	public static void main(String[] args) {
		launch(args);
	}

	//	public GuiClient() {
//		clientConnection = new Client(data -> {
//			Platform.runLater(() -> {
//				listItems2.getItems().add(data.toString());
//				message = (Message) data;
//				if (message.getType() == Message.MessageType.GET_BOARD) {
//					boardState = message.getBoardState();
//					System.out.println("server sent: " + Arrays.deepToString(message.getBoardState()));
//					primaryStage.setScene(createGamePage(primaryStage));
//				}
//			});
//		});
//	}
	@Override
	public void start(Stage primaryStage) {
		listItems2 = new ListView<>();
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				listItems2.getItems().add(data.toString());
				message = (Message) data;
				System.out.println("server sent: " + message.getType());
				if (message.getType() == Message.MessageType.GET_BOARD) {
					boardState = message.getBoardState();
					System.out.println("server sent: " + Arrays.deepToString(message.getBoardState()));
					primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
//					primaryStage.setScene(sceneMap.get("userlist"));
				} else if (message.getType() == Message.MessageType.GET_OPPONENT_BOARD) {
//					opponentBoardState = message.getBoardState();
					primaryStage.setScene(createGamePage(primaryStage, boardState, opponentBoardState));
				} else if (message.getType() == Message.MessageType.GET_BOARD_PLAYER_VS_PLAYER){
					boardState = message.getBoardState();
					primaryStage.setScene(createGamePageHuman(primaryStage, boardState, opponentBoardState));
				}
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
//		Scene clientScene = createClientGuiScene(primaryStage);

		sceneMap.put("Welcome", WelcomePage(primaryStage));
		sceneMap.put("Set", SetBoatsPage(primaryStage));
		sceneMap.put("Rules", RulesPage(primaryStage));
		sceneMap.put("Set Human", SetBoatsPageHuman(primaryStage));

		primaryStage.setTitle("Welcome to Battleship");
		primaryStage.centerOnScreen();
		primaryStage.setScene(sceneMap.get("Welcome"));
		primaryStage.show();


//	public Scene createClientGui() {
//
//		clientBox = new VBox(10, c1,b1,listItems2);
//		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");
//		return new Scene(clientBox, 400, 300);
//
//	}
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


	private int[][] generateRandomPlayerBoard() {
		int[][] playerBoardState = new int[GRID_SIZE][GRID_SIZE];
		Random random = new Random();

		// Define the ships
		int[][] ships = {
				{5, random.nextInt(GRID_SIZE - 4)},  // Carrier (5 holes)
				{4, random.nextInt(GRID_SIZE - 3)},  // Battleship (4 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Cruiser (3 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{2, random.nextInt(GRID_SIZE - 1)}   // Destroyer (2 holes)
		};

		// Place ships randomly
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
						if (startX + i >= GRID_SIZE || playerBoardState[startY][startX + i] != 0) {
							// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
							canPlaceShip = false;
							startX = random.nextInt(GRID_SIZE - size + 1);
							startY = random.nextInt(GRID_SIZE);
							break;
						}
					} else {
						if (startY + i >= GRID_SIZE || playerBoardState[startY + i][startX] != 0) {
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
					playerBoardState[startY][startX + i] = 1;  // Place ship part
				} else {
					playerBoardState[startY + i][startX] = 1;  // Place ship part
				}
			}
		}

		return playerBoardState;
	}

	private int[][] generateRandomOpponentBoard(int[][] playerBoardState) {
		int[][] opponentBoardState = new int[GRID_SIZE][GRID_SIZE];
		Random random = new Random();

		// Define the ships
		int[][] ships = {
				{5, random.nextInt(GRID_SIZE - 4)},  // Carrier (5 holes)
				{4, random.nextInt(GRID_SIZE - 3)},  // Battleship (4 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Cruiser (3 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{2, random.nextInt(GRID_SIZE - 1)}   // Destroyer (2 holes)
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

		// Create a hidden version of the opponent's board
		int[][] hiddenOpponentBoard = new int[GRID_SIZE][GRID_SIZE];
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				// Hide ship placements by setting all cells to 0
				hiddenOpponentBoard[i][j] = 0;
			}
		}

		return hiddenOpponentBoard;
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

	private Scene createGamePage(Stage primaryStage, int[][] boardState, int[][] opponentBoardState) {
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

//		Rectangle rectangle = new Rectangle(30, 30);
//		rectangle.setFill(Color.LIGHTBLUE);
////		gridRectangles = rectangle;
//		int col = Arrays.stream(boardState).findFirst();
//		gridPane.add(rectangle, col, row);
//		yourGridPane.add();

		this.boardState = boardState;//
		System.out.println("what is the board state before add print to screen" + Arrays.deepToString(boardState));
		printBoats(yourGridPane, boardState);
//		addRandomShips();

		GridPane opponentGridPane = createOpponentGridPane(); // Create opponent's grid pane
		this.opponentBoardState = opponentBoardState;
		printBoats(opponentGridPane, opponentBoardState);
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


	private GridPane createOpponentGridPane() {
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(2);
		gridPane.setVgap(2);

		// Initialize opponent's ship placements
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				opponentBoardState[row][col] = 0; // Ensuring all cells are initially set to no ship
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);

				int finalRow = row;
				int finalCol = col;
				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
			}
		}

//		// Place ships randomly
//		addRandomShips(opponentBoardState);

		return gridPane;
	}


	private void addOpponentGridClickHandlers(GridPane opponentGridPane) {
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = gridRectangles[row][col];
				int finalRow = row;
				int finalCol = col;
				rectangle.setOnMouseClicked(event -> handleOpponentGridClick(finalRow, finalCol));
			}
		}
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
		if (boardState[row][col] == 1) {
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


//	private void addRandomShips(int[][] boardState) {
//		// Your existing logic to randomly generate opponent's ship placements goes here
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

	private void sendOpponentBoardStateToServer() {
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
		Message gameState = new Message(Message.MessageType.SET_OPPONENT_BOARD, boardState);

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

//		Rectangle rectangle = new Rectangle(30, 30);
//		rectangle.setFill(Color.LIGHTBLUE);
////		gridRectangles = rectangle;
//		int col = Arrays.stream(boardState).findFirst();
//		gridPane.add(rectangle, col, row);
//		yourGridPane.add();

		this.boardState = boardState;
		System.out.println("what is the board state before add print to screen" + Arrays.deepToString(boardState));
		printBoats(yourGridPane, boardState);
//		addRandomShips();

		GridPane opponentGridPane = createOpponentGridPane(); // Create opponent's grid pane
		this.opponentBoardState = opponentBoardState;
		printBoats(opponentGridPane, opponentBoardState);
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

}