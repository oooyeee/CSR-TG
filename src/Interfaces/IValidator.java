package interfaces;

import java.nio.ByteBuffer;

public interface IValidator {
    // is used to check if incoming data is valid, runs before callback by the server
    public boolean isValid(ByteBuffer data);
}
