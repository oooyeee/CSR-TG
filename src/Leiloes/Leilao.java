package Leiloes;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

    public class Leilao {
        private String id;
        private String descricao;
        private Date dataConclusao;
        private double valorMinimo;
        private String vendedor;
        private Date dataCriacao;
        private static final long serialVersionUID = 1L;
        private byte[] assinaturaVendedor;
        byte[] assinaturaVendedor;
        byte[] timestampToken;
        
        public ItemLeilao(String descricao, Date dataConclusao, double valorMinimo, String vendedor) {
            this.id = UUID.randomUUID().toString();
            this.descricao = descricao;
            this.dataConclusao = dataConclusao;
            this.valorMinimo = valorMinimo;
            this.vendedor = vendedor;
            this.dataCriacao = new Date();
        }

        public String getId() {
            return id;
        }

        public String getDescricao() {
            return descricao;
        }

        public Date getDataConclusao() {
            return dataConclusao;
        }

        public double getValorMinimo() {
            return valorMinimo;
        }

        public String getVendedor() {
            return vendedor;
        }

        public Date getDataCriacao() {
            return dataCriacao;
        }

         public byte[] getAssinaturaVendedor() {
        return assinaturaVendedor;
        }

        public void setAssinaturaVendedor(byte[] assinaturaVendedor) {
        this.assinaturaVendedor = assinaturaVendedor;
        }
        
        /**
         * Retorna os dados que devem ser assinados para garantir não-repúdio
         */
        public String getDadosParaAssinatura() {
            return id + descricao + dataConclusao.getTime() + valorMinimo + vendedor + dataCriacao.getTime();
        }

        public boolean isEncerrado() {
            return new Date().after(dataConclusao);
        }

        @Override
        public String toString() {
            return "ItemLeilao{" +
                    "id='" + id + '\'' +
                    ", descricao='" + descricao + '\'' +
                    ", dataConclusao=" + dataConclusao +
                    ", valorMinimo=" + valorMinimo +
                    ", vendedor='" + vendedor + '\'' +
                    '}';
        }
        
        // gera hash SHA-256
    public static byte[] sha256(byte[] in) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(in);
    }

    // assina dados
    public static byte[] sign(PrivateKey pk, byte[] dados) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(pk); s.update(dados); return s.sign();
    }

    public static boolean verify(PublicKey pub, byte[] dados, byte[] sig) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(pub); s.update(dados); return s.verify(sig);
    }

    // carimbo de tempo simples embutido
    public static byte[] createTimestamp(PrivateKey tsaKey, X509Certificate tsaCert, byte[] hash) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(hash);
        long t = System.currentTimeMillis();
        baos.write(ByteBuffer.allocate(8).putLong(t).array());
        byte[] data = baos.toByteArray();
        byte[] sig = sign(tsaKey, data);
        ByteArrayOutputStream token = new ByteArrayOutputStream();
        token.write(data); token.write(sig);
        return token.toByteArray();
    }

    public static boolean verifyTimestamp(byte[] token, byte[] hash, X509Certificate tsaCert) throws Exception {
        byte[] data = Arrays.copyOf(token, hash.length + 8);
        byte[] sig = Arrays.copyOfRange(token, hash.length + 8, token.length);
        if (!Arrays.equals(hash, Arrays.copyOfRange(data, 0, hash.length))) return false;
        return verify(tsaCert.getPublicKey(), data, sig);
    }

    public static void main(String[] args) throws Exception {
        // Exemplo com JKS
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("tsa_keystore.jks"), "changeit".toCharArray());
        PrivateKey tsaKey = (PrivateKey) ks.getKey("tsa", "changeit".toCharArray());
        X509Certificate tsaCert = (X509Certificate) ks.getCertificate("tsa");

        Leilao l = new Leilao("Quadro", new Date(System.currentTimeMillis() + 86400000), 500, "Joao");
        KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        l.assinaturaVendedor = sign(kp.getPrivate(), l.dados().getBytes(StandardCharsets.UTF_8));
        System.out.println("Assinatura vendedor válida? " + verify(kp.getPublic(), l.dados().getBytes(StandardCharsets.UTF_8), l.assinaturaVendedor));

        byte[] hash = sha256(l.dados().getBytes(StandardCharsets.UTF_8));
        l.timestampToken = createTimestamp(tsaKey, tsaCert, hash);
        System.out.println("Timestamp válido? " + verifyTimestamp(l.timestampToken, hash, tsaCert));
    }
}





