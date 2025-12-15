package server;

import interfaces.IClientSession;
import interfaces.ICommandRouter;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import util.EventEmitter;
import util.Log;

public class CommandRouter implements ICommandRouter {

    private final EventEmitter<IClientSession, String> ee;
    private BiConsumer<IClientSession, String> fallbackCommand;

    public CommandRouter() {
        this.ee = new EventEmitter<>();
        this.fallbackCommand = (session, msg) -> {
            Log.always(":: Unsupported command from client [" + session.clientID() + "]");

            Frame frame = new Frame(Frame.FrameType.STRING.ordinal(), msg.length(), msg.getBytes());

            Log.always(frame.toString());
            ByteBuffer buffer = ByteBuffer.allocate(Constants.bufferSize);
            frame.putIn(buffer);

            write(session, buffer);
        };
    }

    @Override
    public void onCommand(String command, BiConsumer<IClientSession, String> callback) {
        ee.on(command, callback);
    }

    @Override
    public void commandFallback(BiConsumer<IClientSession, String> callback) {
        this.fallbackCommand = callback;
    }

    @Override
    public void parseCommand(IClientSession session, String str) { // only in secure session, data is decoded

        if (str == null) {
            return;
        }

        int i = str.indexOf(' ');
        String command = (i == -1) ? str : str.substring(0, i);
        String msg = (i == -1) ? "" : str.substring(i + 1);

        if (!ee.hasEvent(command)) {
            this.fallbackCommand.accept(session, str);
        } else {
            ee.emit(command, session, msg);
        }
    }

    public void write(IClientSession session, ByteBuffer data) {
        try {
            session.write(data);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public void write(IClientSession session, Frame frame) {
        try {
            ByteBuffer buffer = session.getReadBuffer();
            frame.putIn(buffer);

            session.write(buffer);
        } catch (Exception e) {
            Log.error(e);
        }
    }

}
