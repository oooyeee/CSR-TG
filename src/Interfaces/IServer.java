package interfaces;

import java.util.function.Consumer;

public interface IServer {
    public IServer configure(Consumer<IServerOptions> optionsCallback); // Implementation should apply options

    public void listen(int port, Runnable callback);
    public String name();
}