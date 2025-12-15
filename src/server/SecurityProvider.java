package server;

import interfaces.IClientSession;
import interfaces.ISecurityProvider;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import util.Log;

public class SecurityProvider implements ISecurityProvider {

    public final Map<String, AESCipher> aesCiphers;
    public final Map<String, DHCipher> dhCiphers1;
    public final Map<String, DHCipher> dhCiphers2;

    public SecurityProvider() {
        this.aesCiphers = new HashMap<>();
        this.dhCiphers1 = new HashMap<>();
        this.dhCiphers2 = new HashMap<>();
    }

    @Override
    public void encode(IClientSession session, ByteBuffer data) {
        aesCiphers.get(session.clientID()).encrypt(data);
    }

    @Override
    public void decode(IClientSession session, ByteBuffer data) {
        aesCiphers.get(session.clientID()).decrypt(data);
    }

    @Override
    public boolean handle_send_hanshake(IClientSession session) {
        Log.debug(":: handshaking");
        session.setSecureState(IClientSession.SessionState.NEED_HANDSHAKE);
        // phase 1
        if (this.dhCiphers1.get(session.clientID()) == null) {
            Log.debug(":: phase 1");
            DHCipher dh1 = new DHCipher();

            byte[] payload = dh1.getRequestPayload();
            this.dhCiphers1.put(session.clientID(), dh1);
            try {
                Log.debug(":: phase 1 finishing, sending " + payload.length + " bytes of payload");
                session.write(ByteBuffer.wrap(payload));
            } catch (Exception e) {
                Log.error(e);
            }
            return false;
        }

        // phase 2
        if (this.dhCiphers2.get(session.clientID()) == null) {
            Log.debug(":: phase 2");
            DHCipher dh2 = new DHCipher();

            byte[] payload = dh2.getRequestPayload();
            this.dhCiphers2.put(session.clientID(), dh2);

            try {
                Log.debug(":: phase 2 finishing, sending " + payload.length + " bytes of payload");
                session.write(ByteBuffer.wrap(payload));
            } catch (Exception e) {
                Log.error(e);
            }
            return false;
        }

        // phase 3
        if (this.aesCiphers.get(session.clientID()) == null) {
            Log.debug(":: phase 3");
            DHCipher dh1 = this.dhCiphers1.get(session.clientID());
            DHCipher dh2 = this.dhCiphers2.get(session.clientID());
            this.aesCiphers.put(session.clientID(), new AESCipher(dh1.result, dh2.result));
            session.setSecureState(IClientSession.SessionState.SECURE);
            Log.debug(":: phase 3 finished, AES DONE ::");
            return true;
        }

        Log.rare("!! something went wrong in handle_send_hanshake");
        return false;
    }

    @Override
    public boolean handle_receive_handshake(IClientSession session, ByteBuffer data) {
        DHCipher dh1 = this.dhCiphers1.get(session.clientID());
        DHCipher dh2 = this.dhCiphers2.get(session.clientID());

        if (dh1 == null) {
            Log.error(":: in handle_receive_handshake, could not find dh1, but it must exist already ::");
            return false;
        }

        if (dh2 == null) {
            // stage 1 - response
            Frame frame = Frame.toFrame(data);
            dh1.generateResult(frame.data);
        } else {
            // stage 2 - response
            Frame frame = Frame.toFrame(data);
            dh2.generateResult(frame.data);
            // dh1 and dh2 are now complete, we can generate AES cipher in the next step with handle_send_hanshake
        }

        return true;
    }

    @Override
    public boolean removeCipher(String clientID) {
        return (this.aesCiphers.remove(clientID) != null);
    }

}
