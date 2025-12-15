package util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Conversions {

    public static String byteBufferToString(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.rewind();
        String msg = new String(bytes);
        return msg;
    }

    public static String bufferToHexString(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.rewind();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().trim();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().trim();
    }

    public static byte[] hexStringToBytes(String hexString) {
        String temp = hexString;
        if (temp.endsWith("\n")) {
            temp = temp.substring(0, temp.indexOf("\n") - 1);
        }
        if (temp.length() % 2 != 0) {
            return new byte[0];
        }

        int length = temp.length() / 2;
        byte[] bytes = new byte[length];

        int s = 0;
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) Integer.parseInt(temp.substring(s, s + 2), 16);
            s += 2;
        }

        return bytes;
    }

    public boolean areBuffersEqual(ByteBuffer left, ByteBuffer right) {

        if (left.duplicate().compareTo(right.duplicate()) == 0) {
            return true;
        }

        return false;
    }

    public static LocalDateTime fromLongMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

     public static byte[] getSHA256Hash(byte[] data) {
        byte[] hash;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data);
            hash = digest.digest();
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return hash;
     }
}
