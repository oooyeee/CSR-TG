package server;

import interfaces.*;
import java.nio.ByteBuffer;
import java.util.function.BiPredicate;
import util.Log;

public class ServerOptions implements IServerOptions {
    protected MiddlewareList handshakeChain;
    protected MiddlewareList readChain;
    protected MiddlewareList writeChain;

    public ServerOptions() {
        this.handshakeChain = new MiddlewareList();
        this.readChain = new MiddlewareList();
        this.writeChain = new MiddlewareList();
    }

    @Override
    public IServerOptions useRouter(ICommandRouter router) {
        this.addToReadChain((session, data) -> {

            Frame frame = Frame.toFrame(data);

            if (frame.type != Frame.FrameType.STRING.ordinal()) {
                return false;
            }

            String msg = frame.getDataString();

            if (msg == null) {
                return false;
            }

            if (msg.equals("")) {

                return false;
            }

            if (msg.equals("\n")) {
                return false;
            }

            router.parseCommand(session, msg);

            return false;
        });

        return this;
    }

    @Override
    public IServerOptions addToHandshakeChain(BiPredicate<IClientSession, ByteBuffer> callback) {
        this.handshakeChain.add(callback);
        return this;
    }

    @Override
    public IServerOptions addToReadChain(BiPredicate<IClientSession, ByteBuffer> callback) {
        this.readChain.add(callback);
        return this;
    }

    @Override
    public IServerOptions addToWriteChain(BiPredicate<IClientSession, ByteBuffer> callback) {
        this.writeChain.add(callback);
        return this;
    }

}
