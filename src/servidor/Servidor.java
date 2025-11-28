package servidor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import interfaces.IClientSession;
import interfaces.IServidor;

public class Servidor implements IServidor {

    private int port;
    private boolean isInitiated;
    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Consumer<IClientSession> acceptCallback;
    private BiConsumer<IClientSession, ByteBuffer> messageCallback;
    private Consumer<IClientSession> disconnectCallback;

    // Connected clients
    private final ConcurrentHashMap<SelectionKey, ClientSession> clients = new ConcurrentHashMap<>();

    public Servidor(int port) {
        this.port = port;

        this.acceptCallback = null;
        this.messageCallback = null;
        this.disconnectCallback = null;
    }

    @Override
    public IServidor init() {
        if (isInitiated == true) {
            return this;
        }

        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            // return this;
        } catch (Exception e) {
            System.err.println("Error initiating server: " + e.getMessage());
            return null;
        }

        isInitiated = true;
        return this;

    }

    @Override
    public void listen() {
        if (!isInitiated) {
            System.out.println("Call init() first!");
            return;
        }

        System.out.println("Server is listening on port " + port);
        while (true) {
            try {
                selector.select(); // wait for socket selector events

                for (SelectionKey key : selector.selectedKeys()) {

                    // if(!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    // if(!key.isValid()) continue;

                    if (key.isReadable()) {
                        handleRead(key);
                    }

                    if(!key.isValid()) continue; // key channel may be disconnected in the handleRead and become invalid

                    if (key.isWritable()) {
                        handleWrite(key);
                    }
                    // if(!key.isValid()) continue;
                }
                selector.selectedKeys().clear();

            } catch (Exception e) {
                System.err.println("Something wrong with selectors or channels...");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleWrite(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        if (session.pendingWrite == null) {
            key.interestOpsAnd(~SelectionKey.OP_WRITE); // removes write key for next cycle
            return;
        }

        try {
            session.write(session.pendingWrite);

        } catch (Exception e) {
            System.err.println("Handle Write Exception");
            System.err.println(e);
            session.disconnect();
            // TODO, maybe use disconnectCallback ?
        }

    }

    private void handleRead(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();

        int read;

        try {
            session.readBuffer.clear();
            read = client.read(session.readBuffer); // throws IOException
        } catch (Exception e) {
            System.err.println("Error reading buffer from client channel");
            System.err.println(e);
            read = -1;
        }

        if (read == 0) {
            return;
        }

        if (read == -1) { // client disconnected
            this.disconnectCallback.accept(session);
            return;
        }

        // System.out.println("::: read " + String.valueOf(read) + " bytes");
        session.readBuffer.flip(); // ready to write

        ByteBuffer chunk = session.readBuffer.duplicate().limit(read);

        if (this.messageCallback != null) {
            // this.messageCallback.accept(session, session.readBuffer.duplicate().limit(read)); // needs duplicate + limit (there is garbage after limit)
            this.messageCallback.accept(session, chunk);
            session.readBuffer.compact(); // TODO, maybe not needed
            // session.readBuffer.clear(); // TODO, maybe not needed
        }
    }

    private void handleAccept() {
        try {
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false); // throws IOException
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ); // throws ClosedChannelException

            ClientSession session = new ClientSession(clientKey, this.clients);
            clientKey.attach(session);
            clients.put(clientKey, session);

            if (this.acceptCallback != null) {
                this.acceptCallback.accept(session);
            }
        } catch (Exception e) { // IOException, ClosedChannelException
            System.err.println("Error accepting connection...");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public IServidor onAccept(Consumer<IClientSession> callback) {
        this.acceptCallback = callback;
        return this;
    }

    @Override
    public IServidor onMessage(BiConsumer<IClientSession, ByteBuffer> callback) {
        this.messageCallback = callback;
        return this;
    }

    @Override
    public IServidor onDisconnect(Consumer<IClientSession> callback) {
        this.disconnectCallback = callback;
        return this;
    }

}
