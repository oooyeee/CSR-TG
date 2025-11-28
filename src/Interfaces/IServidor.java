package interfaces;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IServidor {
    public IServidor init();
    public void listen();

    public IServidor onAccept(Consumer<IClientSession> callback);
    // IServidor onMessage(BiConsumer<IClientSession, String> callback);
    public IServidor onMessage(BiConsumer<IClientSession, ByteBuffer> callback);
    public IServidor onDisconnect(Consumer<IClientSession> callback);
}
