import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {
	private Socket socketClient;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Consumer<Serializable> callback;

	public Client(Consumer<Serializable> callback) {
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			// Connect to the server
			socketClient = new Socket("127.0.0.1", 5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);

			// Continuously listen for messages from the server
			while (true) {
				Serializable message = (Serializable) in.readObject();
				callback.accept(message);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			// Close the socket and streams when finished
			try {
				if (socketClient != null) {
					socketClient.close();
				}
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Method to send a message to the server
	public void send(Serializable data) {
		try {
			out.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
