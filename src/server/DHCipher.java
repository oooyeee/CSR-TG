package server;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import util.Log;

public class DHCipher {

    private BigInteger prime;
    private BigInteger generator;

    private byte[] key; // x
    public byte[] pub; // A
    public byte[] result; // K

    public DHCipher() {
        this(DHCipher.generatePrime(), DHCipher.generateGenerator());
    }

    public DHCipher(BigInteger prime, BigInteger generator) {
        this.key = null;
        this.pub = null;
        this.result = null;

        this.prime = prime;
        this.generator = generator;

        this.key = new byte[32]; // 256 bit
        new SecureRandom().nextBytes(this.key);

        this.pub = computeDHPublicKey().toByteArray();
    }

    public boolean isComplete() {
        return this.result != null;
    }

    private BigInteger computeDHPublicKey() {
        return this.generator.modPow(new BigInteger(this.key), prime);
    }

    public void generateResult(byte[] foreignPublicKey) {
        this.result = new BigInteger(foreignPublicKey).modPow(new BigInteger(this.key), prime).toByteArray();
    }

    public static BigInteger generateGenerator() {
        return new BigInteger(
                "44157404837960328768872680677686802650999163226766694797650810379076416463147265401084491113667624054557335394761604876882446924929840681990106974314935015501571333024773172440352475358750668213444607353872754650805031912866692119819377041901642732455911509867728218394542745330014071040326856846990119719675");
    }

    public static BigInteger generatePrime() {
        return new BigInteger(
                "99494096650139337106186933977618513974146274831566768179581759037259788798151499814653951492724365471316253651463342255785311748602922458795201382445323499931625451272600173180136123245441204133515800495917242011863558721723303661523372572477211620144038809673692512025566673746993593384600667047373692203583");
    }

    public byte[] getRequestPayload() { // TODO, this needs rework
        // payload = type, length = 3, frames[1,2,3]
        // Frame 1 = prime
        // frame 2 = generator
        // frame 3 = pubkey
        Frame primeFrame = new Frame(0, 0, null);
        primeFrame.type = Frame.FrameType.BYTES.ordinal();
        primeFrame.data = this.prime.toByteArray();
        primeFrame.length = primeFrame.data.length;

        Frame generatorFrame = new Frame(0, 0, null);
        generatorFrame.type = Frame.FrameType.BYTES.ordinal();
        generatorFrame.data = this.generator.toByteArray();
        generatorFrame.length = generatorFrame.data.length;

        Frame pubkeyFrame = new Frame(0, 0, null);
        pubkeyFrame.type = Frame.FrameType.BYTES.ordinal();
        pubkeyFrame.data =  new byte[this.pub.length];
        System.arraycopy(this.pub, 0, pubkeyFrame.data, 0, this.pub.length);
        pubkeyFrame.length = generatorFrame.data.length;

        int payloadLength = primeFrame.length + generatorFrame.length + pubkeyFrame.length + (4 + 4) * 3;

        Frame payload = new Frame(Frame.FrameType.NESTED_HANDSHAKE.ordinal(), 3, null);
        ByteBuffer buf = ByteBuffer.allocate(payloadLength);
        buf.put(Frame.getBytes(primeFrame));
        buf.put(Frame.getBytes(generatorFrame));
        buf.put(Frame.getBytes(pubkeyFrame));
        buf.flip();
        payload.data = new byte[payloadLength];
        buf.get(payload.data);

        return Frame.getBytes(payload);
    }

    public byte[] getResponsePayload() {
        Frame payload = new Frame(Frame.FrameType.HANDSHAKE.ordinal(), this.pub.length, this.pub);

        return Frame.getBytes(payload);
    }

    public Frame getResponseFrame() {

        return new Frame(Frame.FrameType.HANDSHAKE.ordinal(), this.pub.length, this.pub);
    }
}
