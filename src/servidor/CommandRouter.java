package servidor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import interfaces.IClientSession;
import interfaces.ICommandRouter;

public class CommandRouter implements ICommandRouter {

    @Override
    public ICommandRouter onCommand(String command, BiConsumer<IClientSession, String> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onCommand'");
    }

    @Override
    public ICommandRouter onType(byte command, BiConsumer<IClientSession, byte[]> callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onType'");
    }

}
