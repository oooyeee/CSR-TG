package interfaces;

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

public interface IMiddlewareNode {
    public void setHandler(BiPredicate<IClientSession, ByteBuffer> callback);
    public void setNext(IMiddlewareNode next);
    public boolean handle(IClientSession session, ByteBuffer data);
    public IMiddlewareNode next();
}
