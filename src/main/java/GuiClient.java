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

		GridPane opponentGridPane = createGridPane(); //  need to make a new method to get an actual grid

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



//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.event.EventHandler;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.ListView;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.ClipboardContent;
//import javafx.scene.input.Dragboard;
//import javafx.scene.input.TransferMode;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextAlignment;
//import javafx.stage.Stage;
//import javafx.stage.WindowEvent;
//
//import java.util.HashMap;
//
//public class GuiClient extends Application{
//
//	TextField c1;
//	Button b1;
//	HashMap<String, Scene> sceneMap;
//	VBox clientBox;
//	Client clientConnection;
//
//	ListView<String> listItems2;
//
//	private final int GRID_SIZE = 10;
//	private Color[][] gridColors;
//	private Rectangle[][] gridRectangles;
//
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//
//	@Override
//	public void start(Stage primaryStage) throws Exception {
//		gridColors = new Color[GRID_SIZE][GRID_SIZE]; // Initialize grid colors
//		gridRectangles = new Rectangle[GRID_SIZE][GRID_SIZE]; // Initialize grid rectangles
//
//		// Create the grid for the game board
//		GridPane gridPane = createGridPane();
//
//		// Add ships to the grid
//		addShipsToGrid();
//
//		// Create the sidebar to display ship options
//		//	VBox sidebar = createSidebar();
//
//		// Add the grid and sidebar to a border pane
//		BorderPane borderPane = new BorderPane();
//		borderPane.setCenter(gridPane);
//		//borderPane.setRight(sidebar);
//
//		// Set the background color of the grid
//		gridPane.setStyle("-fx-background-color: black;");
//
//		// Create the scene and set it on the stage
//		Scene scene = new Scene(borderPane, 600, 400);
//		primaryStage.setScene(scene);
//		primaryStage.setTitle("Battleship Game");
//		primaryStage.show();
//	}
//
//	private GridPane createGridPane() {
//		GridPane gridPane = new GridPane();
//		gridPane.setPadding(new Insets(10));
//		gridPane.setHgap(2);
//		gridPane.setVgap(2);
//
//		// Populate the grid with rectangles
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle rectangle = new Rectangle(30, 30, Color.LIGHTBLUE);
//				gridRectangles[row][col] = rectangle;
//				gridPane.add(rectangle, col, row);
//			}
//		}
//
//		// Add mouse event handlers for ship placement
//		gridPane.setOnMousePressed(event -> {
//			if (event.getButton() == MouseButton.PRIMARY) {
//				startX = (int) (event.getX() / 30);
//				startY = (int) (event.getY() / 30);
//				selectedShip = getShipAt(startX, startY);
//			}
//		});
//
//		gridPane.setOnMouseDragged(event -> {
//			if (selectedShip != null && event.getButton() == MouseButton.PRIMARY) {
//				int mouseX = (int) (event.getX() / 30);
//				int mouseY = (int) (event.getY() / 30);
//				if (isValidPlacement(mouseX, mouseY, selectedShip.getSize())) {
//					clearShip(selectedShip);
//					drawShip(mouseX, mouseY, selectedShip);
//				}
//			}
//		});
//
//		return gridPane;
//	}
//
//
//
//	private void addShipsToGrid() {
//		// Add ships of different sizes and colors to the grid
//		addShipToGrid(0, 0, 2, Color.PURPLE);
//		addShipToGrid(2, 0, 3, Color.ORANGE);
//		addShipToGrid(4, 0, 4, Color.BLUE);
//		addShipToGrid(6, 0, 5, Color.GREEN);
//	}
//
//	private void addShipToGrid(int startX, int startY, int size, Color color) {
//		// Add a ship to the grid at the specified position with the specified size and color
//		Ship ship = new Ship(size, color);
//		drawShip(startX, startY, ship);
//	}
//
//	private Ship getShipAt (int startX, int startY, int size, Color color) {
//		// Add a ship to the grid at the specified position with the specified size and color
//		for (int i = 0; i < size; i++) {
//			if (startX + i < GRID_SIZE) {
//				gridColors[startY][startX + i] = color;
//				gridRectangles[startY][startX + i].setFill(color);
//			}
//		}
//	}
//
//
//
//
//	public Scene createClientGui() {
//
//		clientBox = new VBox(10, c1,b1,listItems2);
//		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");
//		return new Scene(clientBox, 400, 300);
//
//	}
//
//	private String toHexString(Color color) {
//		return String.format("#%02X%02X%02X",
//				(int) (color.getRed() * 255),
//				(int) (color.getGreen() * 255),
//				(int) (color.getBlue() * 255));
//	}
//
//
//
//
//}
//


//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.input.MouseButton;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.stage.Stage;
//
//import java.util.Random;

