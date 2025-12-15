package interfaces;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IBaseServer {
    public void listen(int port);

    public IBaseServer onAccept(Consumer<IClientSession> callback);
    public IBaseServer onData(BiConsumer<IClientSession, ByteBuffer> callback);
    public IBaseServer onDisconnect(Consumer<IClientSession> callback);
}