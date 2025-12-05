package servidor;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import interfaces.IClientSession;
import interfaces.ISecureCipher;
import util.Functions;

public class SecureCipher implements ISecureCipher {

    protected boolean isSecure;
    private BigInteger generator;
    private BigInteger prime;

    protected ByteBuffer privateKey1; // should be less than prime in bytes
    protected ByteBuffer publicKey1;
    protected ByteBuffer kMaster1; // AES shared key

    protected ByteBuffer privateKey2; // should be less than prime in bytes
    protected ByteBuffer publicKey2;
    protected ByteBuffer kMaster2; // AES iv

    protected HandshakeStages stage;

    public static BigInteger generateGenerator() {
        return new BigInteger(
                "44157404837960328768872680677686802650999163226766694797650810379076416463147265401084491113667624054557335394761604876882446924929840681990106974314935015501571333024773172440352475358750668213444607353872754650805031912866692119819377041901642732455911509867728218394542745330014071040326856846990119719675");
    }

    public static BigInteger generatePrime() {
        return new BigInteger(
                "99494096650139337106186933977618513974146274831566768179581759037259788798151499814653951492724365471316253651463342255785311748602922458795201382445323499931625451272600173180136123245441204133515800495917242011863558721723303661523372572477211620144038809673692512025566673746993593384600667047373692203583");
    }

    public SecureCipher(String privateKeyString) {
        this.prime = SecureCipher.generatePrime();

        this.generator = SecureCipher.generateGenerator();

        this.privateKey1 = ByteBuffer.wrap(privateKeyString.getBytes());
        this.privateKey2 = ByteBuffer.wrap(privateKeyString.getBytes()); // TODO, change private key!!!!

        this.stage = ISecureCipher.HandshakeStages.fromByte((byte) 0);
    }

    @Override
    public String getCipherCapabilities() {
        String msg = "";
        msg += "Cipher INFO\n";
        msg += "Diffie-Hellmann exchange\n";
        msg += "AES\n";
        return msg;
    }

    // handshake 1
    private byte[] getPayloadDH(byte stage) {
        byte[] p = this.prime.toByteArray();

        byte[] g = this.generator.toByteArray();
        
        this.publicKey1 = ByteBuffer.wrap(computeDHPublicKey().toByteArray());
        byte[] pK = new byte[this.publicKey1.remaining()];
        this.publicKey1.get(pK);

        byte[] payload = new byte[1 + 12 + p.length + g.length + pK.length];

        payload[0] = stage;
        System.arraycopy(Functions.intToByteArray_bigEndian(p.length), 0, payload, 1 + 0, 4);
        System.arraycopy(Functions.intToByteArray_bigEndian(g.length), 0, payload, 1 + 4, 4);
        System.arraycopy(Functions.intToByteArray_bigEndian(pK.length), 0, payload, 1 + 8, 4);

        System.arraycopy(p, 0, payload, 1 + 12, p.length);
        System.arraycopy(g, 0, payload, 1 + 12 + p.length, g.length);
        System.arraycopy(pK, 0, payload, 1 + 12 + p.length + g.length, pK.length);

        return payload;
    }

    @Override
    public boolean processHandshake(ByteBuffer buffer, IClientSession session) {
        Frame frame = Frame.toFrame(buffer);
        // if is valid check if frame is SECURE, then complete handshakes
        boolean result = Frame.isSecureFrame(frame);
        if (!result) {
            return false; // return false if we did not process the frame successfully
        }
        result = true; // return true if we processed the frame
        byte handshakeState = frame.data[0];
        ISecureCipher cipher = session.getCipher();
        HandshakeStages stage = HandshakeStages.fromByte(handshakeState);
        switch (stage) {
            case INITIATOR_START: {
                if (this.stage == stage) {
                    // we are initiator
                    // next stage responder start
                    byte[] payload = this.getPayloadDH((byte) 1);
                    frame.data = payload;
                    frame.length = payload.length;
                    session.write(ByteBuffer.wrap(Frame.toByteArray(frame)));
                } else {
                    // received stage 0 but we have this.stage > 0, this is a bad frame
                    result = false;
                }
                break;
            }
            case RESPONDER_START: {

                break;
            }
            case INITIATOR_NEXT: {

                break;
            }
            case RESPONDER_NEXT: {

                break;
            }
            case INICIATOR_FINAL: {

                break;
            }
            case RESPONDER_FINAL: {

                break;
            }
            case CANSEL: {

                break;
            }
            default:
                break;
        }

        return result;
    }

    private BigInteger computeDHPublicKey() {
        return this.generator.modPow(new BigInteger(this.privateKey1.array()), prime);
    }

    private boolean areBuffersEqual(ByteBuffer left, ByteBuffer right) {

        if (left.duplicate().compareTo(right.duplicate()) == 0) {
            return true;
        }

        return false;
    }

    @Override
    public ByteBuffer handshakeDH1(ByteBuffer Generator, ByteBuffer modP, ByteBuffer publicKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handshakeDH1'");
    }

    @Override
    public ByteBuffer handshakeDH2(ByteBuffer Generator, ByteBuffer modP, ByteBuffer publicKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handshakeDH2'");
    }

    @Override
    public ByteBuffer handshakeAES(ByteBuffer sharedKey) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handshakeAES'");
    }

    @Override
    public ByteBuffer encode(ByteBuffer messageBuffer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public ByteBuffer decode(ByteBuffer criptogrammBuffer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decode'");
    }

}