//public class GuiClient extends Application {
//
//	private final int GRID_SIZE = 10;
//	private Color[][] gridColors;
//	private Rectangle[][] gridRectangles;
//	private Rectangle selectedShip;
//	private int startX, startY;
//	private int offsetX, offsetY;
//	private double initialX, initialY;
//
//	@Override
//	public void start(Stage primaryStage) {
//		gridColors = new Color[GRID_SIZE][GRID_SIZE]; // Initialize grid colors
//		gridRectangles = new Rectangle[GRID_SIZE][GRID_SIZE]; // Initialize grid rectangles
//
//		// Create the grid for the game board
//		GridPane gridPane = createGridPane();
//
//		// Add ships to the grid
//		addRandomShips();
//
//		// Add the grid to a border pane
//		BorderPane borderPane = new BorderPane();
//		borderPane.setCenter(gridPane);
//
//		// Set the background color of the grid
//		gridPane.setStyle("-fx-background-color: black;");
//
//		// Create the scene and set it on the stage
//		Scene scene = new Scene(borderPane, 600, 400);
//		primaryStage.setScene(scene);
//		primaryStage.setTitle("Battleship Game");
//		primaryStage.show();
//	}
//
//	private GridPane createGridPane() {
//		GridPane gridPane = new GridPane();
//		gridPane.setPadding(new Insets(10));
//		gridPane.setHgap(2);
//		gridPane.setVgap(2);
//
//		// Populate the grid with rectangles
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle rectangle = new Rectangle(30, 30, Color.LIGHTBLUE);
//				gridRectangles[row][col] = rectangle;
//				gridPane.add(rectangle, col, row);
//			}
//		}
//
//		// Add mouse event handlers for ship movement and rotation
//		gridPane.setOnMousePressed(event -> {
//			if (event.getButton() == MouseButton.PRIMARY) {
//				initialX = event.getSceneX();
//				initialY = event.getSceneY();
//				startX = (int) (event.getX() / 30);
//				startY = (int) (event.getY() / 30);
//				selectedShip = getShipAt(startX, startY);
//				if (selectedShip != null) {
//					offsetX = (int) (event.getX() - selectedShip.getX());
//					offsetY = (int) (event.getY() - selectedShip.getY());
//				}
//			}
//		});
//
//		gridPane.setOnMouseDragged(event -> {
//			if (selectedShip != null && event.getButton() == MouseButton.PRIMARY) {
//				double newX = event.getSceneX() - initialX - offsetX;
//				double newY = event.getSceneY() - initialY - offsetY;
//				selectedShip.setX(newX);
//				selectedShip.setY(newY);
//			}
//		});
//
//		return gridPane;
//	}
//
//	private void addRandomShips() {
//		Random random = new Random();
//		for (int i = 0; i < 4; i++) {
//			int startX = random.nextInt(GRID_SIZE - 1);
//			int startY = random.nextInt(GRID_SIZE - 1);
//			Color color = getRandomColor();
//			int size = i + 2; // Sizes from 2 to 5
//			addShipToGrid(startX, startY, size, color);
//		}
//	}
//
//	private Color getRandomColor() {
//		Random random = new Random();
//		return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
//	}
//
//	private void addShipToGrid(int startX, int startY, int size, Color color) {
//		Rectangle ship = new Rectangle(size * 30, 30, color);
//		drawShip(startX, startY, ship);
//	}
//
//	private Rectangle getShipAt(int startX, int startY) {
//		for (int i = 0; i < gridRectangles.length; i++) {
//			for (int j = 0; j < gridRectangles[i].length; j++) {
//				Rectangle rectangle = gridRectangles[i][j];
//				if (rectangle.getBoundsInParent().contains(startX * 30, startY * 30)) {
//					return rectangle;
//				}
//			}
//		}
//		return null;
//	}
//
//	private void drawShip(int startX, int startY, Rectangle ship) {
//		for (int i = 0; i < ship.getWidth() / 30; i++) {
//			if (startX + i < GRID_SIZE) {
//				gridColors[startY][startX + i] = (Color) ship.getFill();
//				gridRectangles[startY][startX + i].setFill(ship.getFill());
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}
//
//
//
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
//import javafx.scene.input.MouseButton;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
//import javafx.stage.Stage;
//
//import java.util.Random;

