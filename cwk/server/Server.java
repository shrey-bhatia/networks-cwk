import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
	private static final int THREAD_POOL_SIZE = 20;
	private static final int PORT = 9100; // Replace with the desired port number

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				executorService.execute(new ClientHandler(clientSocket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			// TODO: Implement client request handling logic
		}
	}
}