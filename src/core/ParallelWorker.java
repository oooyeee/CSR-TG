package core;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import util.Log;

public class ParallelWorker extends Thread {

    private final BlockingQueue<SignRequest> queue = new LinkedBlockingQueue<>();
    private final HeadlessClient client;

    public ParallelWorker(String host, int port) throws IOException {
        this.client = new HeadlessClient(host, port);
        start();
    }

    public CompletableFuture<byte[]> submit(byte[] data) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        queue.add(new SignRequest(data, future));
        return future;
    }

    @Override
    public void run() {
        while (true) {
            try {
                client.readFromSocket();

                if (client.isSecure()) {
                    SignRequest req = queue.poll();
                    if (req != null) {
                        client.sendSignRequest(req.data, req.future);
                    }
                }

                Thread.sleep(5);
            } catch (Exception e) {
                Log.error(":: Error in running worker thread");
                Log.error(e);
                e.printStackTrace();
            }
        }
    }

    private static class SignRequest {
        byte[] data;
        CompletableFuture<byte[]> future;

        SignRequest(byte[] data, CompletableFuture<byte[]> future) {
            this.data = data;
            this.future = future;
        }
    }
}
