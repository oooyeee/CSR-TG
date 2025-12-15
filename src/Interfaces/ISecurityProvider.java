package interfaces;

import java.nio.ByteBuffer;

public interface ISecurityProvider {
    public boolean handle_send_hanshake(IClientSession session);
    public boolean handle_receive_handshake(IClientSession session, ByteBuffer data);
    public void encode(IClientSession session, ByteBuffer data); // modifies buffer
    public void decode(IClientSession session, ByteBuffer data); // modifies buffer
    public boolean removeCipher(String clientID);
}
