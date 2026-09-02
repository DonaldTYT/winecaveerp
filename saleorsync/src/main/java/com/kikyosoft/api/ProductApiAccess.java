package com.kikyosoft.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class ProductApiAccess {
    private final ProductApiProperties properties;

    public ProductApiAccess(ProductApiProperties properties) {
        this.properties = properties;
    }

    public void requireClientAccess(HttpServletRequest request) {
        String expected = trim(properties.getClientApiKey());
        if (expected.isEmpty()) return;
        String supplied = trim(request.getHeader("X-API-Key"));
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ProductApiException(401, "Missing or invalid X-API-Key");
        }
    }

    public void requireAllowedOrigin(HttpServletRequest request) {
        String origin = trim(request.getHeader("Origin"));
        if (origin.isEmpty()) return;
        String configured = trim(properties.getAllowedOrigins());
        boolean allowed = !configured.isEmpty() && Arrays.stream(configured.split(","))
                .map(String::trim).anyMatch(origin::equals);
        if (!allowed) throw new ProductApiException(403, "Origin is not allowed");
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
}
