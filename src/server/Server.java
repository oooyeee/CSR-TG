package server;

import interfaces.*;
import java.util.function.Consumer;
import util.Log;

public class Server implements IServer {
    String name;

    private final BaseServer server;
    private ServerOptions serverOptions;

    public Server(String name) {
        this.name = name;
        this.server = new BaseServer();
        this.serverOptions = null;
    }

    public Server(String name, int bufferSizeInKB) {
        this.name = name;
        this.server = new BaseServer(bufferSizeInKB);
        this.serverOptions = null;
    }

    public Server(String name, BaseServer server) {
        this.name = name;
        this.server = server;
        this.serverOptions = null;
    }

    @Override
    public IServer configure(Consumer<IServerOptions> optionsCallback) {
        this.serverOptions = new ServerOptions();
        optionsCallback.accept(this.serverOptions);
        return this;
    }

    @Override
    public void listen(int port, Runnable callback) {
        try {
            callback.run();
            this.applyOptions();
            server.listen(port);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public String name() {
        return this.name;
    }

    private void applyOptions() { // TODO add accept middleware
        if (this.serverOptions != null) {
            ServerOptions o = this.serverOptions;

            IMiddlewareNode read_middleware_head = o.readChain.head();
            server.onData((session, data) -> {
                IMiddlewareNode current = read_middleware_head;
                boolean result = true;

                while (current != null) {
                    result = current.handle(session, data);
                    if (result == false) {
                        break;
                    }
                    current = current.next();
                }
            });

            IMiddlewareNode accept_middleware_head = o.handshakeChain.head();
            server.onAccept((session) -> { // runs once

                // inject writeChain
                session.injectWriteFiltersChain(o.writeChain);

                IMiddlewareNode current = accept_middleware_head;
                boolean result;
                while (current != null) {
                    result = current.handle(session, null);
                    if (!result) {
                        break;
                    }
                    current = current.next();
                }
            });

            // todo add to write middleware
        }
    }
}