//public class GuiClient extends Application {
//
//	private final int GRID_SIZE = 10;
//	private Color[][] gridColors;
//	private Rectangle[][] gridRectangles;
//	private Rectangle selectedShip;
//	private int startX, startY;
//	private double offsetX, offsetY;
//	private double initialX, initialY;
//
//	@Override
//	public void start(Stage primaryStage) {
//		gridColors = new Color[GRID_SIZE][GRID_SIZE]; // Initialize grid colors
//		gridRectangles = new Rectangle[GRID_SIZE][GRID_SIZE]; // Initialize grid rectangles
//
//		// Create the grid for the game board
//		GridPane gridPane = createGridPane();
//
//		// Add ships to the grid
//		addRandomShips();
//
//		// Add the grid to a border pane
//		BorderPane borderPane = new BorderPane();
//		borderPane.setCenter(gridPane);
//
//		// Set the background color of the grid
//		gridPane.setStyle("-fx-background-color: black;");
//
//		// Create the scene and set it on the stage
//		Scene scene = new Scene(borderPane, 600, 400);
//		primaryStage.setScene(scene);
//		primaryStage.setTitle("Battleship Game");
//		primaryStage.show();
//	}
//
//	private GridPane createGridPane() {
//		GridPane gridPane = new GridPane();
//		gridPane.setPadding(new Insets(10));
//		gridPane.setHgap(2);
//		gridPane.setVgap(2);
//
//		// Populate the grid with rectangles
//		for (int row = 0; row < GRID_SIZE; row++) {
//			for (int col = 0; col < GRID_SIZE; col++) {
//				Rectangle rectangle = new Rectangle(30, 30, Color.LIGHTBLUE);
//				gridRectangles[row][col] = rectangle;
//				gridPane.add(rectangle, col, row);
//			}
//		}
//
//		gridPane.setOnMousePressed(event -> {
//			if (event.getButton() == MouseButton.PRIMARY) {
//				startX = (int) (event.getX() / 30);
//				startY = (int) (event.getY() / 30);
//				selectedShip = getShipAt(startX, startY);
//				if (selectedShip != null) {
//					offsetX = event.getX() - selectedShip.getX();
//					offsetY = event.getY() - selectedShip.getY();
//				}
//			}
//		});
//
//		gridPane.setOnMouseDragged(event -> {
//			if (selectedShip != null && event.getButton() == MouseButton.PRIMARY) {
//				double newX = event.getX() - offsetX;
//				double newY = event.getY() - offsetY;
//				selectedShip.setX(newX);
//				selectedShip.setY(newY);
//			}
//		});
//
//		// Add mouse event handlers for ship movement
////		gridPane.setOnMousePressed(event -> {
////			if (event.getButton() == MouseButton.PRIMARY) {
////				startX = (int) (event.getX() / 30);
////				startY = (int) (event.getY() / 30);
////				selectedShip = getShipAt(startX, startY);
////				if (selectedShip != null) {
////					offsetX = event.getX() - (selectedShip.getX() + startX * 30);
////					offsetY = event.getY() - (selectedShip.getY() + startY * 30);
////				}
////			}
////		});
////
////		gridPane.setOnMouseDragged(event -> {
////			if (selectedShip != null && event.getButton() == MouseButton.PRIMARY) {
////				double newX = event.getX() - offsetX;
////				double newY = event.getY() - offsetY;
////				selectedShip.setX(newX);
////				selectedShip.setY(newY);
////			}
////		});
//
//		return gridPane;
//	}
//
//	private void addRandomShips() {
//		Random random = new Random();
//		for (int i = 0; i < 5; i++) {
//			int startX = random.nextInt(GRID_SIZE - 1);
//			int startY = random.nextInt(GRID_SIZE - 1);
//			Color color = getRandomColor();
//			int size = i + 2; // Sizes from 2 to 5
//			addShipToGrid(startX, startY, size, color);
//
//		}
//	}
//
//	private Color getRandomColor() {
//		Random random = new Random();
//		return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
//	}
//
//	private void addShipToGrid(int startX, int startY, int size, Color color) {
//		Rectangle ship = new Rectangle(size * 30, 30, color);
//		drawShip(startX, startY, size, color);
//		gridRectangles[startY][startX].setOnMouseClicked(event -> {
//			if (event.getButton() == MouseButton.PRIMARY) {
//				selectedShip = gridRectangles[startY][startX];
//				offsetX = event.getX();
//				offsetY = event.getY();
//			}
//		});
//	}
//
//	private Rectangle getShipAt(int startX, int startY) {
//		return gridRectangles[startY][startX];
//	}
//
//	private void drawShip(int startX, int startY, int size, Color color) {
//		int endX = startX + size - 1; // Calculate the ending X coordinate of the ship
//		int endY = startY + 1; // Since ships are only one row high, ending Y coordinate is the same as starting Y
//
//		for (int row = startY; row < endY; row++) {
//			for (int col = startX; col <= endX; col++) {
//				if (col < GRID_SIZE) {
//					gridColors[row][col] = color;
//					gridRectangles[row][col].setFill(color);
//				}
//			}
//		}
//	}
//
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}


