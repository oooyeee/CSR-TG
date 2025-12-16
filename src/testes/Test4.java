package testes;

import core.AppLeiloes;
import interfaces.IApp;
import server.Constants;

public class Test4 {
    public static void main(String[] args) {
        System.out.println("Test4 running...");

        IApp leiloesServer = new AppLeiloes("Servidor de Leiloes");

        leiloesServer.start(Constants.leiloesPort);
    }
}
