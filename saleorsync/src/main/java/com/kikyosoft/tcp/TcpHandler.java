package com.kikyosoft.tcp;

import java.io.OutputStream;
import java.net.Socket;

public interface TcpHandler {
    default void onClientConnected(Socket s) throws Exception {}
//    void onFrame(byte[] frame, OutputStream out) throws Exception;
    void handleConnection(Socket socket) throws Exception;
}
