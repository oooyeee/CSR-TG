package core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.Instant;

import util.Conversions;
import util.Log;

public class Leilao {
    private static int idCounter = 0;

    private int itemID;
    private int ownerID;
    private String description;
    private int startPrice;
    private long createdAt;
    private long updatedAt;

    public Leilao(int ownerID, String description, int startPrice) {
        this.itemID = idCounter++;
        this.ownerID = ownerID;
        this.description = description;
        this.startPrice = startPrice;
        this.createdAt = Instant.now().toEpochMilli();
        this.updatedAt = this.createdAt;
    }

    private Leilao(int itemID,
            int ownerID,
            String description,
            int startPrice,
            long createdAt,
            long updatedAt) {
        this.itemID = itemID;
        this.ownerID = ownerID;
        this.description = description;
        this.startPrice = startPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public byte[] getSha256Hash() {
        return Conversions.getSHA256Hash(this.serialize());
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.itemID);
            out.writeInt(this.ownerID);
            out.writeUTF(this.description);
            out.writeInt(this.startPrice);
            out.writeLong(this.createdAt);
            out.writeLong(this.updatedAt);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bos.toByteArray();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Item ID: " + this.itemID + "\n");
        sb.append("Owner ID: " + this.ownerID + "\n");
        sb.append("Description: " + this.description + "\n");
        sb.append("Start Price: " + this.startPrice + "\n");
        sb.append("Created At: " + Conversions.fromLongMillis(this.createdAt) + "\n");
        sb.append("Updated At: " + Conversions.fromLongMillis(this.updatedAt) + "\n");
        return sb.toString();
    }

    public static Leilao fromBytes(byte[] serializedBytes) {
        ByteArrayInputStream bos = new ByteArrayInputStream(serializedBytes);
        DataInputStream in = new DataInputStream(bos);
        Leilao item = null;
        try {
            int itemID = in.readInt();
            int ownerID = in.readInt();
            String description = in.readUTF();
            int startPrice = in.readInt();
            long createdAt = in.readLong();
            long updatedAt = in.readLong();

            item = new Leilao(itemID, ownerID, description, startPrice, createdAt, updatedAt);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return item;
    }
}
