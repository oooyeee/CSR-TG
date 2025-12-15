package server;

import interfaces.IClientSession;
import interfaces.IMiddlewareList;
import interfaces.IMiddlewareNode;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import util.Log;

public class ClientSession implements IClientSession {

    private static long idCount = 0;
    private IMiddlewareList writeChain;
    private boolean isTrusted = false;

    public static String getNewID() {
        return String.valueOf(idCount++);
    }

    final private String clientID;
    final private SelectionKey selectorKey;

    protected SessionState sesionState;
    protected WriteState writeState;

    protected ByteBuffer pendingWrite;
    protected ByteBuffer readBuffer;

    public ClientSession(SelectionKey key, int writeBufferSizeInKB) {
        this.sesionState = SessionState.NEED_HANDSHAKE;
        this.writeState = WriteState.NOT_READY;

        this.clientID = getNewID();
        this.selectorKey = key;
        this.pendingWrite = ByteBuffer.allocate(writeBufferSizeInKB * 1024);
        this.readBuffer = ByteBuffer.allocate(writeBufferSizeInKB * 1024);

        this.writeChain = null;
    }

    @Override
    public String clientID() {
        return this.clientID;
    }

    @Override
    public SelectionKey clientKey() {
        return this.selectorKey;
    }

    @Override
    public void write(String message) throws IOException { // message should be terminated with "\n"
        write(StandardCharsets.UTF_8.encode(message));
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException { // dont use to Frame.toFrame here, because buffer may be encoded
        IMiddlewareNode current = this.writeChain.head();
        boolean result = true;
        while (current != null) {
            result = current.handle(this, buffer); // mutates buffer if nesessary
            if (result == false) {
                break;
            }
            current = current.next();
        }

        if (result == false) {
            return;
        }
        writeBuffer(buffer);
    }

    private void writeBuffer(ByteBuffer buffer) throws IOException {
        SocketChannel clientChannel = (SocketChannel) this.selectorKey.channel();

        Log.debug(">> out " + buffer.duplicate().remaining() + " bytes");

        while (buffer.hasRemaining()) {
            int written = clientChannel.write(buffer);
            if (written == 0) {
                pendingWrite = buffer;
                this.selectorKey.interestOpsOr(SelectionKey.OP_WRITE); // next time server will check for write key
                // first and use pendingWrite buffer (i think)
                return;
            }
        }

    }

    @Override
    public SessionState getSecureState() {
        return this.sesionState;
    }

        @Override
    public WriteState getWriteState() {
        return this.writeState;
    }

    @Override
    public void injectWriteFiltersChain(IMiddlewareList writeFiltersChain) {
        this.writeChain = writeFiltersChain;
    }

    @Override
    public void setSecureState(SessionState newState) {
        this.sesionState = newState;
    }

    @Override
    public void setWriteState(WriteState newState){
        this.writeState = newState;
    }

    @Override
    public ByteBuffer getReadBuffer() {
        return this.readBuffer;
    }

    public boolean isTrusted(){
        return this.isTrusted;
    }
    public void setTrusted(boolean value){
        this.isTrusted = value;
    }
}
