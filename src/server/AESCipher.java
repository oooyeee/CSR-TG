package server;

import java.nio.ByteBuffer;
import javax.crypto.Cipher; // https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import util.Cryptic;
import util.Log;

public class AESCipher {

    private SecretKeySpec key;
    private IvParameterSpec iv;

    public AESCipher(byte[] key, byte[] iv) {
        this.key = new SecretKeySpec(Cryptic.deriveTestIv16(key), "AES"); // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher
        this.iv = new IvParameterSpec(Cryptic.deriveTestIv16(iv));
    }

    public void encrypt(ByteBuffer data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // TODO, implement HKDF to derive keys and ivs
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] plain = new byte[data.remaining()];
            data.get(plain);
            data.clear(); // same as rewind + limit(capacity)
            byte[] encrypted = cipher.doFinal(plain);
            if (encrypted.length > data.capacity()) {
                Log.error("Buffer too small for encrypted data: " + encrypted.length); // somewhere after, program will throw exception
            }

            data.put(encrypted);
            data.flip();

        } catch (Exception e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void decrypt(ByteBuffer data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Log.debug(":: iv length: " + iv.getIV().length);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] encrypted = new byte[data.remaining()];
            data.get(encrypted);
            data.clear();
            byte[] decrypted = cipher.doFinal(encrypted); // Bad padding exception
        
            data.put(decrypted);
            data.flip();
            
            // System.exit(1); // this line is not executed, exception happens before
        } catch (Exception e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
