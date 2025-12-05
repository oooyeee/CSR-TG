package servidor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import interfaces.IClientSession;
import interfaces.ICommandRouter;
import interfaces.ISecureCipher;

import interfaces.IServidorSeguro;
import interfaces.IValidator;
import interfaces.ISecureCipher.HandshakeStages;

import java.util.UUID;

import util.EventEmitter;

public class SecureServer extends ServidorBase implements IServidorSeguro {

    // private EventEmitter<Frame> ee;
    private IValidator validator;
    private Class<? extends ISecureCipher> cipherClass;

    public SecureServer(int port) {
        super(port);
        this.validator = null;
        this.cipherClass = null;
    }

    @Override
    public ServidorBase onAccept(Consumer<IClientSession> callback) {
        super.onAccept((session) -> {

            // validate and rest here
            if (this.cipherClass != null) {
                try {
                    ISecureCipher cipher = cipherClass.getDeclaredConstructor(String.class)
                            .newInstance(UUID.randomUUID().toString());

                    session.setCipherOnce(cipher);

                    String hello = "Hello!\n" + cipher.getCipherCapabilities();

                    session.write(hello);
                    cipher.processHandshake(ByteBuffer.wrap(new byte[] {
                        (byte)0,
                        (byte)0,
                        (byte)0,
                        (byte)0, // frame type = SECURE
                        (byte)0,
                        (byte)0,
                        (byte)0,
                        (byte)1, // frame length = 1 byte
                        (byte)0  // data = cipher stage 0 = initiator_start
                    }), session);
                } catch (Exception e) {
                    System.err.println("Internal server error: Could not instanciate cipher class");
                    System.err.println(e.getMessage());
                    session.disconnect();
                    return;
                }
            }

            System.out.println("In SecureServer onAccept callback, before calling callback");

            callback.accept(session);

            System.out.println("In SecureServer onAccept callback, after calling callback");
        });
        return this;
    }

    @Override
    public SecureServer onData(BiConsumer<IClientSession, ByteBuffer> callback) {
        super.onData((session, data) -> {

            // validate and rest here
            System.out.println("Intersepting onData from client: " + session.clientID());
            ByteBuffer buffer = data.duplicate();

            ISecureCipher cipher = session.getCipher();
            if (session.isSecure()) {
                if (cipher == null) { // must be impossible
                    System.err.println("Session isSecure, but no cipher exist for client: " + session.clientID());
                    return; // TODO, should be an error
                }

                buffer = cipher.decode(buffer);
            }

            if (validator != null && !validator.isValid(buffer)) {
                // if is not valid return
                return;
            }

            if (cipher.processHandshake(buffer, session)) {
                return;
            }

            callback.accept(session, buffer);
        });

        return this;
    }

    @Override
    public ServidorBase onDisconnect(Consumer<IClientSession> callback) {
        super.onDisconnect((session) -> {

            // do stuffi here
            callback.accept(session);
            session.disconnect(); // without this, if implmenented runs in loop
        });
        return this;
    }

    @Override
    public void useValidator(IValidator validator) {
        if (this.isInitiated) {
            return;
        }

        this.validator = validator;
    }

    @Override
    public void useSecureCipherClass(Class<? extends ISecureCipher> secureCipherClass) {
        if (this.isInitiated) {
            return;
        }

        this.cipherClass = secureCipherClass;
    }

    @Override
    public void useCommandRouter(ICommandRouter router) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
