package testes;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import server.AESCipher;
import server.Constants;
import server.DHCipher;
import server.Frame;
import util.Log;

public class Client {

    private final SocketChannel socket;
    private volatile boolean running = true;
    private DHCipher dh1;
    private DHCipher dh2;
    private AESCipher aes;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private boolean isSecure;
    private boolean isHandshaking;

    public Client(String host, int port) throws IOException {
        this.isSecure = false;
        this.isHandshaking = false;
        this.readBuffer = ByteBuffer.allocate(Constants.bufferSize);
        this.writeBuffer = ByteBuffer.allocate(Constants.bufferSize);

        this.dh1 = null;
        this.dh2 = null;

        this.aes = null;
        socket = SocketChannel.open(new InetSocketAddress(host, port));
        socket.configureBlocking(false);

    }

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = Constants.leiloesPort;
        Log.setLevel(4, false);

        Client client = new Client(host, port);
        client.start();
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
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Socket client ready\nStart Typing\nType: quit - to exit");
            while (running) {
                // System.out.print("$>");
                String line = scanner.nextLine();

                try {
                    if (line.equalsIgnoreCase("quit")) {
                        running = false;
                        socket.close();
                        System.out.println("Client quitting...");
                        break;
                    }
                    // Send string as bytes

                    Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), 0, null);
                    frame.length = line.getBytes(StandardCharsets.UTF_8).length;
                    frame.data = line.getBytes(StandardCharsets.UTF_8);
                    sendBytes(Frame.getBytes(frame));

                } catch (IOException e) {
                    Log.error("Error sending data");
                    Log.error(e);
                }
            }
        }
    }

    private void sendBytes(byte[] data) throws IOException {
        writeBuffer.clear();
        Frame frame = Frame.toFrame(ByteBuffer.wrap(data));
        writeBuffer.clear();
        writeBuffer.put(Frame.getBytes(frame));

        writeBuffer.flip();

        if (this.isSecure) {
            aes.encrypt(writeBuffer);
        } else {
            // ??
        }

        while (writeBuffer.hasRemaining()) {
            Log.info(":: sending " + writeBuffer.remaining() + " bytes");
            socket.write(writeBuffer);
        }
    }


    private void readFromSocket() {
        while (running) {
            try {
                readBuffer.clear();
                int bytesRead = socket.read(readBuffer);
                if (bytesRead > 0) {
                    Log.always(":: got message from socket, bytesRead: " + bytesRead);
                    readBuffer.flip();

                    if (this.isSecure) {
                        aes.decrypt(readBuffer);
                    }

                    byte[] readyBytes = new byte[readBuffer.remaining()];
                    readBuffer.get(readyBytes);

                    Frame tempFrame = Frame.toFrame(ByteBuffer.wrap(readyBytes));

                    Log.always(">> " + tempFrame.getDataString());

                    // Handshake Phase
                    if (!this.isSecure) {
                        isHandshaking = true;
                        handleHandshake(readyBytes);
                        continue;
                    }

                } else if (bytesRead == -1) {
                    // Log.info("Server closed connection");
                    running = false;
                    socket.close();
                    break;
                }
                Thread.sleep(50);
            } catch (Exception e) {
                if (running) {
                    Log.error("Error reading socket");
                    Log.error(e);
                }
                running = false;
            }
        }
    }

    public void handleHandshake(byte[] incoming) { // recieves HANDSHAKE with NESTED TLV, SENDS HANDSHAKE with simple TLV (only pubkey)
        Frame frame = Frame.toFrame(ByteBuffer.wrap(incoming));

        if (!isHandshaking) {
            Log.warn(":: not handshaking, ignoring frame ::");
            return;
        }

        // phase 1
        if (this.dh1 == null || !this.dh1.isComplete()) {
            if (frame.length != Frame.FrameType.NESTED_HANDSHAKE.ordinal()) {
                // frame: type=HANDSHAKE, length = 3, data: [frame,frame,frame]
                Log.error(":: expected length of 3 in the NESTED_HANDSHAKE frame ::");
                return;
            }

            Log.debug(":: phase 1");

            try {
                ByteBuffer buffer = ByteBuffer.wrap(incoming);
                buffer.getInt(); // skip type
                buffer.getInt(); // skip length
                Frame primeFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                primeFrame.type = buffer.getInt(); // data[0].type
                primeFrame.length = buffer.getInt(); // data[0].length
                primeFrame.data = new byte[primeFrame.length];
                buffer.get(primeFrame.data);

                Frame generatorFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                generatorFrame.type = buffer.getInt(); // data[1].type
                generatorFrame.length = buffer.getInt(); // data[1].length
                generatorFrame.data = new byte[generatorFrame.length];
                buffer.get(generatorFrame.data);

                Frame foreignPubKeyFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                foreignPubKeyFrame.type = buffer.getInt(); // data[1].type

                foreignPubKeyFrame.length = buffer.getInt(); // data[1].length

                foreignPubKeyFrame.data = new byte[foreignPubKeyFrame.length];

                buffer.get(foreignPubKeyFrame.data);
                

                this.dh1 = new DHCipher(new BigInteger(primeFrame.data), new BigInteger(generatorFrame.data));
                this.dh1.generateResult(foreignPubKeyFrame.data);

                // send my payload for dh1 (my pubkey)
                byte[] out = dh1.getResponsePayload(); // wraps pukey in frame
                Log.debug(":: phase 1 finishing, sending");
                sendBytes(out);
            } catch (Exception e) {
                Log.error(":: error parsing handshake buffer ::");
                Log.error(e);
                // TODO send error response to the server
            }

            return;
        }

        // phase 2
        if (this.dh2 == null || !this.dh2.isComplete()) {
            if (frame.length != Frame.FrameType.NESTED_HANDSHAKE.ordinal()) {
                // frame: type=HANDSHAKE, length = 3, data: [frame,frame,frame]
                Log.error(":: phase 2: expected length of 3 in the NESTED_HANDSHAKE frame ::");
                return;
            }

            Log.debug(":: phase 2");

            try {
                ByteBuffer buffer = ByteBuffer.wrap(incoming);
                buffer.getInt(); // skip type
                buffer.getInt(); // skip length
                Frame primeFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                primeFrame.type = buffer.getInt(); // data[0].type
                primeFrame.length = buffer.getInt(); // data[0].length
                primeFrame.data = new byte[primeFrame.length];
                buffer.get(primeFrame.data);

                Frame generatorFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                generatorFrame.type = buffer.getInt(); // data[1].type
                generatorFrame.length = buffer.getInt(); // data[1].length
                generatorFrame.data = new byte[generatorFrame.length];
                buffer.get(generatorFrame.data);

                Frame foreignPubKeyFrame = new Frame(Frame.FrameType.BYTES.ordinal(), 0, null);
                foreignPubKeyFrame.type = buffer.getInt(); // data[1].type
                foreignPubKeyFrame.length = buffer.getInt(); // data[1].length
                foreignPubKeyFrame.data = new byte[foreignPubKeyFrame.length];
                buffer.get(foreignPubKeyFrame.data);

                this.dh2 = new DHCipher(new BigInteger(primeFrame.data), new BigInteger(generatorFrame.data));
                this.dh2.generateResult(foreignPubKeyFrame.data);
                byte[] out = dh2.getResponsePayload(); // wraps pubkey in frame
                Log.debug(":: phase 2 finishing, sending");
                sendBytes(out);

                // phase 3
                if (this.aes == null) {
                    Log.debug(":: phase 3");
                    this.aes = new AESCipher(dh1.result, dh2.result);
                    this.isSecure = true;
                    isHandshaking = false;
                    Log.debug(":: phase 3 finished, AES DONE ::");
                    return;
                }

            } catch (Exception e) {
                Log.error(":: error parsing handshake buffer ::");
                Log.error(e);
                // TODO send error response to the server
            }

            return;
        }
        Log.rare(":: in handleHandshake, should not be here");
        return;
    }

}
