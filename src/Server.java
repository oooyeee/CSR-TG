import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import servidor.Servidor;

public class Server {

    public static final int PORT = 1777;

    public static void main(String args[]) {
        System.out.println("SERVER APP STARTING");

        Servidor servidor = new Servidor(PORT);

        servidor.init()
            .onAccept((session)->{
                // session.write has overload for a String and a ByteBuffer
                session.write("""
                        Welcome to server
                        Your ID: %s,
                        Online users: %s
                        """.formatted(session.clienID(), session.getOnlineCount()));

                // session.broadcast has overload for a String and a ByteBuffer
                session.broadcast("Client [" +session.clienID()+"] just joined\n");
            })
            .onMessage((session, msg)-> {
                String message = StandardCharsets.UTF_8.decode(msg.duplicate()).toString();
                String userMessage = "[" +session.clienID()+"]: " + message;
                System.out.println(userMessage.trim());
                // session.broadcast has overload for a String and a ByteBuffer
                session.broadcast(userMessage);
            })
            .onDisconnect((session)->{
                System.out.println("Client [" + session.clienID() + "] has disconnected");
                session.disconnect();
            });

        servidor.listen();

    }
}
