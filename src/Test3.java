
import java.nio.ByteBuffer;

import servidor.FrameValidator;
import servidor.SecureCipher;
import servidor.SecureServer;


public class Test3 {

    public static void main(String[] args) {
        SecureServer servidorLeiloes = new SecureServer(1777);

        servidorLeiloes.useValidator(new FrameValidator());
        servidorLeiloes.useSecureCipherClass(SecureCipher.class);

        servidorLeiloes.init();

        servidorLeiloes.onData((session, data) -> {
            System.out.println("in the main");
            int data_size = data.remaining();
            System.out.println("message size: " + data_size);
            byte[] temp = new byte[data_size];
            data.duplicate().get(temp);
            session.write("got your message: " + new String(temp));
        });

        servidorLeiloes.onAccept((session) -> {
            System.out.println("in Test 3 on Accept");
        });

        // servidorLeiloes.onDisconnect((session) -> {
        //     System.out.println("in Test 3 on Disco");
        //     session.disconnect();
        // });

        servidorLeiloes.listen();
    }

}
