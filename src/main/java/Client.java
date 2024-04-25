import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.function.Consumer;

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
//				String message = in.readObject().toString();
//				callback.accept(message);
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