import javafx.application.Platform;

import java.awt.*;
import java.io.*;
import java.net.*;

import java.util.function.Consumer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import javafx.scene.shape.Rectangle;

public class Client extends Thread{


	Socket socketClient;

	ObjectOutputStream out;
	ObjectInputStream in;

	private Rectangle[][] gridRectangles;


	private Consumer<Serializable> callback;

	Client(Consumer<Serializable> call){

		callback = call;
	}

	public void run() {

		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}

		while(true) {

			try {
				String message = in.readObject().toString();
				callback.accept(message);
			}
			catch(Exception e) {}
		}

	}

	public void send(String data) {

		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleHitOrMiss(Serializable data) {
		// Parse the message from the server
		if (data instanceof String) {
			String message = (String) data;
			// Example message format: "HIT:3,4" or "MISS:5,6"
			String[] parts = message.split(":");
			if (parts.length == 2) {
				String[] coordinates = parts[1].split(",");
				if (coordinates.length == 2) {
					int row = Integer.parseInt(coordinates[0]);
					int col = Integer.parseInt(coordinates[1]);
					if (parts[0].equals("HIT")) {
						// Update the client's grid to show a hit at the specified coordinates
						Platform.runLater(() -> updateGridCell(row, col, Color.RED));
					} else if (parts[0].equals("MISS")) {
						// Update the client's grid to show a miss at the specified coordinates
						Platform.runLater(() -> updateGridCell(row, col, Color.GRAY));
					}
				}
			}
		}
	}

	// Helper method to update a grid cell's color


	private void updateGridCell(int row, int col, Color color) {
		Rectangle cell = gridRectangles[row][col];
		Paint paint = Color.valueOf(color.toString());
		cell.setFill(paint);
	}



}