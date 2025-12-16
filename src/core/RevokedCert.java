package core;

import interfaces.IItem;

public class RevokedCert implements IItem {
    private static int revokedCount = 0;
    public final int revokedID;
    public final byte[] hash;

    public RevokedCert(byte[] certHash){
        this.revokedID = revokedCount++;
        this.hash = certHash;
    }

    @Override
    public int getID() {
        return this.revokedID;
    }

    @Override
    public void update(IItem newItem) {
        return;
    }
    
}
