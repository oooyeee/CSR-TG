package Cliente;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import servidor.Frame;

public class Cliente {

    private SocketChannel socket;
    private boolean isRunning;

    public Cliente(String host, int port) {
        socket = null;
        isRunning = true;
        try {
            socket = SocketChannel.open(new InetSocketAddress(host, port));
            socket.configureBlocking(false);
        } catch (Exception e) {
            System.err.println("Could not connect to the server");
            System.err.println(e.getMessage());
        }
    }

    public void start() {
        // Thread to read console input
        Thread consoleThread = new Thread(this::handleConsole);
        consoleThread.start();

        // Thread to read from socket
        Thread socketThread = new Thread(this::readFromSocket);
        socketThread.start();
    }

    private void handleConsole() {
        Scanner scanner = new Scanner(System.in);
        while (isRunning) {
            System.out.print("Type (quit/test[1,2,3] or anything else):\n>");
            String line = scanner.nextLine();

            try {
                if (line.equalsIgnoreCase("quit")) {
                    isRunning = false;
                    socket.close();
                    System.out.println("Client quitting...");
                    break;
                } else if (line.equalsIgnoreCase("test1")) {

                    Frame frame = Frame.toFrame("privet");
                    byte[] arr = Frame.toByteArray(frame);
                    System.out.println("message size in bytes: " + arr.length);
                    System.out.println(Frame.toPrettyString(frame));

                    sendBytes(arr);
                    System.out.println("Sent test bytes 1");
                } else if (line.equalsIgnoreCase("test2")) {
                    byte[] arr = {
                            (byte) 0,
                            (byte) 0,
                            (byte) 0,
                            (byte) 0, // 1
                            (byte) 0,
                            (byte) 0,
                            (byte) 0,
                            (byte) 5, // 5
                            (byte) 11,
                            (byte) 22,
                            (byte) 33,
                            (byte) 44,
                            (byte) 55
                    };

                    sendBytes(arr);
                    System.out.println("Sent test bytes 2");
                } else if (line.equalsIgnoreCase("test3")) {
                    byte[] arr = {
                            (byte) 0,
                            (byte) 0,
                            (byte) 0,
                            (byte) 123, // 1
                            (byte) 0,
                            (byte) 0,
                            (byte) 0,
                            (byte) 5, // 5
                            (byte) 11,
                            (byte) 22,
                            (byte) 33,
                            (byte) 44,
                            (byte) 55
                    };

                    sendBytes(arr);
                    System.out.println("Sent test bytes 3");
                } else {
                    // Send string as bytes
                    sendBytes(line.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                System.err.println("Error sending data: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private void sendBytes(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            socket.write(buffer);
        }
    }

    private void readFromSocket() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (isRunning) {
            try {
                int bytesRead = socket.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] received = new byte[buffer.remaining()];
                    buffer.get(received);
                    System.out.println("Received: " + bytesToHex(received));
                    // System.out.println("Received: " + new String(received));
                    buffer.clear();
                } else if (bytesRead == -1) {
                    System.out.println("Server closed connection");
                    isRunning = false;
                    socket.close();
                    break;
                }
                Thread.sleep(50);
            } catch (IOException | InterruptedException e) {
                if (isRunning) {
                    System.err.println("Error reading socket: " + e.getMessage());
                }
                isRunning = false;
            }
        }
    }

    // Helper to print bytes in hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 1777;

        Cliente client = new Cliente(host, port);
        client.start();
    }
}
