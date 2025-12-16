package server;

import java.nio.ByteBuffer;
import java.util.Arrays;

import util.Log;

// TODO: frame has length and data, needs TYPE (handshake or normal)
public class Frame {

    public int type;
    public int length;
    public byte[] data;

    public Frame(int type, int length, byte[] data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }

    public static enum FrameType {
        STRING, // TLV
        NESTED, // array: T_L=n_V=[T1L1V1,T2V2L2,...,TnVnLn]
        HANDSHAKE, // TLV
        NESTED_HANDSHAKE, // array: T_L=n_V=[T1L1V1,T2V2L2,...,TnVnLn]
        BYTES, //TLV
        EXTENDED // mixed
    }

    public static String whatType(Frame frame) {
        switch (frame.type) {
            case 0:
                return "STRING";
            case 1:
                return "NESTED";
            case 2:
                return "HANDSHAKE";
            case 3:
                return "NESTED_HANDSHAKE";
            case 4:
                return "BYTES";
            default:
                return "EXTENDED";
        }
    }

    public static Frame toFrame(ByteBuffer buffer) {
        ByteBuffer buf = buffer.duplicate();
        Frame frame = new Frame(0, 0, null);
        frame.type = buf.getInt();
        frame.length = buf.getInt();
        // if frame type is not NESTED or NESTED_HANDSHAKE, then do this
        if(frame.type != Frame.FrameType.NESTED.ordinal() && frame.type != Frame.FrameType.NESTED_HANDSHAKE.ordinal()) {
            frame.data = new byte[frame.length];
            buf.get(frame.data);
            return frame;
        }
        // if frame type is NESTED or NESTED_HANDSHAKE, then data contains multiple frames
        // reconstruct N frames, where N = frame.length
        for (int i = 0; i < frame.length; i++) {
            // skip 4 bytes, then read next 4 bytes, which is length of data of this [i] frame and position buf accordingly
            buf.position(buf.position() + 4); // skip type
            int len = buf.getInt(); // read length
            buf.position(buf.position() + len); // skip data
        }

        frame.data = new byte[buf.position() - 8]; // total bytes read minus type and length fields
        buf.position(8);
        buf.get(frame.data);

        return frame;
    }

    public static Frame toFrame(String str) {
        Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), str.length(), str.getBytes());
        return frame;
    }


    // public static Frame toFrame(byte[] pureData, int type) {
    //     Frame frame = new Frame(0, 0, null);
    //     frame.type = type;
    //     frame.length = pureData.length;
    //     frame.data = new byte[pureData.length];
    //     System.arraycopy(pureData, 0, frame.data, 0, pureData.length);
    //     return frame;
    // }

    public static ByteBuffer fromFrame(Frame frame) {
        ByteBuffer buf;

        buf = ByteBuffer.allocate(4 + 4 + frame.data.length); // sizeof int x2 + data length

        buf.putInt(frame.type);
        buf.putInt(frame.length);
        buf.put(frame.data);
        buf.flip();
        return buf;
    }

    public static ByteBuffer fromFrame(Frame frame, int setAllocatedSizeInBytes) {
        ByteBuffer buf;

        buf = ByteBuffer.allocate(setAllocatedSizeInBytes); // sizeof int x2 + data length

        buf.putInt(frame.type);
        buf.putInt(frame.length);
        buf.put(frame.data);
        buf.flip();
        return buf;
    }
    
    

    public static boolean isFrame(ByteBuffer buffer) {
        // Need at least 8 bytes for the type and length fields
        if (buffer.remaining() < 8) {
            return false;
        }

        buffer.mark(); // Remember current position
        int type = buffer.getInt(); // Read type
        int length = buffer.getInt(); // Read length

        buffer.reset(); // Restore position

        if (type >= Frame.FrameType.values().length) {
            return false;
        }

        // Length must be >= 0 and full frame must fit in buffer
        return length >= 0 && buffer.remaining() >= (4 + length);
    }

    public static byte[] getBytes(Frame frame) {
        return fromFrame(frame).array();
    }

    @Override
    public String toString() {

        String type = String.valueOf(this.type);
        String length = String.valueOf(this.length);
        // String data = String.valueOf(this.data);

        String[] parts = {
            "Frame {type: " + type,
            "length: " + length,
            "data-length: " + this.data.length + "}"
        };

        return Arrays.toString(parts);
    }

    public String getDataString(){
        if(this.type == Frame.FrameType.STRING.ordinal()){
            return new String(this.data);
        }
        return null;
    }

    public void putIn(ByteBuffer buffer) {
        buffer.clear();
        buffer.putInt(this.type);
        buffer.putInt(this.length);
        buffer.put(this.data);
        buffer.flip();
    }
}
