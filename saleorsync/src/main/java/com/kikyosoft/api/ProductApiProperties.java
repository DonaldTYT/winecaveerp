package com.kikyosoft.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "saleor.product-api")
public class ProductApiProperties {
    private String graphqlUrl = "http://192.168.19.212:8000/graphql/";
    private String token = "";
    private String clientApiKey = "";
    private String allowedOrigins = "";
    private String channel = "hk";
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 20000;

    public String getGraphqlUrl() { return graphqlUrl; }
    public void setGraphqlUrl(String graphqlUrl) { this.graphqlUrl = graphqlUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getClientApiKey() { return clientApiKey; }
    public void setClientApiKey(String clientApiKey) { this.clientApiKey = clientApiKey; }
    public String getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
