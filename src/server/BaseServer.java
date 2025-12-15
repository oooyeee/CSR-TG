package server;

import interfaces.IBaseServer;
import interfaces.IClientSession;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import util.Log;

public class BaseServer implements IBaseServer {

    private int bufferSizeInKB;

    private Consumer<IClientSession> acceptCallback;
    private BiConsumer<IClientSession, ByteBuffer> dataCallback;
    private Consumer<IClientSession> disconnectCallback;

    private Selector selector;
    private ServerSocketChannel serverChannel;
    // Connected clients
    protected final ConcurrentHashMap<String, ClientSession> clients = new ConcurrentHashMap<>();

    public BaseServer() {
        this(Constants.bufferSizeInKB);
    }

    public BaseServer(int bufferSizeInKB) {
        this.bufferSizeInKB = bufferSizeInKB;

        try {
            this.selector = Selector.open();
            this.serverChannel = ServerSocketChannel.open();
            // this.serverChannel.bind(new InetSocketAddress(port)); // bind at listen()
            this.serverChannel.configureBlocking(false);
            this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            Log.error("Error, while creating the server: " + e.getMessage());
        }
    }

    @Override
    public void listen(int port) {
        try {
            this.serverChannel.bind(new InetSocketAddress(port));

            while (true) {
                try {
                    selector.select();
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isAcceptable()) {
                            handleAccept();
                        }

                        if (key.isReadable()) {
                            handleRead(key);
                        }

                        if (!key.isValid()) {
                            continue; // key channel may be disconnected in the handleRead and become invalid
                        }

                        if (key.isWritable()) {
                            handleWrite(key);
                        }
                    }
                } catch (Exception e) {
                    Log.error("Something wrong with selectors or channels...");
                    Log.error(e.getMessage());
                    e.printStackTrace();

                } finally {
                    selector.selectedKeys().clear();
                }
            }

        } catch (Exception e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public IBaseServer onAccept(Consumer<IClientSession> callback) {
        this.acceptCallback = callback;
        return this;
    }

    @Override
    public IBaseServer onData(BiConsumer<IClientSession, ByteBuffer> callback) {
        this.dataCallback = callback;
        return this;
    }

    @Override
    public IBaseServer onDisconnect(Consumer<IClientSession> callback) {
        this.disconnectCallback = callback;
        return this;
    }

    private void handleAccept() {
        try {
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false); // throws IOException
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ); // throws ClosedChannelException

            ClientSession session = new ClientSession(clientKey, this.bufferSizeInKB);
            clientKey.attach(session);
            clients.put(session.clientID(), session);

            if (this.acceptCallback != null) {
                this.acceptCallback.accept(session);
            }
        } catch (Exception e) { // IOException, ClosedChannelException
            Log.error("Error accepting connection...");
            Log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        SocketChannel clientChannel = (SocketChannel) key.channel();

        int bytesRead;

        try {
            session.readBuffer.clear();
            bytesRead = clientChannel.read(session.readBuffer); // throws IOException
        } catch (SocketException e) {
            Log.error("Client disconnected HARD"); // sometimes, if client terminates with ctrl+c
            Log.error(e);

            handleDisconnect(session);
            return;
        } catch (Exception e) {
            Log.error("Error reading buffer from client channel");
            Log.error(e);
            bytesRead = -1;
        }

        if (bytesRead == 0) {
            return;
        }

        if (bytesRead == -1) { // client disconnected
            handleDisconnect(session);
            return;
        }

        session.readBuffer.flip(); // ready to be read (from 0 to limit)

        if (this.dataCallback != null) {
            this.dataCallback.accept(session, session.readBuffer);
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
            Log.error("Handle Write Exception");
            Log.error(e);
            disconnect(session);
        }

    }

    private void disconnect(IClientSession session) {
        try {
            session.clientKey().cancel();
            session.clientKey().channel().close();
        } catch (Exception e) {
            Log.error("Failed to close the connection for client: " + session.clientID());
            Log.error(e);
        }
        Log.info("disconnecting client session: " + session.clientID());
        clients.remove(session.clientID());
    }

    protected void handleDisconnect(IClientSession session) {
        if (this.disconnectCallback != null) {
            this.disconnectCallback.accept(session);

            String sessionID = null;

            for (Map.Entry<String, ClientSession> entry : clients.entrySet()) {
                if (entry.getValue().clientID().equals(session.clientID())) {
                    sessionID = entry.getKey();
                    break;
                }
            }

            if (sessionID == null) {
                Log.error("Cannot disconnect unknown client session");
                return;
            }

            this.disconnect(session);
        } else {
            this.disconnect(session);
        }
    }

}
