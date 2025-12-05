package util;

public class Functions {

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes, String separator) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(separator);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    // returns an array of 4 bytes
    public static byte[] intToByteArray_bigEndian(int value) {
        return new byte[]{
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }

    public static byte[] stringToByteArray(String str){
        return str.getBytes();
    }

    // public static ByteBuffer 
}
