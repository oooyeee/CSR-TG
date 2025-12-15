package server;

import interfaces.IClientSession;
import interfaces.IValidator;
import java.nio.ByteBuffer;
import util.Log;

public class Validator implements IValidator {

    @Override
    public boolean verify(IClientSession session, ByteBuffer data) {
        return Frame.isFrame(data);
    }


}
