package com.kikyosoft.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class SaleorProductClient {
    private final ProductApiProperties properties;
    private final RestTemplate rest;

    public SaleorProductClient(ProductApiProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        this.rest = new RestTemplate(requestFactory);
    }

    public JsonNode execute(String operationName, String query, Map<String, Object> variables) {
        String token = trim(properties.getToken());
        if (token.isEmpty()) {
            throw new ProductApiException(503,
                    "Product API is not configured: set SALEOR_PRODUCT_MCP_TOKEN");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operationName", operationName);
        payload.put("query", query);
        payload.put("variables", variables == null ? Collections.emptyMap() : variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        try {
            ResponseEntity<JsonNode> response = rest.exchange(properties.getGraphqlUrl(), HttpMethod.POST,
                    new HttpEntity<>(payload, headers), JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null) throw new ProductApiException(502, "Saleor returned an empty response");
            JsonNode errors = body.path("errors");
            if (errors.isArray() && errors.size() > 0) {
                throw new ProductApiException(502, "Saleor GraphQL error: " + errors.toString());
            }
            return body.path("data");
        } catch (ProductApiException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ProductApiException(502, "Unable to call Saleor GraphQL", ex);
        }
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
}
