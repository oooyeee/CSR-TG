package server;

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

import interfaces.IClientSession;
import interfaces.IMiddlewareNode;

public class MiddlewareNode implements IMiddlewareNode {

    private BiPredicate<IClientSession, ByteBuffer> callback;
    private IMiddlewareNode next;

    public MiddlewareNode(){
        this.callback = null;
        this.next = null;
    }

    @Override
    public void setHandler(BiPredicate<IClientSession, ByteBuffer> callback) {
        this.callback = callback;
    }

    @Override
    public void setNext(IMiddlewareNode next) {
        this.next = next;
    }

    @Override
    public boolean handle(IClientSession session, ByteBuffer data) {
       return this.callback.test(session, data);
    }

    @Override
    public IMiddlewareNode next() {
        return this.next;
    }
 
}
