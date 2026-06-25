package com.kikyosoft.tcp;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TcpServerInstance {

    private final String name;
    private final int port;
    private final int backlog;
    private final int clientReadTimeoutMs;
    private final ThreadPoolTaskExecutor workers;
    private final TcpHandler handler;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile ServerSocket server;

    public TcpServerInstance(String name, int port, int backlog, int clientReadTimeoutMs,
                             ThreadPoolTaskExecutor workers, TcpHandler handler) {
        this.name = name;
        this.port = port;
        this.backlog = backlog;
        this.clientReadTimeoutMs = clientReadTimeoutMs;
        this.workers = workers;
        this.handler = handler;
    }

    public void start() {
        new Thread(this::acceptLoop, "tcp-boss-" + name).start();
    }

    public void stop() {
        running.set(false);
        try {
            ServerSocket s = this.server;
            if (s != null && !s.isClosed()) s.close();
        } catch (IOException ignore) {}
        try {
            workers.shutdown();
        } catch (Throwable ignore) {}
        System.out.println("[TCP][" + name + "] stopped.");
    }

    private void acceptLoop() {
        if (!running.compareAndSet(false, true)) return;
        try (ServerSocket ss = new ServerSocket()) {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(port), backlog);
            this.server = ss;
            System.out.println("[TCP][" + name + "] listening on " + port);

            while (running.get()) {
                try {
                    Socket s = ss.accept();
                    s.setTcpNoDelay(true);
                    s.setSoTimeout(clientReadTimeoutMs);
                    workers.execute(() -> handleClient(s));
                } catch (SocketException se) {
                    if (running.get()) System.err.println("[TCP][" + name + "] accept error: " + se.getMessage());
                    break;
                }
            }
        } catch (IOException bindEx) {
            System.err.println("[TCP][" + name + "] bind failed on " + port + ": " + bindEx.getMessage());
        } finally {
            running.set(false);
        }
    }

//    private void handleClient(Socket socket) {
//        try (Socket s = socket;
//             BufferedInputStream in = new BufferedInputStream(s.getInputStream());
//             BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())) {
//
//            handler.onClientConnected(s);
//
//            // Simple line-delimited framing. Replace with your own codec if needed.
//            ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
//            while (!s.isClosed()) {
//                int b = in.read();
//                if (b == -1) break;
//                if (b == '\n') {
//                    byte[] frame = buf.toByteArray();
//                    buf.reset();
//                    if (frame.length == 0) continue;
//                    handler.onFrame(frame, out);
//                } else {
//                    buf.write(b);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("[TCP][" + name + "] client error: " + e.getMessage());
//        }
//    }
    
    private void handleClient(Socket socket) {
        try (Socket s = socket;) {
            handler.onClientConnected(s);
            handler.handleConnection(s);
        } catch (Exception e) {
            System.err.println("[TCP][" + name + "] client error: " + e.getMessage());
        }
    }
}
