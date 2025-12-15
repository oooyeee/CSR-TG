package interfaces;

import java.util.function.BiConsumer;

public interface ICommandRouter {

    public void onCommand(String command, BiConsumer<IClientSession, String> callback);

    public void commandFallback(BiConsumer<IClientSession, String> callback); // for unsupported commands

    public void parseCommand(IClientSession session, String message);

    // public void write(IClientSession session, ByteBuffer data);
    // public void addWritePipeline(BiPredicate<IClientSession, ByteBuffer> callback);
}
