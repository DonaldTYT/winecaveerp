package com.kikyosoft.tcp;



import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component("loopBackTcpHandler")
public class LoopBackTcpHandler implements TcpHandler {
    @Override
    public void onClientConnected(Socket s) {}

    private void onFrame(byte[] frame, OutputStream out) throws Exception {
        String msg = new String(frame, StandardCharsets.UTF_8).trim();
        String resp = "LOOPBACK:" + msg + "\n";
        out.write(resp.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
    
    @Override
    public void handleConnection(Socket socket) throws Exception {
        try (Socket s = socket;
             BufferedInputStream in = new BufferedInputStream(s.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream())) {

            // Simple line-delimited framing. Replace with your own codec if needed.
            ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
            System.out.println("tcp connected\n");
            while (!s.isClosed()) {
                int b = in.read();
                if (b == -1) break;
                if (b == '\n') {
                    byte[] frame = buf.toByteArray();
                    buf.reset();
                    if (frame.length == 0) continue;
                    onFrame(frame, out);
                } else {
                    buf.write(b);
                }
            }
            System.out.println("tcp disconnected\n");
        } catch (Exception e) {
            System.err.println("[TCP][" +  "] client error: " + e.getMessage());
        }
    }
}
