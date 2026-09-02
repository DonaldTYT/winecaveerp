package com.kikyosoft.api;

public class ProductApiException extends RuntimeException {
    private final int status;

    public ProductApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public ProductApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() { return status; }
}
