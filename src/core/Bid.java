package core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.time.Instant;

import util.Conversions;
import util.Log;

public class Bid {
    private static int idCounter = 0;

    protected final int leilaoID;
    private int itemID;
    private byte[] itemHash;
    private int valor; // em centimos
    private long timestamp; // em ms

    // for creating new
    public Bid(int itemID, int valor, byte[] itemHash) {
        this.leilaoID = idCounter++;
        this.itemID = itemID;
        this.valor = valor;
        this.itemHash = itemHash;
        this.timestamp = Instant.now().toEpochMilli();
    }

    // for deserializaton
    private Bid(int leilaoID, int itemID, byte[] itemHash, int valor, long timestamp) {
        this.leilaoID = leilaoID;
        this.itemID = itemID;
        this.itemHash = itemHash;
        this.valor = valor;
        this.timestamp = timestamp;
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.leilaoID);
            out.writeInt(this.itemID);
            out.write(this.itemHash);
            out.writeInt(this.valor);
            out.writeLong(timestamp);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bos.toByteArray();
    }

    public static Bid fromBytes(byte[] serializedBytes) {
        ByteArrayInputStream bos = new ByteArrayInputStream(serializedBytes);
        DataInputStream in = new DataInputStream(bos);
        Bid bid = null;
        try {
            int leilaoID = in.readInt();
            int itemID = in.readInt();
            byte[] itemHash = new byte[32];
            in.read(itemHash);
            int valor = in.readInt();
            long timestamp = in.readLong();

            bid = new Bid(leilaoID, itemID, itemHash, valor, timestamp);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bid;
    }

    public String toString() {
        String str = "";
        str += "leilaoID: " + String.valueOf(this.leilaoID) + "\n";
        str += "itemID: " + String.valueOf(this.itemID) + "\n";
        str += "itemHash[" + this.itemHash.length + "]: " + Conversions.bytesToHex(this.itemHash) + "\n";
        str += "valor: " + String.valueOf(this.valor) + "\n";
        str += "timestamp: " + String.valueOf(this.timestamp);
        return str;
    }
}
