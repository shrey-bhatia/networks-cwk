import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 20;
    private static final int PORT = 9100; // Replace with the desired port number

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //System.out.println("Server started on port " + PORT);
            //System.out.println("Current working directory: " + System.getProperty("user.dir"));
            //TODO: Remove the above print statements for submission

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
        private final File serverFilesDir;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            String serverFilesDirPath = System.getProperty("user.dir") + "/cwk/server/serverFiles";
            this.serverFilesDir = new File(serverFilesDirPath);

            // Create the serverFiles directory if it doesn't exist
            if (!serverFilesDir.exists() && !serverFilesDir.mkdir()) {
                System.err.println("Failed to create serverFiles directory");
            }
        }

        private static String getCurrentTimestamp() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }

        private void logRequest(String timestamp, String clientIP, String request) {
            try {
                // Get the path of the directory containing Server.java
                String serverDir = System.getProperty("user.dir") + "/cwk/server";
                File logFile = new File(serverDir, "log.txt");
                if (!logFile.exists() && !logFile.createNewFile()) {
                    System.err.println("Failed to create log file");
                    return;
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
                } else if (request.startsWith("put ")) {
                    handlePutRequest(in, out, request.substring(4)); // Pass the filename to handlePutRequest
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

        private void handlePutRequest(BufferedReader in, PrintWriter out, String filename) throws IOException {
            File file = new File(serverFilesDir, filename);

            if (file.exists()) {
                out.println("Error: Cannot upload file '" + filename + "'; already exists on server.");
            } else {
                out.println("Uploading file '" + filename + "'");
                if (!file.createNewFile()) {
                    out.println("Error: Failed to create file '" + filename + "' on server.");
                    return;
                }

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