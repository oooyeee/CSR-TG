package interfaces;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;

public interface IClientSession {
    public String clientID();

    public SelectionKey clientKey();

    public int getOnlineCount();
    public boolean isSecure();
    public ISecureCipher getCipher();
    public void setCipherOnce(ISecureCipher cipher);

    // write functions send message to single client of current client section
    public void write(String message);
    public void write(ByteBuffer buffer);

    // broadcast functions require implementation to have access to all sessions
    // in order to send message to all clients
    public void broadcast(String message);
    public void broadcast(ByteBuffer buffer);

    public void disconnect();
    public boolean isDisconnectCalled();

    public List<ByteBuffer> getDataChunks();
    public void putDataChunk(ByteBuffer chunk);
}
