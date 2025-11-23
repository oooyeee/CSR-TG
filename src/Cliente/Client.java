package Cliente;

import java.security.*;
import java.util.*;
import java.io.*;
import java.net.*;


public class Client {
    private static final String HOST = "localhost";
    private static final int PORTA=5050;

    private String userName;
    //private KeyPair;
    //private PublicKeyServidor;
    private final Scanner scanner = new Scanner(System.in);

    public static void main (String[] args){
        new Client().start();
    }

    public void start (){
        System.out.println("Cliente Leilões Seguro \n");

        // Carregar a chave do server


    }

    //##################################
    // Fazer Login
    //##################################
    private void login () Exception {

    }


    //########################################
    //Registrar novo cliente
     //#######################################
    private void newUser throws Exception {

    }

    /*#############################################
                    Menu
    ############################################*/
    private void menu (){
        while (true){
            System.out.println ("\n1. Criar Leilão");
            System.out.println ("2. Listar Ativos");
            System.out.println ("3. Fazer Lance");
            System.out.println ("4. Ver Encerrados");
            System.out.println ("5. Sair");
            System.out.println (">");

            try {
                int op = Integer.parseInt(scanner.nextLine());

                switch (op) {
                    case 1 -> criarLeilao();
                    case 2 -> listarAtivos();
                    case 3 -> fazerLance();
                    case 4 -> listarEncerrados();
                    case 5 -> {return;}
                    default -> System.out.println ("Inválido");
                }
            } catch (Exception e) {
                System.out.println ("Erro:" );
            }
        }
    }

    /*##################################################################
    Criar Leilão
     ###################################################################*/

    private void criarLeilao() throws Exception {
        System.out.println("Descrição: ");
        String desc = scanner.nextLine();

        System.out.println ("Valor mínimo: ");
        double min = Double.parseDouble(scanner.nextLine());

        System.out.println ("Duração em horas:");
        int horas = Integer.parseInt(scanner.nextLine());

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, horas);
        Date fim = c.getTime();

        /*ItemLeilao item = new ItemLeilao(desc, fim, min, userName);
        byte[] assinatura = CryptoManager.sign(item.getDadosParaAssinatura(), keyPair.getPrivate());
        item.setAssinaturaVendedor(assinatura);

        MensagemSegura msg = ProtocoloSeguro.criarMensagem(
                MensagemSegura.TipoOperacao.CRIAR_LEILAO,
                item,
                publicKeyServidor,
                userName
        );

        enviarMensagemSimples(msg);*/
    }

    /*##############################################
    Listar Ativos
    ##############################################*/
    private void listarAtivos() throws Exception {
        /*MensagemSegura msg = ProtocoloSeguro.criarMensagem(
                MensagemSegura.TipoOperacao.LISTAR_LEILOES_ATIVOS,
                "listar",
                publicKeyServidor,
                userName
        );

        enviarMensagemSimples(msg);*/
    }

    /*##################################################
    * Lance
    * #####################################################*/
    private void fazerLance() throws Exception {
        System.out.println("ID do item: ");
        String id = scanner.nextLine();

        System.out.println ("Valor: ");
        double valor = Double.parseDouble (scanner.nextLine());

        //Lance lance = new Lance (id, userName, valor);
        /*byte[] sig = CryptoManager.sign(lance.getDadosParaAssinatura(), keyPair.getPrivate());
        lance.setAssinaturaUsuario(sig);

        MensagemSegura msg = ProtocoloSeguro.criarMensagem(
                MensagemSegura.TipoOperacao.FAZER_LANCE,
                lance,
                publicKeyServidor,
                userName
        );

        enviarMensagemSimples(msg); */
    }

    /*################################################################
    Encerrados
     #################################################################*/
    private void listarEncerrados () throws Exception {
        /*MensagemSegura msg = ProtocoloSeguro.criarMensagem(
                MensagemSegura.TipoOperacao.LISTAR_LEILOES_ENCERRADOS,
                "listar",
                publicKeyServidor,
                userName
        );

        enviarMensagemSimples(msg);*/
    }

    /*##############################################################
     ##################### Envio /Rececão#####################################
     ###############################################################*/
    private void enviarMensagemSimples (MensagemSegura msg){
        try (Socket socket = new Socket (HOST, PORTA);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            //para serializar a mensagem
            String encode = ProtocoloSeguro.serializarMensagem(msg);
            out.println(encoded);
            out.flush();

            // espera uma resposta por linha
            String resposta = in.readLine();
            if (resposta ==null) {
                System.out.println (" O servidor não respondeu.");
                return;
            }

            MensagemSegura msgResp = ProtocoloSeguro.deserializarMensagem(resposta);
            Object conteudo = ProtocoloSeguro.processarMensagem(msgResp, keyPair.getPrivate());

            System.out.println ("\n Resposta do servidor:");
            System.out.println(conteudo);
        } catch (Exception e) {
            System.err.println("Erro na comunicação:" /*+.getMessage()*/);
        }
    }
}
