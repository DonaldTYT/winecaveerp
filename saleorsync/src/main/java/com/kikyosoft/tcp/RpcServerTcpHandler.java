package com.kikyosoft.tcp;

import org.springframework.stereotype.Component;

import com.kikyosoft.rpccall.RpcConnection;
import com.kikyosoft.rpccall.RpcServlet;
import com.kikyosoft.rpcservlet.SaleorSyncRpc;
import com.kikyosoft.stream.SocketByteStream;

import java.net.Socket;

@Component("rpcServerTcpHandler")
public class RpcServerTcpHandler implements TcpHandler {
    @Override
    public void onClientConnected(Socket s) {}

    @Override
    public void handleConnection(Socket socket) throws Exception {
    	RpcServlet rpc = new SaleorSyncRpc();
        try (Socket s = socket) {
            System.out.println("rpcserver connected\n");
            SocketByteStream sbs = new SocketByteStream(s);
            RpcConnection rpcConn = new RpcConnection(null);
    		rpcConn.setServlet(rpc.getClass().getName(), rpc);
    		rpcConn.setByteStream(sbs);
            rpcConn.setTimeOut(60000);
    		rpcConn.run();
            System.out.println("rpcserver disconnected\n");
        } catch (Exception e) {
            System.err.println("[TCP][" +  "] client error: " + e.getMessage());
        }
    }
}
