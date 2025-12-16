package core;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import interfaces.IApp;
import interfaces.IClientSession;
import interfaces.IClientSession.WriteState;
import server.CommandRouter;
import server.Frame;
import server.SecurityProvider;
import server.Server;
import server.Validator;
import util.Log;
import util.Certificates;
import server.Constants;

public class AppCA implements IApp {

    private String name;
    private X509Certificate signerCert;
    private PrivateKey pk;

    public AppCA(String name) {
        this.name = name;

        try {
            String signerCertPath = Constants.signerCertPath;
            String signerKeyPath = Constants.signerKeyPath;

            this.signerCert = Certificates.loadCertificate(signerCertPath);
            if (this.signerCert == null) {
                throw new Exception("Could not load signer certificate");
            }
            this.pk = Certificates.loadEncryptedPrivateKey(signerKeyPath, "sign".toCharArray());

        } catch (Exception e) {
            Log.error(":: Critical error initializing " + this.name + " server ::");
            Log.error(e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void start(int port) {
        Log.toggleDateFormat();
        Server server = new Server(name);

        server.configure((options) -> {
            SecurityProvider sp = new SecurityProvider();
            Validator validator = new Validator();
            CommandRouter router = new CommandRouter();

            Repositorio<RevokedCert> crl = new Repositorio<>();

            router.onCommand("SIGN", (session, msg) -> {
                Log.debug(":: /SIGN: " + msg);
                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), 0, null);
                String message = "hello from sign, your text: " + msg;
                frame.data = message.getBytes();
                frame.length = frame.data.length;
                Log.debug(frame.toString());
                // ByteBuffer buffer = ByteBuffer.allocate(Constants.bufferSize);
                ByteBuffer buffer = session.getReadBuffer();
                frame.putIn(buffer);

                router.write(session, buffer);
            });

            router.onCommand("CRL", (session, msg) -> {
                Log.debug(":: /CRL: " + msg);
                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), 0, null);
                frame.data = "hello from sign".getBytes();
                frame.length = frame.data.length;
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
                    return sp.handle_send_hanshake(session);
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
                    } else { // secure and ready

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
                    Log.debug("[" + session.clientID() + "] sent " + Frame.FrameType.values()[frame.type].name()
                            + " with " + frame.data.length + " bytes of data");
                }
                return true;
            });

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

        server.listen(port, () -> {
            Log.always(server.name() + " started at port " + port);
        });
    }
}
