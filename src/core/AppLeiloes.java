package core;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import java.util.Base64;

import interfaces.IApp;
import interfaces.IClientSession;
import interfaces.IClientSession.WriteState;
import server.CommandRouter;
import server.Constants;
import server.Frame;
import server.SecurityProvider;
import server.Server;
import server.Validator;
import util.Certificates;
import util.Conversions;
import util.Log;

public class AppLeiloes implements IApp {
    private String name;
    private X509Certificate leiloesCert;
    private PrivateKey pk;
    private ParallelWorker parallelWorker;

    public AppLeiloes(String name) {
        this.name = name;

        try {
            this.parallelWorker = new ParallelWorker("127.0.0.1", Constants.signerPort);

            String signerCertPath = Constants.signerCertPath;
            String signerKeyPath = Constants.signerKeyPath;

            this.leiloesCert = Certificates.loadCertificate(signerCertPath);
            if (this.leiloesCert == null) {
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

        Repositorio<User> users = new Repositorio<User>();
        Repositorio<Leilao> leiloes = new Repositorio<Leilao>();
        Repositorio<Lance> lances = new Repositorio<Lance>();

        server.configure((options) -> {

            SecurityProvider sp = new SecurityProvider();
            Validator validator = new Validator();
            CommandRouter router = new CommandRouter();

            router.onCommand("SIGN", (session, msg) -> {
                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), 0, null);
                String message = "SIGN " + msg;
                frame.data = message.getBytes();
                frame.length = frame.data.length;

                parallelWorker.submit(Frame.getBytes(frame)).thenAccept((req) -> {
                    ByteBuffer buff = ByteBuffer.allocate(Constants.bufferSize);
                    buff.clear();
                    buff.put(req);
                    buff.flip();
                    Frame promisedFrame = Frame.toFrame(buff);

                    router.write(session, promisedFrame);
                });
            });

            router.onCommand("_TRUST_", (session, msg) -> {
                try {
                    byte[] certBytes = this.leiloesCert.getEncoded();
                    String certBytesEncodedBase64 = new String(Base64.getEncoder().encode(certBytes));
                    byte[] nonceBytes = msg.getBytes();
                    byte[] sigBytes;
                    sigBytes = Certificates.sign(nonceBytes, pk);

                    String sigBase64 = new String(Base64.getEncoder().encode(sigBytes));
                    String data = "TRUST " + certBytesEncodedBase64 + " " + sigBase64;

                    Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), 0, null);
                    frame.data = data.getBytes();
                    frame.length = frame.data.length;

                    ByteBuffer buffer = session.getReadBuffer();
                    frame.putIn(buffer);

                    router.write(session, buffer);
                } catch (Exception e) {
                    Log.error(":: could not sign nonce");
                    Log.error(e);
                    e.printStackTrace();
                    return;
                }
            });

            CommandRouter getRouter = new CommandRouter();
            getRouter.onCommand("leiloes", (session, msg) -> {
                Log.debug(":: /GET/leiloes: " + msg);
                String listaLeiloes = "LISTA: \n";

                for (Leilao leilao : leiloes.getAll()) {
                    listaLeiloes += leilao.toString() + "\n";
                }

                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), listaLeiloes.length(), listaLeiloes.getBytes());
                Log.debug(frame.getDataString());
                ByteBuffer buffer = session.getReadBuffer();
                frame.putIn(buffer);
                getRouter.write(session, buffer);
            });

            getRouter.onCommand("leilao", (session, msg) -> {
                int leilaoID;
                try {
                    leilaoID = Integer.valueOf(msg);
                    Leilao leilao = leiloes.get(leilaoID);
                    if (leilao == null) {
                        getRouter.write(session, Frame.toFrame("leilao " + leilaoID + " nao existe"));
                        return;
                    }

                    getRouter.write(session, Frame.toFrame(leilao.toString()));
                } catch (Exception e) {
                    Log.error(e);
                    getRouter.writeError(session, "(/get/leilao) internal router error");
                }
            });


            router.onCommand("GET", (session, msg) -> {

                // router.write(session, buffer);

                getRouter.parseCommand(session, msg);
            });

            CommandRouter postRouter = new CommandRouter();
            postRouter.onCommand("leiloes", (session, msg) -> {
                Log.debug(":: /POST/leiloes: " + msg);
                Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), msg.length(), msg.getBytes());
                Log.debug(frame.getDataString());
                ByteBuffer buffer = session.getReadBuffer();
                frame.putIn(buffer);
                postRouter.write(session, buffer);
            });

            router.onCommand("POST", (session, msg) -> {


                postRouter.parseCommand(session, msg);
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
                    } else { // secure and ready

                        return true;
                    }
                }

                return false;
            });

            // options.addToReadChain((session, data) -> {
            //     Frame frame = Frame.toFrame(data);

            //     if (frame.type == Frame.FrameType.STRING.ordinal()) {
            //         Log.debug("[" + session.clientID() + "] sent " + data.remaining() + " bytes, msg: "+ frame.getDataString());
            //     } else {
            //         Log.debug("[" + session.clientID() + "] sent " +
            //                 Frame.FrameType.values()[frame.type].name()
            //                 + " with " + frame.data.length + " bytes of data");
            //     }
            //     return true;
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

        server.listen(port, () -> {
            Log.always(server.name() + " started at port " + port);
        });

    }

}
