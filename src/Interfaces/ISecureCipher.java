package interfaces;

import java.nio.ByteBuffer;

public interface ISecureCipher {

    // TODO, show message on Accept
    public String getCipherCapabilities();

    // step DH1 used by initiator and responder, then stores kmaster for AES key
    public ByteBuffer handshakeDH1(ByteBuffer Generator, ByteBuffer modP, ByteBuffer publicKey);

    // step DH2 used by initiator and responder, then stores kmaster for AES iv
    public ByteBuffer handshakeDH2(ByteBuffer Generator, ByteBuffer modP, ByteBuffer publicKey);

    // step AES used by initiator and responder, use key and iv to compute shared key
    public ByteBuffer handshakeAES(ByteBuffer sharedKey);

    // used by initiator and responder, use shared key to encode
    public ByteBuffer encode(ByteBuffer messageBuffer);
    
    // used by initiator and responder, use shared key to dencode
    public ByteBuffer decode(ByteBuffer criptogrammBuffer);

    // performs handshakes and returns success result
    public boolean processHandshake(ByteBuffer buffer, IClientSession session); 

    public static enum HandshakeStages {
        INITIATOR_START((byte)0),
        RESPONDER_START((byte)1),
        INITIATOR_NEXT((byte)2),
        RESPONDER_NEXT((byte)3),
        INICIATOR_FINAL((byte)4),
        RESPONDER_FINAL((byte)5),
        CANSEL((byte)6);

        private final byte code;

        HandshakeStages(byte code){
            this.code = code;
        }
        public byte getCode(){
            return this.code;
        }

        public static HandshakeStages fromByte(byte stage){
            for(HandshakeStages s : values()){
                if(s.code == stage) return s;
            }

            throw new IllegalArgumentException("Unknown stage: " + stage);
        }
    }
}
