package com.kikyosoft.tcp;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
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
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final Set<Socket> activeSockets = ConcurrentHashMap.newKeySet();
    private volatile ServerSocket server;
    private volatile Thread bossThread;

    public TcpServerInstance(String name, int port, int backlog, int clientReadTimeoutMs,
                             ThreadPoolTaskExecutor workers, TcpHandler handler) {
        this.name = name;
        this.port = port;
        this.backlog = backlog;
        this.clientReadTimeoutMs = clientReadTimeoutMs;
        this.workers = workers;
        this.handler = handler;
    }

    public synchronized void start() {
        if (stopRequested.get() || !running.compareAndSet(false, true)) return;
        Thread thread = new Thread(this::acceptLoop, "tcp-boss-" + name);
        bossThread = thread;
        thread.start();
    }

    public void stop() {
        requestStop();
        awaitStopped(10000L);
    }

    /** Signal shutdown without waiting, allowing the manager to signal every server first. */
    public void requestStop() {
        stopRequested.set(true);
        running.set(false);
        closeServerSocket(server);
        closeActiveSockets();
        Thread thread = bossThread;
        if (thread != null) thread.interrupt();
    }

    /** Complete executor shutdown and wait for the accept thread within a bounded time. */
    public boolean awaitStopped(long timeoutMs) {
        requestStop();
        long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

        try {
            // At this point the webapp is terminating. Do not keep queued RPC calls
            // or blocked client handlers alive after their sockets have been closed.
            workers.setWaitForTasksToCompleteOnShutdown(false);
            workers.setAwaitTerminationMillis(Math.max(0L, deadline - System.currentTimeMillis()));
            workers.shutdown();
        } catch (Throwable error) {
            System.err.println("[TCP][" + name + "] worker shutdown error: " + error.getMessage());
        }

        Thread thread = bossThread;
        if (thread != null && thread != Thread.currentThread()) {
            long waitMs = Math.max(0L, deadline - System.currentTimeMillis());
            if (waitMs > 0L) {
                try {
                    thread.join(waitMs);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        closeActiveSockets();
        boolean stopped = thread == null || !thread.isAlive();
        if (!stopped) {
            System.err.println("[TCP][" + name + "] accept thread did not stop; state=" + thread.getState());
        } else {
            System.out.println("[TCP][" + name + "] stopped.");
        }
        return stopped;
    }

    private void acceptLoop() {
        try (ServerSocket ss = new ServerSocket()) {
            // Publish the socket before bind so a concurrent stop can close it and
            // prevent a late bind after the Spring context has begun shutting down.
            this.server = ss;
            if (stopRequested.get() || !running.get()) return;
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress(port), backlog);
            if (stopRequested.get() || !running.get()) return;
            this.server = ss;
            System.out.println("[TCP][" + name + "] listening on " + port);

            while (running.get()) {
                try {
                    Socket s = ss.accept();
                    if (stopRequested.get() || !running.get()) {
                        closeSocket(s);
                        break;
                    }
                    s.setTcpNoDelay(true);
                    s.setSoTimeout(clientReadTimeoutMs);
                    activeSockets.add(s);
                    try {
                        workers.execute(() -> handleClient(s));
                    } catch (RejectedExecutionException rejected) {
                        activeSockets.remove(s);
                        closeSocket(s);
                        if (!stopRequested.get()) {
                            System.err.println("[TCP][" + name + "] worker rejected client: " + rejected.getMessage());
                        }
                    }
                } catch (SocketException se) {
                    if (running.get()) System.err.println("[TCP][" + name + "] accept error: " + se.getMessage());
                    break;
                }
            }
        } catch (IOException bindEx) {
            if (!stopRequested.get()) {
                System.err.println("[TCP][" + name + "] bind failed on " + port + ": " + bindEx.getMessage());
            }
        } finally {
            running.set(false);
            server = null;
            if (bossThread == Thread.currentThread()) bossThread = null;
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
            if (!stopRequested.get()) {
                System.err.println("[TCP][" + name + "] client error: " + e.getMessage());
            }
        } finally {
            activeSockets.remove(socket);
        }
    }

    private void closeActiveSockets() {
        for (Socket socket : activeSockets) closeSocket(socket);
        activeSockets.clear();
    }

    private static void closeSocket(Socket socket) {
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException ignore) {}
    }

    private static void closeServerSocket(ServerSocket socket) {
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException ignore) {}
    }
}
