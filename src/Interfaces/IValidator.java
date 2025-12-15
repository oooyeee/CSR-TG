package interfaces;

import java.nio.ByteBuffer;

public interface IValidator {
    public boolean verify(IClientSession session, ByteBuffer data);
}
