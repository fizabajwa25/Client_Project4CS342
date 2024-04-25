import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.function.Consumer;
/*-------------------------------------------
Program 4: BattleShip
Course: CS 342, Spring 2024, UIC
System: IntelliJ
Author: Aleena Mehmood, Fiza Bajwa, Ashika Shekar
------------------------------------------- */

/*-------------------------------------------
NOTE FROM AUTHORS:
player vs player (or "Play with human") only works when at least 2 clients are running.
If you try to hit "start" without an  opponent already connected on server, the code does not work.
Please ensure at least 2 clients are running before you hit "start" in the set ships page.
Thank you.
------------------------------------------- */
public class Client extends Thread{

	Socket socketClient;

	ObjectOutputStream out;
	ObjectInputStream in;

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
				Message data = (Message) in.readObject();
				callback.accept(data);
				System.out.println("message sent from server:" + data.getType());
			}
			catch(Exception e) {}
		}

	}

	public void send(Message data) {
		try {
			out.writeObject(data);
			System.out.println("sent to server: "+ Arrays.deepToString(data.getBoardState()));
			System.out.println("sent coord to sever: row: " + data.getRow() +" col: "+data.getCol());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}