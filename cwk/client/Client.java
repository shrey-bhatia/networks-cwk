import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args) {
		if (args.length != 1 && args.length != 2) {
			System.err.println("Usage: java Client <command> [filename]");
			System.err.println("Commands: list, put <filename>");
			System.exit(1);
		}

		String command = args[0];
		String filename = (args.length == 2) ? args[1] : null;

		try {
			Socket socket = new Socket("localhost", 9100); // Replace 9100 with the desired port number

			switch (command) {
				case "list":
					handleListCommand(socket);
					break;
				case "put":
					if (filename == null) {
						System.err.println("Error: Missing filename for 'put' command");
						System.exit(1);
					}
					handlePutCommand(socket, filename);
					break;
				default:
					System.err.println("Error: Invalid command");
					System.exit(1);
			}

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleListCommand(Socket socket) {
		// TODO: Implement logic to request file list from the server
	}

	private static void handlePutCommand(Socket socket, String filename) {
		// TODO: Implement logic to upload the file to the server
	}
}