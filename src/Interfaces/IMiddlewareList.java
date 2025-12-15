package interfaces;

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

public interface IMiddlewareList {
    public void add(BiPredicate<IClientSession, ByteBuffer> callback);
    public IMiddlewareNode head();
}
