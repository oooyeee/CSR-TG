package server;

import util.Conversions;

public class Constants {
    public static int bufferSizeInKB = 32;
    public static int bufferSize = bufferSizeInKB * 1024;

    public static byte[] testKey = Conversions.hexStringToBytes("9D5B039267538685681CB35165927F8F9D7E0F2BD8F1EDC5EDB491177C76216C");

    public static byte[] testIv = "1234567890ABCDEF".getBytes();

    public static final String rootCaPath = "res/rootCA.crt";
    public static final String rootCaKeyPath = "res/rootCA.key";
    public static final String rootPassword = "secret";

    public static final String signerCertPath = "res/signer.crt";
    public static final String signerKeyPath = "res/signer.key";
    public static final String signerPassword = "sign";

    public static final String leiloesCertPath = "res/leiloes.crt";
    public static final String leiloesKeyPath = "res/leiloes.key";
    public static final String leiloesPassword = "pass";

    public static final String joaoCertPath = "res/joao.crt";
    public static final String joaoKeyPath = "res/joao.key";
    public static final String joaoPassword = "password";

    public static final String mariaCertPath = "res/maria.crt";
    public static final String mariaKeyPath = "res/maria.key";
    public static final String mariaPassword = "password";

    public static final String horacioCertPath = "res/horacio.crt";
    public static final String horacioKeyPath = "res/horacio.key";
    public static final String horacioPassword = "password";

    public static final String conceicaoCertPath = "res/conceicao.crt";
    public static final String conceicaoKeyPath = "res/conceicao.key";
    public static final String conceicaoPassword = "password";

    public static final int leiloesPort = 1777;
    public static final int signerPort = 1888;
}
