import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		} finally {
			executorService.shutdown(); // Add this line to shut down the ExecutorService
		}
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private final File serverFilesDir = new File("serverFiles");

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		private static String getCurrentTimestamp() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		}

		private void logRequest(String timestamp, String clientIP, String request) {
			try {
				File logFile = new File("log.txt");
				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
				writer.write(timestamp + "|" + clientIP + "|" + request);
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

				String request = in.readLine();
				if (request.equals("list")) {
					handleListRequest(out);
					logRequest(getCurrentTimestamp(), clientSocket.getInetAddress().getHostAddress(), "list");
				} else if (request.equals("put")) {
					handlePutRequest(in, out);
				} else {
					out.println("Error: Invalid request");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void handleListRequest(PrintWriter out) {
			File[] files = serverFilesDir.listFiles();
			if (files != null) {
				out.println("Listing " + files.length + " file(s):");
				for (File file : files) {
					out.println(file.getName());
				}
			} else {
				out.println("Error: Failed to list files");
			}
		}

		private void handlePutRequest(BufferedReader in, PrintWriter out) throws IOException {
			String filename = in.readLine().substring(4); // Remove "put " prefix
			File file = new File(serverFilesDir, filename);

			if (file.exists()) {
				out.println("Error: Cannot upload file '" + filename + "'; already exists on server.");
			} else {
				out.println("Uploading file '" + filename + "'");
				file.createNewFile(); // Result of 'File.createNewFile()' is ignored

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				String line;
				while ((line = in.readLine()) != null) {
					writer.write(line);
					writer.newLine();
				}
				writer.close();

				out.println("Uploaded file '" + filename + "'");
				logRequest(getCurrentTimestamp(), clientSocket.getInetAddress().getHostAddress(), "put");
			}
		}
	}
}