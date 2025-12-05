
import java.nio.ByteBuffer;
import java.util.function.Function;
// import java.nio.charset.StandardCharsets;
import servidor.Frame;
import servidor.FrameValidator;
import servidor.ServidorBase;
import util.EventEmitter;

public class Test {

    public static void main(String[] args) {
        ServidorBase server = new ServidorBase(1777, 2);

        EventEmitter<Frame> ee = new EventEmitter<Frame>();

        server.init();

        server.onData((session, readBuffer) -> {
            System.out.println("=== === ===");
            int remaining = readBuffer.remaining();
            
            byte[] bytes = new byte[remaining];
            readBuffer.duplicate().get(bytes);
            // System.out.println(Functions.bytesToHex(bytes));
            // for (byte b : bytes) {
            //     System.out.print(String.valueOf(b) + "_");
            // }
            // System.out.println();

            Frame frame = Frame.toFrame(readBuffer);

            if (frame.type == Frame.Types.BYTES.ordinal()) {
                System.out.println("GOT BYTES FRAME!");
            } else if (frame.type == Frame.Types.STRING.ordinal()) {
                System.out.println("GOT STRING FRAME!");
            } else if (frame.type == Frame.Types.FILE.ordinal()) {
                System.out.println("GOT FILE FRAME!");
            }

            // System.out.println("ORdinals: " + Frame.Types.BYTES.ordinal());
            // System.out.println("ORdinals: " + Frame.Types.STRING.ordinal());
            // System.out.println("ORdinals: " + Frame.Types.FILE.ordinal());
            System.out.println("Frame type: " + frame.type);
            System.out.println("Frame length: " + frame.length);
            System.out.println(Frame.toPrettyString(frame));

        });

        server.listen();
    }

}
