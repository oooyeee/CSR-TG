package servidor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import interfaces.IClientSession;
import interfaces.ISecureCipher;
import java.util.ArrayList;
import java.util.List;

public class ClientSession implements IClientSession {

    public static int id_count = 0;

    private String clientID;
    private SelectionKey clientKey;
    private ConcurrentHashMap<SelectionKey, ClientSession> allClients;

    protected boolean isSecure;
    protected ISecureCipher cipher;
    protected List<ByteBuffer> readChunks;
    protected ByteBuffer readBuffer;
    protected ByteBuffer pendingWrite;

    private boolean isDisconnectCalled;

    public ClientSession(SelectionKey clientKey, ConcurrentHashMap<SelectionKey, ClientSession> allClients, int bufferSizeKB) {
        this.clientKey = clientKey;
        this.allClients = allClients;

        // this.clientID = UUID.randomUUID().toString().substring(0, 8);
        this.clientID = String.valueOf(id_count++);

        this.isSecure = false;
        this.cipher = null;

        this.pendingWrite = null;
        this.readBuffer = ByteBuffer.allocate(1024 * bufferSizeKB);
        this.readChunks = new ArrayList<>();

        this.isDisconnectCalled = false;
    }

    @Override
    public String clientID() {
        return this.clientID;
    }

    @Override
    public SelectionKey clientKey() {
        return this.clientKey;
    }

    @Override
    public int getOnlineCount(){
        return this.allClients.size();
    }

    
    @Override
    public void write(String message) {
        // System.out.println(">>> IN WRITE (STRING) >>>");
        // write(StandardCharsets.UTF_8.encode(message));
        write(StandardCharsets.UTF_8.encode(message + "\n"));
    }

    @Override
    public void write(ByteBuffer buffer) {
        // System.out.println(">>> IN WRITE (BUFFER) >>>");
        // System.out.println("::" + new String(buffer.array(), StandardCharsets.UTF_8));
        writeBuffer(buffer); // TODO, maybe duplicate ?
    }

    @Override
    public void broadcast(String message) {
        // allClients.values().forEach((session) -> {
        this.broadcast(StandardCharsets.UTF_8.encode(message));
    }

    @Override
    public void broadcast(ByteBuffer buffer) {
        ByteBuffer buf = buffer;
        allClients.values().forEach((session) -> {
            session.write(buf.duplicate()); // TODO, maybe duplicate ?
            // buffer.rewind();
        });
    }

    private void writeBuffer(ByteBuffer buffer){

        // System.out.println("GOT BUFFER CLASS: " + buffer.getClass());

        SocketChannel clientChannel = (SocketChannel) this.clientKey.channel();
        try {
            while(buffer.hasRemaining()) {
                int written = clientChannel.write(buffer);
                if(written == 0) {
                    pendingWrite = buffer;
                    clientKey.interestOpsOr(SelectionKey.OP_WRITE); // next time server will check for write key first and use pendingWrite buffer (i think)
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write to the client: " + clientID);
            System.err.println(e);
            disconnect();
        }
    }

    @Override
    public void disconnect() {
        try {
            this.clientKey.cancel();
            clientKey.channel().close();
            this.allClients.remove(this.clientKey);
            System.out.println("["+clientID+"] - connection closed");
        } catch (IOException e) {
            System.err.println("Failed to close the connection for client: " + clientID);
            System.err.println(e);
            //TODO, ignore ?
        } finally {
            this.isDisconnectCalled = true;
        }
    }

    @Override
    public List<ByteBuffer> getDataChunks() {
        return this.readChunks;
    }

    @Override
    public void putDataChunk(ByteBuffer chunk) {
        this.readChunks.add(chunk);
    }

    @Override
    public boolean isSecure(){
        if (this.cipher != null && this.isSecure) return true;
        
        return false;
    }

    @Override
    public ISecureCipher getCipher(){
        return this.cipher;
    }


    @Override
    public void setCipherOnce(ISecureCipher cipher) {
        if (this.cipher != null) return;
        
        this.cipher = cipher;
    }

    @Override
    public boolean isDisconnectCalled(){
        return this.isDisconnectCalled;
    }
}
