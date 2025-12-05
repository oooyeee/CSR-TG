import Cliente.Cliente;

public class NioClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1777;

        Cliente client = new Cliente(host, port);
        client.start();
    }
}
