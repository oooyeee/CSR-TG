package interfaces;

import java.util.function.Consumer;

public interface ICommandRouter {
    public ICommandRouter onCommand(String command, Consumer<IClientSession> session);
    public ICommandRouter onType(byte command, Consumer<IClientSession> session);
}
