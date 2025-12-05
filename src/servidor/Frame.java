package servidor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import interfaces.ISecureCipher;
import util.Functions;

public class Frame {

    public int type;
    public int length;
    public byte[] data;

    private static int TWO_INTs_SIZE = 8;
    private static int MAX_DATA_SIZE = 10240000; // 10MegaBytes

    public static enum Types {
        SECURE, // should upgrade to secure channel (start exchange sequence)
        BYTES,
        STRING,
        FILE
    }

    public static boolean isSecureFrame(Frame frame){
        if(frame.type != Types.SECURE.ordinal()) return false;

        // if(frame.length != 1) return false;

        //  // only 7 states in ISecureCipher types, from 0 to 6, enough for 1 byte
        // if (frame.data[0] > ISecureCipher.HandshakeStages.values().length) return false;

        return true;
    }


    public static String getDataString(Frame frame) {
        String data = new String(frame.data, StandardCharsets.UTF_8);
        return data;
    }

    public static String toPrettyString(Frame frame) {
        if (frame == null) {
            return "";
        }

        String str = String.format("%3S", Integer.toString(frame.type)).replace(' ', '0');

        str += "_" + String.format("%3S", Integer.toString(frame.length)).replace(' ', '0');

        str += "_x|" + Functions.bytesToHex(frame.data, "|").toUpperCase();

        return str;
    }

    public static boolean isFrameLike(ByteBuffer bb) {
        if (bb.remaining() < TWO_INTs_SIZE) return false;

        int type = bb.duplicate().getInt();

        
        boolean isValidType = false;
        for (Types t : Types.values()) {
            if (t.ordinal() == type) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) return false;
        
        int length = bb.duplicate().getInt();
        if(length > MAX_DATA_SIZE) return false;

        return true;
    }

    public static byte[] toByteArray(Frame frame) {
        ByteBuffer buffer = ByteBuffer.allocate(frame.length + 8);
        buffer.putInt(frame.type);
        buffer.putInt(frame.length);
        buffer.put(frame.data);
        return buffer.array();
    }

    // TODO, refactor maybe
    public static Frame toFrame(ByteBuffer buf) {
        int buffer_length = buf.remaining();
        if (buffer_length < TWO_INTs_SIZE) {
            return null; // must have at least: 4 for int type + 4 for int length, data can be 0 bytes
        }
        // byte[] bytes = new byte[remaining];
        // buf.duplicate().get(bytes);

        // ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ByteBuffer buffer = buf.duplicate();
        // System.out.println("in the toFrame");
        // for (byte b : bytes) {
        // System.out.print(String.valueOf(b) + "_");
        // }
        // System.out.println();

        // System.out.println("checked buffer length: " + buffer_length);
        int type = buffer.getInt(); // gets first 4 bytes
        int length = buffer.getInt(); // gets second 4 bytes

        // System.out.println("found frame Type: " + type);
        // System.out.println("found frame data Length: " + length);

        int data_size = (length <= buffer.capacity() - TWO_INTs_SIZE) ? length : buffer.capacity() - TWO_INTs_SIZE;

        Frame frame = new Frame(type, length, data_size);
        for (int i = 0; i < data_size; i++) {
            try {
                frame.data[i] = buffer.get(); // gets 1 byte and advances automatically
            } catch (Exception e) {// BufferUnderflowException
                System.err.println("Error filling Frame data: BufferUnderflowException");
                break;
            }
        }
        return frame;
    }

    // TODO fix max sise (must be less than session buffer)
    public static Frame toFrame(String str) {
        byte[] bytes = str.getBytes();
        System.out.println("string bytes: " + bytes.length);
        Frame frame = new Frame(Types.STRING.ordinal(), bytes.length, bytes.length);
        frame.data = bytes;
        return frame;
    }

    public Frame() {
        type = 0;
        length = 0;
        this.data = null;
    }

    public Frame(int dataLength) {
        type = 0;
        length = 0;
        data = new byte[dataLength];
    }

    public Frame(int type, int length, int dataLength) {
        this.type = type;
        this.length = length;
        this.data = new byte[dataLength];
    }

    public Frame(int type, int length, byte[] data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }
}
