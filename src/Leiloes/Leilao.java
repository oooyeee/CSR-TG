package Leiloes;

import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.tsp.*;
import org.bouncycastle.util.Store;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
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
        // ----- Cripto utilitária -----
    public static KeyPair gerarChaves() throws Exception {
        KeyPairGenerator k = KeyPairGenerator.getInstance("RSA");
        k.initialize(2048);
        return k.generateKeyPair();
    }


    public static byte[] assinar(PrivateKey priv, String dados) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(priv);
        s.update(dados.getBytes(StandardCharsets.UTF_8));
        return s.sign();
    }


    public static boolean verificar(PublicKey pub, String dados, byte[] assinatura) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(pub);
        s.update(dados.getBytes(StandardCharsets.UTF_8));
        return s.verify(assinatura);
    }


     public static byte[] sha256(byte[] in) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(in);
    }


// ----- Timestamp Simplificado -----
    public static byte[] obterTimestamp(String url, byte[] hash) throws Exception {
        TimeStampRequest req = new TimeStampRequestGenerator().generate(TSPAlgorithms.SHA256, hash);
        byte[] reqBytes = req.getEncoded();


        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setDoOutput(true);
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/timestamp-query");
        c.getOutputStream().write(reqBytes);
        return c.getInputStream().readAllBytes();
    }


    public static boolean verificarTimestamp(byte[] tsBytes, byte[] hashOriginal) throws Exception {
        TimeStampToken tok = new TimeStampResponse(tsBytes).getTimeStampToken();
        if (tok == null) 
            return false;
            // Confere hash
            if (!Arrays.equals(hashOriginal, tok.getTimeStampInfo().getMessageImprintDigest()))
                return false;
        // Verifica assinatura da TSA
        Store<X509CertificateHolder> certs = tok.getCertificates();
        var matches = certs.getMatches(tok.getSID());
        X509CertificateHolder tsaCert = matches.iterator().next();
        tok.validate(new JcaSimpleSignerInfoVerifierBuilder().build(tsaCert));
    return true;
    }
}



