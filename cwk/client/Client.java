import java.io.*;
import java.net.Socket;

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
		try {
			// Send the "list" request to the server
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("list");

			// Read the response from the server
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String response = in.readLine();

			if (response.startsWith("Error:")) {
				System.out.println(response);
			} else {
				System.out.println("Listing " + response.split(" ")[1] + " file(s):");
				while ((response = in.readLine()) != null) {
					System.out.println(response);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handlePutCommand(Socket socket, String filename) {
		try {
			File file = new File(filename);
			if (!file.exists()) {
				System.err.println("Error: Cannot open local file '" + filename + "' for reading.");
				return;
			}

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.println("put " + file.getName());
			String response = in.readLine();
			if (response.startsWith("Error:")) {
				System.out.println(response);
			} else {
				// Send the file contents to the server
				BufferedReader fileReader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = fileReader.readLine()) != null) {
					out.println(line);
				}
				fileReader.close();
				System.out.println("Uploaded file " + file.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}