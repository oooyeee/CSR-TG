package testes;

import core.AppCA;
import core.AppFactory;
import interfaces.IApp;
import server.Constants;

public class Test5 {
        public static void main(String[] args) {
        System.out.println("Test5 running...");

        AppCA signer = new AppCA("Servidor de Authoridade");

        signer.start(Constants.signerPort);
    }
}
