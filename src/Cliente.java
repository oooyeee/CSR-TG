import java.io.*;
import java.net.Socket;

public class Cliente {

    private static final String HOST = "localhost";
    private static final int PORT = 1777;

    public static void main(String[] args) {
        new Cliente().start();
    }

    public void start() {
        try (Socket socket = new Socket(HOST, PORT)) {
            System.out.println("Client connected to " + HOST + ":" + PORT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start background thread to print server messages (MOTD + echoes)
            Thread serverReader = new Thread(() -> {
                try {
                    String line;
                    while (socket.isConnected() && (line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    // Connection lost
                }
                System.out.println("\nServer disconnected.");
            });
            serverReader.setDaemon(true);
            serverReader.start();

            System.out.println("Type your messages (or 'quit' to exit):");

            // Main loop: send user input while socket is connected
            try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
                String userInput;
                while (socket.isConnected() && (userInput = console.readLine()) != null) {
                    if ("quit".equalsIgnoreCase(userInput.trim())) {
                        break;
                    }
                    out.println(userInput);  // sends with \n
                }
            }

        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }

        System.out.println("Client stopped.");
    }
}