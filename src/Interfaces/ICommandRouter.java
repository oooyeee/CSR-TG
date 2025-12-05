package interfaces;

import java.util.function.BiConsumer;

public interface ICommandRouter {
    public ICommandRouter onCommand(String command, BiConsumer<IClientSession, String> callback);
    public ICommandRouter onType(byte command, BiConsumer<IClientSession, byte[]> callback);
}
