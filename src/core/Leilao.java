package core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.Instant;

import interfaces.IItem;
import util.Conversions;
import util.Log;

public class Leilao implements IItem {
    private static int idCounter = 0;

    public final int itemID;
    public final int ownerID;
    public final String estado;
    public final int startPrice;
    private final long createdAt;
    public String description;
    private long updatedAt;
    private final long expiresAt;
    private byte[] ownerSignature;

    public Leilao(int ownerID, String description, int startPrice, String estado) {
        this(ownerID, description, startPrice, estado, Conversions.getTimeNow(120));
    }

    public Leilao(int ownerID, String description, int startPrice, String estado, long expiresAt) {
        this.itemID = idCounter++;
        this.ownerID = ownerID;
        this.estado = estado;
        this.startPrice = startPrice;
        this.createdAt = Instant.now().toEpochMilli();
        this.description = description;
        this.updatedAt = this.createdAt;
        this.expiresAt = expiresAt;
        this.ownerSignature = null;
    }

    private Leilao(int itemID,
            int ownerID,
            String estado,
            int startPrice,
            String description,
            long createdAt,
            long updatedAt,
            long expiresAt,
            byte[] ownerSignature) {
        this.itemID = itemID;
        this.ownerID = ownerID;
        this.estado = estado;
        this.startPrice = startPrice;
        this.createdAt = createdAt;
        this.description = description;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.ownerSignature = ownerSignature;
    }

    public void putSignature(byte[] singnature) {
        this.ownerSignature = singnature;
    }

    public byte[] getSha256Hash() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.itemID);
            out.writeInt(this.ownerID);
            out.writeUTF(this.estado);
            out.writeInt(this.startPrice);
            out.writeLong(this.createdAt);
            out.writeLong(this.expiresAt);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        // return Conversions.getSHA256Hash(this.serialize());
        return Conversions.getSHA256Hash(bos.toByteArray());
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        try {
            out.writeInt(this.itemID);
            out.writeInt(this.ownerID);
            out.writeUTF(this.estado);
            out.writeInt(this.startPrice);
            out.writeLong(this.createdAt);
            out.writeUTF(this.description);
            out.writeLong(this.updatedAt);
            out.writeLong(this.expiresAt);
            if (this.ownerSignature == null) {
                out.write((byte) 0);
            } else {
                out.write((byte) 1);
                out.write(ownerSignature.length);
                out.write(this.ownerSignature);
            }
        } catch (Exception e) {
            Log.error(e);
            return null;
        }

        return bos.toByteArray();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Item ID: " + this.itemID + "\n");
        // sb.append("Owner ID: " + this.ownerID + "\n");
        sb.append("Estado: " + this.estado + "\n");
        sb.append("Description: " + this.description + "\n");
        sb.append("Start Price: " + this.startPrice + "\n");
        sb.append("Created At: " + Conversions.fromLongMillis(this.createdAt) + "\n");
        sb.append("Updated At: " + Conversions.fromLongMillis(this.updatedAt) + "\n");
        sb.append("Expires At: " + Conversions.fromLongMillis(this.expiresAt) + "\n");
        sb.append("Signature: " + Conversions.bytesToHex(ownerSignature).substring(0, 7) + "...\n");
        return sb.toString();
    }

    public static Leilao fromBytes(byte[] serializedBytes) {
        ByteArrayInputStream bos = new ByteArrayInputStream(serializedBytes);
        DataInputStream in = new DataInputStream(bos);
        Leilao item = null;
        try {
            int itemID = in.readInt();
            int ownerID = in.readInt();
            String estado = in.readUTF();
            int startPrice = in.readInt();
            long createdAt = in.readLong();
            String description = in.readUTF();
            long updatedAt = in.readLong();
            long expiresAt = in.readLong();
            byte hasSignature = in.readByte();
            byte[] ownerSignature = null;
            if ((byte) hasSignature == 1) {
                int sigLength = in.readInt();
                ownerSignature = new byte[sigLength];
                in.readFully(ownerSignature);
            }

            item = new Leilao(itemID, ownerID, estado, startPrice, description, createdAt, updatedAt, expiresAt,
                    ownerSignature);
        } catch (Exception e) {
            Log.error(e);
            e.printStackTrace();
            return null;
        }

        return item;
    }

    @Override
    public int getID() {
        return this.itemID;
    }

    @Override
    public void update(IItem newItem) {
        this.description = ((Leilao) newItem).description;
        this.updatedAt = Instant.now().toEpochMilli();
    }
}
