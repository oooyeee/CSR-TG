package testes;

import interfaces.IClientSession;
import interfaces.IClientSession.WriteState;

import java.nio.ByteBuffer;
import server.CommandRouter;
import server.Constants;
import server.Frame;
import server.SecurityProvider;
import server.Server;
import server.Validator;
import util.Log;

public class Test {

    public static void main(String[] args) {
        int port = 1777;
        // Log.setLevel(4, true);
        // Log.suppressLevels();
        Log.toggleDateFormat();
        // Log.toggleReverseOrder();

        Server server = new Server("Leiloes Server!");

        server.configure((options) -> {

            SecurityProvider sp = new SecurityProvider();
            Validator validator = new Validator();
            CommandRouter router = new CommandRouter();

            router.onCommand("GET", (session, msg) -> {
                Log.debug(":: /GET: " + msg);

                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), msg.length(), msg.getBytes());

                Log.debug(frame.toString());

                router.write(session, frame);
            });

            router.onCommand("POST", (session, msg) -> {
                Log.debug(":: /POST: " + msg);
                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), msg.length(), msg.getBytes());

                Log.debug(frame.toString());
                // ByteBuffer buffer = ByteBuffer.allocate(Constants.bufferSize);
                ByteBuffer buffer = session.getReadBuffer();
                frame.putIn(buffer);

                router.write(session, buffer);
            });

            options.addToHandshakeChain((session, data) -> { // runs once
                return sp.handle_send_hanshake(session);
            });

            options.addToReadChain((session, data) -> {
                if (session.getSecureState() == IClientSession.SessionState.NEED_HANDSHAKE) {
                    if (!sp.handle_receive_handshake(session, data)) {
                        Log.rare(":: could not handle received handshake");
                        return false;
                    }
                    return sp.handle_send_hanshake(session); // after 2 dh exchanges, at step 3 this generates AES and sets session to secure
                }
                sp.decode(session, data);
                return true;
            });

            options.addToReadChain((session, data) -> {
                boolean result = validator.verify(session, data);

                if (!result) {
                    Log.rare(":: incoming data is unlikely a frame ::");
                    return false;
                }

                return result;
            });

            options.addToReadChain((session, data) -> {
                if (session.getSecureState() == IClientSession.SessionState.SECURE) {
                    if (session.getWriteState() != WriteState.READY) {
                        session.setWriteState(IClientSession.WriteState.READY);
                        return false;
                    } else { // secure and ready, continue chain

                        return true;
                    }
                }

                return false;
            });

            options.addToReadChain((session, data) -> {
                Frame frame = Frame.toFrame(data);

                if (frame.type == Frame.FrameType.STRING.ordinal()) {
                    Log.debug("[" + session.clientID() + "]: " + frame.getDataString());
                } else {
                    Log.debug("[" + session.clientID() + "] sent " + Frame.FrameType.values()[frame.type].name() + " with " + frame.data.length
                            + " bytes of data");
                }
                return true;
            });

            // options.addToReadChain((session, data) -> {
            // return echoHandler(session, data, sp);
            // });

            /*********************************
             * COMMANDS
             *********************************/
            options.useRouter(router); // last

            options.addToWriteChain((session, data) -> {
                if (session.getSecureState() != IClientSession.SessionState.SECURE) {
                    return true;
                }

                sp.encode(session, data);

                return true;
            });

            options.addToWriteChain((session, data) -> { // DATA IS ENCODED!
                ByteBuffer shortenedData = ByteBuffer.allocate(data.remaining());
                shortenedData.put(data.duplicate());
                shortenedData.flip();
                data.clear();
                data.put(shortenedData);
                data.flip();

                return true;
            });

        });

        server.listen(1777, () -> {
            Log.always(server.name() + " started at port " + port);
        });

    }

    public static boolean echoHandler(IClientSession session, ByteBuffer data, SecurityProvider sp) {

        Frame frame = Frame.toFrame(data);

        Log.always("[" + session.clientID() + "]: " + frame.getDataString());

        try {
            session.write(data);
        } catch (Exception e) {
            Log.always(e);
        }

        return false;
    }

    public static void testLogs() {
        Log.info(":: info");
        Log.warn(":: warn");
        Log.error(":: error");
        Log.debug(":: debug");
        Log.rare(":: rare");
        Log.always(":: always");
    }
}
