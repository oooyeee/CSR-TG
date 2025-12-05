
import java.nio.ByteBuffer;
// import java.nio.charset.StandardCharsets;
import servidor.Frame;
import util.Functions;

public class Test2 {

    public static void main(String[] args) {

        int type = 20;
        int length = 5555;
        // char data[] = {'h','e','l','o','w','o','r','l','d','!'};

        String data = "hheloworldd";
        byte byteData[] = data.getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + data.length());
        buffer.putInt(type);
        buffer.putInt(length);
        buffer.put(byteData);

        byte[] byteFrame = buffer.array();

        System.out.println("Hello main");
        System.out.println("bytes in int: " + Integer.BYTES);
        System.out.println("bytes in char: " + Character.BYTES);
        System.out.println(Functions.bytesToHex(byteData).toUpperCase());
        System.out.println("byteData length: " + byteData.length);
        System.out.println("Buffer length: " + buffer.duplicate().compact().limit());
        System.out.println("Buffer capacity: " + ByteBuffer.allocate(123).capacity());
        // System.out.println(message.substring(4, length));

        for (byte b : byteFrame) {
            System.out.print(String.valueOf(b) + "_");
        }

        System.out.println("\n");
        System.out.println(new String(byteFrame, 8, data.length()));

        Frame frm = Frame.toFrame(buffer);

        System.out.println("FRAME: " + Frame.toPrettyString(frm));
        System.out.println("FRAME data: " + Frame.getDataString(frm));
        // Frame.getDataString(frm);
        // String asd = Functions.bytesToHex(frm.data, "|");
        // System.out.println("frm.data lencgth: " + frm.data.length);
        // System.out.println("asd: " + asd);
        Functions.bytesToHex(frm.data, "|");

        Frame frame2 = Frame.toFrame("privet");

        System.out.println("Frame data: " + Frame.toPrettyString(frame2));

        System.out.println(frame2.type + " :: " + frame2.length);

        byte[] arr = Frame.toByteArray(frame2);

        System.out.println("message size in bytes: " + arr.length);
        System.out.println(Frame.toPrettyString(frame2));
        System.out.println(Functions.bytesToHex(arr));
    }

}
