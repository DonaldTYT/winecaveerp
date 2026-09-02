package com.kikyosoft.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.kikyosoft.api")
public class ProductApiExceptionHandler {
    @ExceptionHandler(ProductApiException.class)
    public ResponseEntity<Map<String, Object>> productApi(ProductApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage(), "status", ex.getStatus()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> missingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getParameterName() + " is required", "status", 400));
    }
}
