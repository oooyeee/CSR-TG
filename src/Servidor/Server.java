package Servidor;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private static final int PORTA = 5050;

    private final Map<String, PublicKey> usuarios = new ConcurrentHashMap<>();
    private final Map <String, ItemLeilao> itensLeilao = new ConcurrentHashMap<>();
    private final Map <String, List<Lance>> lances = new ConcurrentHashMap<>();

    private KeyPair keyPairServidor;

    //public ServidorLeilaoTexto() throws Exception {
        /*if (KeyManager.userHasKeys("servidor")) {
            keyPairServidor = new KeyPair(
                    KeyManager.loadPublicKey("servidor"),
                    KeyManager.loadPrivateKey("servidor")
            );
            System.out.println("Chaves do servidor carregadas.");
        } else {
            keyPairServidor = CryptoManager.generateKeyPair();
            KeyManager.saveKeyPair("servidor", keyPairServidor);
            System.out.println("Novas chaves do servidor geradas.");
        }*/
    //}

    /*########################################################
    Iniciar Servidor
    ################################################################*/

    public void iniciar() {
        System.out.println("Servidor de Leilões");
        System.out.println("Porta:" + PORTA);

        try (ServerSocket ss = new ServerSocket(PORTA)) {

            while (true) {
                Socket socket = ss.accept();
                System.out.println("\nCliente Conectado:" + socket.getInetAddress());

                new Thread(() -> tratarCliente(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*#############################################################
    Tratar cliente
    ################################################################*/

    public void tratarCliente (Socket skt){
        try ( BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
              PrintWriter out = new PrintWriter(skt.getOutputStream())
        ){
            String linha = in.readline();
            if (linha == null)
                return;
            MensagemSegura msg = ProtocoloSeguro.deserializarMensagem(linha);
            MensagemSegura resposta = processarMensagem(msg);

            String respostaStr = ProtocoloSeguro.serializarMensagem(resposta).replace("\n", "").replace("\r", "");

            out.println(respostaStr);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                skt.close();
            }catch (IOException ignored){}
        }
    }

    private MensagemSegura processarMensagem (MensagemSegura mensagem) {
        tr
    }
}
