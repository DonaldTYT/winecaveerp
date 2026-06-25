package com.kikyosoft.tcp;

import org.springframework.stereotype.Component;

import com.uniinformation.bicore.BiCoreRpcServlet;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.rpccall.RpcServer;
import com.uniinformation.rpccall.RpcServerConnection;

import java.net.Socket;

@Component("BiRpcServerTcpHandler")
public class BiRpcServerTcpHandler implements TcpHandler {
    @Override
    public void onClientConnected(Socket s) {}

    @Override
    public void handleConnection(Socket socket) throws Exception {
    	RpcServlet rpc;
    	rpc = new BiCoreRpcServlet();
        try (Socket s = socket) {
            System.out.println("rpcserver connected\n");
            rpc = new BiCoreRpcServlet();
//            RpcServer rpcsvr = new RpcServer(0);
//            rpcsvr.registerService("com.uniinformation.bicore.BiCoreRpcServlet", false, true);
            RpcServerConnection conn = new RpcServerConnection(null);
            conn.setServlet(rpc.getClass().getName(), rpc);
            conn.setSocket(s);
            conn.run();
            System.out.println("rpcserver disconnected\n");
        } catch (Exception e) {
            System.err.println("[TCP][" +  "] client error: " + e.getMessage());
        }
    }
}
