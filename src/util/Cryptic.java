package util;

import java.security.MessageDigest;
import java.util.Arrays;

public class Cryptic {
    public static byte[] deriveTestIv16(byte[] array128){
        try {
            MessageDigest sha256 = MessageDigest.getInstance(   "SHA-256");
            byte[] hash = sha256.digest(array128);
            return Arrays.copyOf(hash, 16);
        } catch (Exception e) {
            Log.error(e);
        }

        return null;
    }
}
