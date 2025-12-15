package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Certificates {

    public static X509Certificate loadCertificate(String path) {
        X509Certificate cert;
        try (InputStream in = new FileInputStream(path)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(in);
        } catch (Exception e) {
            Log.error(e);
            e.printStackTrace();
            return null;
        }
        return cert;
    }

    public static PrivateKey loadEncryptedPrivateKey(String path, char[] password) throws Exception {
        // Read and decode PEM
        String pem = new String(Files.readAllBytes(Paths.get(path)));
        pem = pem.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                .replace("-----END ENCRYPTED PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encryptedKeyBytes = Base64.getDecoder().decode(pem);

        // Parse encrypted PKCS#8 key
        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(encryptedKeyBytes);

        // Derive key from password
        SecretKeyFactory skf = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        Key pbeKey = skf.generateSecret(pbeKeySpec);

        // Decrypt private key
        PKCS8EncodedKeySpec keySpec = encryptedPrivateKeyInfo.getKeySpec(pbeKey);

        // Generate private key (RSA assumed)
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean verifySignature(
            byte[] data,
            byte[] signatureBytes,
            String certificatePath) throws Exception {

        // Load X.509 certificate
        X509Certificate certificate;
        try (FileInputStream fis = new FileInputStream(certificatePath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(fis);
        }

        // Initialize verifier
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());
        signature.update(data);

        // Verify signature
        return signature.verify(signatureBytes);
    }

    public static boolean writeBytesToFile(byte[] signatureBytes, String outputPath) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(signatureBytes);
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        return true;
    }
}
