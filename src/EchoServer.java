import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import servidor.ServidorBase;

public class EchoServer {

    public static final int PORT = 1777;

    public static void main(String args[]) {
        System.out.println("SERVER APP STARTING");

        ServidorBase servidor = new ServidorBase(PORT);

        servidor.init()
            .onAccept((session)->{
                // session.write has overload for a String and a ByteBuffer
                // session.write("""
                //         Welcome to server
                //         Your ID: %s,
                //         Online users: %s
                //         """.formatted(session.clienID(), session.getOnlineCount()));

                String welcome = "Welcome to server\nYour ID: " + session.clientID() + "\nOnline users: " + session.getOnlineCount();
                session.write(welcome);

                // session.broadcast has overload for a String and a ByteBuffer
                session.broadcast("Client [" +session.clientID()+"] just joined\n");
            })
            .onData((session, data)-> { // if buffer is small, process in chunks, store in session.readChunks
                String message = StandardCharsets.UTF_8.decode(data.duplicate()).toString();
                String userMessage = "[" +session.clientID()+"]: " + message;
                System.out.println(userMessage.trim());
                // session.broadcast has overload for a String and a ByteBuffer
                session.broadcast(userMessage);
            })
            .onDisconnect((session)->{
                System.out.println("Client [" + session.clientID() + "] has disconnected");
                session.disconnect();
            });

        servidor.listen();

    }
}
