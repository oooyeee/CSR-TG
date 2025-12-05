package servidor;

import java.nio.ByteBuffer;

import interfaces.IValidator;

public class FrameValidator implements IValidator{

    public FrameValidator(){

    }

    @Override
    public boolean isValid(ByteBuffer data) {
        return Frame.isFrameLike(data);
    }
    
}
