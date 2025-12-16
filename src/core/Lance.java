package core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.time.Instant;

import interfaces.IItem;
import util.Conversions;
import util.Log;

public class Lance implements IItem {
    private static int idCounter = 0;

    protected final int leilaoID;
    protected final int bidderID;
    protected final int itemID;
    private final byte[] itemHash;
    private final int valor; // em centimos
    private final long timestamp; // em ms
    private byte[] bidderSignature;

    // for creating new
    public Lance(int itemID, int bidderID, int valor, byte[] itemHash, byte[] bidderSignature) {
        this.leilaoID = idCounter++;
        this.bidderID = bidderID;
        this.itemID = itemID;
        this.valor = valor;
        this.itemHash = itemHash;
        this.timestamp = Instant.now().toEpochMilli();
        this.bidderSignature = bidderSignature;
    }

    // for deserializaton
    private Lance(int leilaoID, int bidderID, int itemID, byte[] itemHash, int valor, long timestamp,
            byte[] bidderSignature) {
        this.leilaoID = leilaoID;
        this.bidderID = bidderID;
        this.itemID = itemID;
        this.itemHash = itemHash;
        this.valor = valor;
        this.timestamp = timestamp;
        this.bidderSignature = bidderSignature;
    }

    public byte[] getSha256Hash() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.leilaoID);
            out.writeInt(this.bidderID);
            out.writeInt(this.itemID);
            out.write(this.itemHash);
            out.writeInt(this.valor);
            out.writeLong(timestamp);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
        return Conversions.getSHA256Hash(bos.toByteArray());
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.leilaoID);
            out.writeInt(this.bidderID);
            out.writeInt(this.itemID);
            out.write(this.itemHash);
            out.writeInt(this.valor);
            out.writeLong(timestamp);
            out.write(this.bidderSignature);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bos.toByteArray();
    }

    public static Lance fromBytes(byte[] serializedBytes) {
        ByteArrayInputStream bos = new ByteArrayInputStream(serializedBytes);
        DataInputStream in = new DataInputStream(bos);
        Lance bid = null;
        try {
            int leilaoID = in.readInt();
            int bidderID = in.readInt();
            int itemID = in.readInt();
            byte[] itemHash = new byte[32]; // 256 sha bits
            in.read(itemHash);
            int valor = in.readInt();
            long timestamp = in.readLong();
            byte[] bidderSignature = new byte[256]; // 2048 rsa bit key
            in.read(bidderSignature);

            bid = new Lance(leilaoID, bidderID, itemID, itemHash, valor, timestamp, bidderSignature);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bid;
    }

    public String toString() {
        String str = "";
        str += "leilaoID: " + String.valueOf(this.leilaoID) + "\n";
        str += "bidderID: " + String.valueOf(this.bidderID) + "\n";
        str += "itemID: " + String.valueOf(this.itemID) + "\n";
        str += "itemHash[" + this.itemHash.length + "]: " + Conversions.bytesToHex(this.itemHash) + "\n";
        str += "valor: " + String.valueOf(this.valor) + "\n";
        str += "timestamp: " + String.valueOf(this.timestamp);
        str += "bidderSignature[" + this.bidderSignature.length + "]: " + Conversions.bytesToHex(this.bidderSignature) + "\n";
        return str;
    }

    @Override
    public int getID() {
        return this.leilaoID;
    }

    @Override
    public void update(IItem newItem) {
        // um lance é final
        Log.warn(":: nao se pode fazer update a um lance!");
        return;
    }
}
