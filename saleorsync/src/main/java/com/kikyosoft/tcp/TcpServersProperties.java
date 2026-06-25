package com.kikyosoft.tcp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.tcp")
public class TcpServersProperties {

    public static class ServerSpec {
        private String name;
        private int port;
        private int backlog = 200;
        private int workerCore = 16;
        private int workerMax = 32;
        private int queueCapacity = 500;
        private int clientReadTimeoutMs = 30000;
        private String handlerBean;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public int getBacklog() { return backlog; }
        public void setBacklog(int backlog) { this.backlog = backlog; }
        public int getWorkerCore() { return workerCore; }
        public void setWorkerCore(int workerCore) { this.workerCore = workerCore; }
        public int getWorkerMax() { return workerMax; }
        public void setWorkerMax(int workerMax) { this.workerMax = workerMax; }
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
        public int getClientReadTimeoutMs() { return clientReadTimeoutMs; }
        public void setClientReadTimeoutMs(int clientReadTimeoutMs) { this.clientReadTimeoutMs = clientReadTimeoutMs; }
        public String getHandlerBean() { return handlerBean; }
        public void setHandlerBean(String handlerBean) { this.handlerBean = handlerBean; }
    }

    private List<ServerSpec> servers = new ArrayList<>();
    public List<ServerSpec> getServers() { return servers; }
    public void setServers(List<ServerSpec> servers) { this.servers = servers; }
}
