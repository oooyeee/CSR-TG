package interfaces;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public interface IClientSession {
    public String clientID();

    public SelectionKey clientKey();

    public void write(String message) throws IOException;

    public void write(ByteBuffer buffer) throws IOException;

    public ByteBuffer getReadBuffer();

    public void injectWriteFiltersChain(IMiddlewareList writeFiltersChain);

    public SessionState getSecureState();
    public WriteState getWriteState();

    public void setSecureState(SessionState newState);
    public void setWriteState(WriteState newState);

    public boolean isTrusted();
    public void setTrusted(boolean value);

    public static enum SessionState {
        NEED_HANDSHAKE,
        SECURE
    }

    public static enum WriteState {
        READY,
        NOT_READY
    }

}
