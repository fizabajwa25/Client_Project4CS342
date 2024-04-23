import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

	private final int GRID_SIZE = 10;
	private Rectangle[][] gridRectangles;


	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		listItems2 = new ListView<>();
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				listItems2.getItems().add(data.toString());
			});
		});

		clientConnection.start();

//		gameBoard = new int[10][10]; // Initialize the game board
		this.primaryStage = primaryStage;
		sceneMap = new HashMap<String, Scene>();

//		client = new Client("localhost", 5555);

		Scene welcomeScene = WelcomePage(primaryStage);
//		Scene setBoatsScene = SetBoatsPage(primaryStage);
		Scene rulesScene = RulesPage(primaryStage);
//		Scene clientScene = createClientGuiScene(primaryStage);

		sceneMap.put("Welcome", WelcomePage(primaryStage));
		sceneMap.put("Place", SetBoatsPage(primaryStage));
		sceneMap.put("Rules", RulesPage(primaryStage));

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

	private Scene WelcomePage (Stage primaryStage){
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
			primaryStage.setScene(sceneMap.get("Place"));
		});

		playWithHumanButton.setOnAction(event -> {
			System.out.println("Play with Human");
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
		Scene scene = new Scene(borderPane, 600, 400);

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
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text title = new Text("Set Ships");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		title.setFill(Color.WHITE);

		GridPane gridPane = createGridPane();
		addRandomShips();

		Button startButton = createButtonInGame("Start"," #76b6c4");
		startButton.setOnAction(e -> {
			primaryStage.setScene(createGamePage(primaryStage));
		});

		Button regenerateButton = createButtonInGame("Regenerate", "#76b6c4");
		regenerateButton.setOnAction(e -> {
//			gridPane.getChildren().clear();
//			gridPane = createGridPane();
//			addRandomShips();
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



		Scene scene = new Scene(borderPane, 600, 400);
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


	private void addRandomShips() {
		Random random = new Random();
		// Define the ships
		int[][] ships = {
				{5, random.nextInt(GRID_SIZE - 4)},  // Carrier (5 holes)
				{4, random.nextInt(GRID_SIZE - 3)},  // Battleship (4 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Cruiser (3 holes)
				{3, random.nextInt(GRID_SIZE - 2)},  // Submarine (3 holes)
				{2, random.nextInt(GRID_SIZE - 1)}   // Destroyer (2 holes)
		};

		for (int[] ship : ships) {
			int size = ship[0];
			int startX = ship[1];
			int startY = random.nextInt(GRID_SIZE);

			Color color = getRandomColor(); // Generate a unique random color for each ship

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
	}

	private Color getRandomColor() {
		Random random = new Random();
		return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	private void addShipToGrid(int startX, int startY, int size, Color color) {
		boolean canPlaceShip = true;
		for (int i = 0; i < size; i++) {
			if (startX + i >= GRID_SIZE || gridRectangles[startY][startX + i].getFill() != Color.LIGHTBLUE) {
				// Ship cannot be placed because a cell is already occupied or it exceeds grid boundary
				canPlaceShip = false;
				break;
			}
		}

		if (canPlaceShip) {
			for (int i = 0; i < size; i++) {
				Rectangle rectangle = gridRectangles[startY][startX + i];
				rectangle.setFill(color);
			}
		} else {
			// Retry placing the ship until it finds a valid position
			addRandomShips();
		}
	}

	private Scene createGamePage(Stage primaryStage) {
		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(20));

		Text yourGridTitle = new Text("Your Grid");
		yourGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		yourGridTitle.setFill(Color.WHITE);

		Text opponentGridTitle = new Text("Opponent's Grid");
		opponentGridTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		opponentGridTitle.setFill(Color.WHITE);

		GridPane yourGridPane = createGridPane();
		addRandomShips(); // update this to get actual grid

		GridPane opponentGridPane = createOpponentGridPane(); // Create opponent's grid pane
		addOpponentGridClickHandlers(opponentGridPane); // Add event handlers to opponent's grid

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

		// Add labels to the grid
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				Rectangle rectangle = new Rectangle(30, 30);
				rectangle.setFill(Color.LIGHTBLUE);
				gridRectangles[row][col] = rectangle;
				gridPane.add(rectangle, col, row);
			}
		}
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
		// Assuming you have a method to send the shot coordinates to the opponent
		// For example, clientConnection.sendShot(row, col);

		// For demonstration purposes, let's simulate a hit or a miss randomly
		Random random = new Random();
		boolean isHit = random.nextBoolean();

		// Update the display based on the result of the shot (hit or miss)
		Rectangle targetRectangle = gridRectangles[row][col];
		if (isHit) {
			// Change the color of the rectangle to indicate a hit
			targetRectangle.setFill(Color.RED);
			// You can also update the opponent's grid to show the result of the shot
			// For example: opponentGridPane.getChildren().get(row * GRID_SIZE + col).setFill(Color.RED);
		} else {
			// Change the color of the rectangle to indicate a miss
			targetRectangle.setFill(Color.GRAY);
		}
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

