package server;

import interfaces.IClientSession;
import interfaces.IMiddlewareList;
import interfaces.IMiddlewareNode;

import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

public class MiddlewareList implements IMiddlewareList {

    public MiddlewareNode head, last;

    public MiddlewareList(){
        this.head = null;
        this.last = this.head;
    }

    @Override
    public void add(BiPredicate<IClientSession, ByteBuffer> callback) {
        if(this.head == null) {
            this.head = new MiddlewareNode();
            this.head.setHandler(callback);
            this.head.setNext(null);
            this.last = this.head;
        } else {
            MiddlewareNode next = new MiddlewareNode();
            next.setHandler(callback);
            next.setNext(null);
            this.last.setNext(next);
            this.last = next;
        }
    }

    @Override
    public IMiddlewareNode head() {
       return this.head;
    }



}
