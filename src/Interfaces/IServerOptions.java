package interfaces;

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

public interface IServerOptions {
    // public IServerOptions useSecureProvider(ISecurityProvider cp);
    // public IServerOptions useValidator(IValidator validator);

    public IServerOptions useRouter(ICommandRouter router);

    public IServerOptions addToHandshakeChain(BiPredicate<IClientSession, ByteBuffer> callback);

    public IServerOptions addToReadChain(BiPredicate<IClientSession, ByteBuffer> callback);

    public IServerOptions addToWriteChain(BiPredicate<IClientSession, ByteBuffer> callback);
}