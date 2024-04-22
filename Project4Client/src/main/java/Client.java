import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

/*-------------------------------------------
Program 3: Messaging App
Course: CS 342, Spring 2024, UIC
System: IntelliJ
Author: Aleena Mehmood, Fiza Bajwa
------------------------------------------- */

public class Client extends Thread{
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	Message message = new Message(Message.Type.NEW_USER);
	private Consumer<Serializable> callback;
	private GuiClient gui;

	Client(Consumer<Serializable> call, GuiClient gui){

		callback = call;
		this.gui = gui;
	}

	public void run() {
		try {
			socketClient = new Socket("127.0.0.1", 5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
//
		while (true) {
			message = (Message) in.readObject(); // READ FROM SERVER
			callback.accept(message);

			Platform.runLater(()->{
				if (message.getMessageType() == Message.Type.USER_LIST_UPDATE) {
					ArrayList<String> userList = message.getUserList();
					message.setSenderUsername(message.getSenderUsername());
					gui.updateList(userList);
				}
			});
			System.out.println("message sent from server:" + message.getMessageType());
				}
		}
		catch(Exception e) {}
	}
	public void send(Message message) {
		try {
			out.writeObject(message);
			out.flush();
			out.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}