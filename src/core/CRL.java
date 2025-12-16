package core;

public class CRL {
    public final Repositorio<RevokedCert> revoked;
    public CRL(){
        this.revoked = new Repositorio<>();
    }
}
